/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.localservice;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.foundation.vertx.SharedVertxFactory;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.Registration;
import org.apache.servicecomb.registry.api.RegistrationInstance;
import org.apache.servicecomb.transport.rest.vertx.TransportConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;

/**
 * BootListener that starts local HTTP servers on 127.0.0.1, reusing the Router
 * captured by {@link RouterHolderDispatcher} from RestServerVerticle.
 *
 * <p>This allows local service instances to communicate without network overhead.
 *
 * <p>When a REST listen address is wildcard bound (e.g. 0.0.0.0), plain HTTP or TLS,
 * connections to 127.0.0.1 on the same port are already served by the main server,
 * so no additional listener is started. Local listeners are only started for
 * specific-IP listen addresses.
 *
 * <p>Because the consumer side rewrites local (P1) endpoints to plain HTTP loopback,
 * a provider may only advertise {@code supportLocalService=true} when the loopback
 * port serves plain HTTP. This listener guarantees that precondition: whenever the
 * rest address does not serve plain HTTP on 127.0.0.1 (e.g. a TLS wildcard binding,
 * or the listener could not start), a declared {@code supportLocalService=true}
 * instance property is automatically corrected to {@code false} before registration.
 *
 * <p>Registered as a Spring bean by {@link LocalServiceConfiguration}; in Java Chassis 3.x
 * BootListener instances are collected via Spring injection instead of Java SPI.
 */
public class LocalServiceBootListener implements BootListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceBootListener.class);

  private static final String LOCAL_HOST = "127.0.0.1";

  private static final String SSL_ENABLED_KEY = "sslEnabled";

  private static final String SUPPORT_LOCAL_SERVICE = "supportLocalService";

  private final List<HttpServer> localServers = new CopyOnWriteArrayList<>();

  @Override
  public int getOrder() {
    return 100;
  }

  @Override
  public void onAfterTransport(BootEvent event) {
    if (!LocalServiceConfig.isEnabled()) {
      LOGGER.debug("Local service listener is disabled");
      return;
    }

    boolean loopbackPlainHttp;
    try {
      loopbackPlainHttp = startLocalServers();
    } catch (Exception e) {
      LOGGER.error("Failed to start local service listener", e);
      loopbackPlainHttp = false;
    }
    if (!loopbackPlainHttp) {
      autoCorrectSupportLocalService();
    }
  }

  @Override
  public void onBeforeClose(BootEvent event) {
    stopLocalServers();
    // release the Router captured by RouterHolderDispatcher, so that a same-JVM
    // engine restart does not observe a stale Router
    RouterHolderDispatcher.reset();
  }

  /**
   * Start the local listener when needed.
   *
   * @return true when the loopback port of the rest address is guaranteed to serve
   *         plain HTTP (wildcard plain HTTP binding covered by the main server, or a
   *         local listener started for a specific-IP binding); false otherwise.
   */
  private boolean startLocalServers() {
    String addressConfig = TransportConfig.getAddress();
    if (StringUtils.isEmpty(addressConfig)) {
      LOGGER.warn("rest listen address is not configured, cannot start local server");
      return false;
    }

    // Java Chassis 3.x treats servicecomb.rest.address as a single listen address
    String address = addressConfig.trim();
    if (!isLoopbackPlainHttp(address)) {
      // covers the wildcard TLS case: loopback is served by the main server, but with TLS
      LOGGER.warn("rest address [{}] does not serve plain HTTP on loopback, "
          + "local service support is not available", address);
      return false;
    }

    IpPort target = parseLocalListenTarget(address);
    if (target == null) {
      // wildcard plain HTTP binding: loopback is already covered by the main server.
      // No local listener is needed and no Router is required, so this also covers
      // deployments where REST is served by another transport (e.g. the servlet
      // transport) without a RestServerVerticle.
      LOGGER.info("rest address [{}] is wildcard bound and serves plain HTTP on loopback, "
          + "skip local listener", address);
      return true;
    }

    // Specific-IP binding: a local plain HTTP listener is required. It reuses the Router
    // captured from RestServerVerticle; do not try to rebuild the mount chain here,
    // because it would duplicate RestServerVerticle logic and drift.
    Router router = RouterHolderDispatcher.getCapturedRouter();
    if (router == null) {
      LOGGER.warn("Could not get Router from RestServerVerticle, local service listener will not start");
      return false;
    }

    Vertx vertx = SharedVertxFactory.getSharedVertx(LegacyPropertyFactory.getEnvironment());
    if (vertx == null) {
      LOGGER.warn("Shared Vertx is not available, cannot start local server");
      return false;
    }

    startLocalServer(vertx, router, address, target);
    return true;
  }

  private void startLocalServer(Vertx vertx, Router router, String address, IpPort target) {
    HttpServerOptions serverOptions = new HttpServerOptions();
    serverOptions.setHost(LOCAL_HOST);
    serverOptions.setPort(target.getPort());

    HttpServer localServer = vertx.createHttpServer(serverOptions);
    localServer.requestHandler(router);
    // register immediately so that onBeforeClose can still close a server whose
    // listen() has not completed yet; drop it again when listen() fails, so that
    // localServerCount() only reports servers that actually listen
    localServers.add(localServer);
    localServer.listen().onComplete(result -> {
      if (result.succeeded()) {
        LOGGER.info("Local service listener started on {}:{} for rest address [{}]",
            LOCAL_HOST, result.result().actualPort(), address);
      } else {
        localServers.remove(localServer);
        LOGGER.error("Failed to start local service listener for rest address [{}]", address, result.cause());
      }
    });
  }

  /**
   * When the loopback port does not serve plain HTTP, a declared
   * {@code supportLocalService=true} instance property would make consumers rewrite
   * requests to a plain HTTP call against a non plain HTTP port, which is a black hole.
   * Correct the declared property to {@code false} before registration publishes it.
   *
   * <p>Runs at {@code AFTER_TRANSPORT}, i.e. after {@code RegistrationManager.init()}
   * (declared properties are loaded) and before {@code RegistrationManager.run()}
   * (registration publishes), so the corrected value is what consumers discover.
   */
  void autoCorrectSupportLocalService(RegistrationManager registrationManager) {
    try {
      Registration<? extends RegistrationInstance> registration = registrationManager.getPrimaryRegistration();
      if (registration == null) {
        return;
      }
      Map<String, String> properties = registration.getMicroserviceInstance().getProperties();
      if (properties == null || !Boolean.parseBoolean(properties.get(SUPPORT_LOCAL_SERVICE))) {
        // not declared as true, nothing to correct
        return;
      }
      registrationManager.addProperty(SUPPORT_LOCAL_SERVICE, "false");
      LOGGER.warn("instance property supportLocalService=true is declared, but the rest address does not "
          + "serve plain HTTP on 127.0.0.1 (wildcard TLS binding, or the local listener did not start), "
          + "corrected it to false before registration");
    } catch (Exception e) {
      LOGGER.warn("Failed to auto-correct supportLocalService property: {}", e.getMessage());
    }
  }

  private void autoCorrectSupportLocalService() {
    try {
      SCBEngine engine = SCBEngine.getInstance();
      autoCorrectSupportLocalService(engine.getRegistrationManager());
    } catch (Exception e) {
      LOGGER.warn("Failed to auto-correct supportLocalService property: {}", e.getMessage());
    }
  }

  /**
   * Decide whether a local listener is needed for the given rest address and
   * return the parsed loopback listen target, or null when no listener is needed.
   *
   * <p>Wildcard bindings (e.g. 0.0.0.0), plain HTTP or TLS, already serve connections
   * to 127.0.0.1 on the same port, so an additional loopback listener is never started:
   * it would be redundant for plain HTTP wildcard bindings, and would fail to bind on
   * several platforms for TLS wildcard bindings.
   *
   * <p>Package private for testing.
   */
  static IpPort parseLocalListenTarget(String address) {
    IpPort ipPort;
    try {
      ipPort = NetUtils.parseIpPort(address);
    } catch (Exception e) {
      LOGGER.warn("Illegal rest address [{}], cannot start local server on it", address);
      return null;
    }
    if (ipPort == null || ipPort.getPort() <= 0) {
      LOGGER.warn("Illegal rest address [{}], cannot start local server on it", address);
      return null;
    }

    if (isWildcardHost(ipPort.getHostOrIp())) {
      LOGGER.info("rest address [{}] is wildcard bound, loopback connections are already served "
          + "by the main server, skip local listener", address);
      return null;
    }
    return ipPort;
  }

  /**
   * Decide whether the loopback port of the given rest address is guaranteed to
   * serve plain HTTP, which is the precondition for advertising
   * {@code supportLocalService=true}.
   *
   * <ul>
   * <li>wildcard plain HTTP binding: the main server itself covers 127.0.0.1, plain HTTP</li>
   * <li>wildcard TLS binding: the main server covers 127.0.0.1, but it is TLS, so no</li>
   * <li>specific address binding: the local listener covers 127.0.0.1 with plain HTTP</li>
   * </ul>
   *
   * <p>Package private for testing.
   */
  static boolean isLoopbackPlainHttp(String address) {
    if (StringUtils.isEmpty(address)) {
      return false;
    }
    try {
      URI uri = URI.create("http://" + address.trim());
      IpPort ipPort = NetUtils.parseIpPort(uri, true);
      if (ipPort == null || ipPort.getPort() <= 0) {
        return false;
      }
      if (isWildcardHost(ipPort.getHostOrIp())) {
        // the main server serves loopback directly; it is plain HTTP only without TLS
        return !isSslEnabled(uri);
      }
      // specific address: the local plain HTTP listener covers loopback
      return true;
    } catch (Exception e) {
      LOGGER.warn("Illegal rest address [{}], cannot decide loopback plain HTTP", address);
      return false;
    }
  }

  static boolean isWildcardHost(String host) {
    if (StringUtils.isEmpty(host)) {
      return true;
    }
    try {
      return InetAddress.getByName(host).isAnyLocalAddress();
    } catch (Exception e) {
      LOGGER.debug("Failed to resolve host [{}] for wildcard check", host);
      return false;
    }
  }

  static boolean isSslEnabled(URI uri) {
    String query = uri.getQuery();
    if (StringUtils.isEmpty(query)) {
      return false;
    }
    for (String pair : query.split("&")) {
      String[] kv = pair.split("=", 2);
      if (SSL_ENABLED_KEY.equals(kv[0]) && Boolean.parseBoolean(kv.length > 1 ? kv[1] : "")) {
        return true;
      }
    }
    return false;
  }

  private void stopLocalServers() {
    for (HttpServer localServer : localServers) {
      localServer.close().onComplete(result -> {
        if (result.succeeded()) {
          LOGGER.info("Local service listener stopped");
        } else {
          LOGGER.error("Failed to stop local service listener", result.cause());
        }
      });
    }
    localServers.clear();
  }

  /**
   * Number of local servers currently started, public for integration tests
   * in the {@code it} subpackage.
   */
  public int localServerCount() {
    return localServers.size();
  }
}
