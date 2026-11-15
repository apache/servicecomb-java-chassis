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

package org.apache.servicecomb.localservice.it;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.localservice.LocalServiceBootListener;
import org.apache.servicecomb.localservice.LocalServiceConfig;
import org.apache.servicecomb.localservice.RouterHolderDispatcher;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;

/**
 * Integration tests with real HTTP servers and real loopback connections.
 *
 * <p>Run explicitly: {@code mvn test -Dtest=*IT -pl handlers/handler-localservice}
 */
public class TestLocalServiceBootListenerIT {
  private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(5);

  private final HttpClient httpClient = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(2))
      .build();

  private final LocalServiceBootListener listener = new LocalServiceBootListener();

  private final MockEnvironment environment = new MockEnvironment();

  private Vertx vertx;

  private HttpServer mainServer;

  private Router router;

  private int port;

  private String localIp;

  @BeforeEach
  public void setUp() throws Exception {
    LegacyPropertyFactory.setEnvironment(environment);
    RouterHolderDispatcher.reset();

    port = freePort();

    InetAddress localHost = InetAddress.getLocalHost();
    localIp = localHost.getHostAddress();
    Assumptions.assumeFalse(localHost.isLoopbackAddress(),
        "test machine only has loopback address, real network tests are skipped");
  }

  @AfterEach
  public void tearDown() throws Exception {
    try {
      listener.onBeforeClose(new BootListener.BootEvent());
    } finally {
      if (mainServer != null) {
        mainServer.close().toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
        mainServer = null;
      }
      if (vertx != null) {
        vertx.close().toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
        vertx = null;
      }
      LegacyPropertyFactory.setEnvironment(null);
      RouterHolderDispatcher.reset();
    }
  }

  private static int freePort() throws Exception {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }

  /**
   * Simulate RestServerVerticle: create a Router with a test route, start the main
   * HTTP server and capture the Router through RouterHolderDispatcher.
   */
  private void startMainServer(String host, int port) throws Exception {
    if (vertx == null) {
      vertx = Vertx.vertx();
    }
    Router router = Router.router(vertx);
    router.route("/ping").handler(ctx -> ctx.response().setStatusCode(200).end("pong"));

    this.router = router;
    new RouterHolderDispatcher().init(router);

    mainServer = vertx.createHttpServer(new HttpServerOptions().setHost(host).setPort(port));
    mainServer.requestHandler(router);
    mainServer.listen(port, host)
        .toCompletionStage()
        .toCompletableFuture()
        .get(10, TimeUnit.SECONDS);
  }

  private int status(String url) throws Exception {
    HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return response.statusCode();
  }

  private void awaitOk(String url) {
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> {
      try {
        return status(url) == 200;
      } catch (Exception e) {
        return false;
      }
    });
  }

  private void awaitUnreachable(String url) {
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> {
      try {
        status(url);
        return false;
      } catch (Exception e) {
        return true;
      }
    });
  }

  private void enableLocalService(String restAddress) {
    environment.setProperty(LocalServiceConfig.KEY_ENABLED, "true");
    if (restAddress != null) {
      environment.setProperty("servicecomb.rest.address", restAddress);
    }
  }

  @Test
  public void specificIpAddressStartsLocalListenerAndServesLoopback() throws Exception {
    startMainServer(localIp, port);
    enableLocalService(localIp + ":" + port);

    listener.onAfterTransport(new BootListener.BootEvent());

    Assertions.assertEquals(1, listener.localServerCount());
    // the main server only binds the external address, loopback must be served
    // by the local listener
    awaitOk("http://127.0.0.1:" + port + "/ping");
    Assertions.assertEquals(200, status("http://" + localIp + ":" + port + "/ping"));
  }

  @Test
  public void wildcardAddressWithoutSslSkipsLocalListener() throws Exception {
    startMainServer("0.0.0.0", port);
    enableLocalService("0.0.0.0:" + port);

    listener.onAfterTransport(new BootListener.BootEvent());

    Assertions.assertEquals(0, listener.localServerCount());
    // loopback is already served by the wildcard bound main server
    awaitOk("http://127.0.0.1:" + port + "/ping");
  }

  @Test
  public void wildcardTlsAddressSkipsLocalListener() throws Exception {
    // the rest address is configured as a TLS wildcard binding; the listener decision
    // reads the configured address, so the main server here may be plain HTTP - in
    // production it would be TLS
    startMainServer("0.0.0.0", port);
    enableLocalService("0.0.0.0:" + port + "?sslEnabled=true");

    listener.onAfterTransport(new BootListener.BootEvent());

    Assertions.assertEquals(0, listener.localServerCount());
  }

  @Test
  public void disabledDoesNotStartAnyServer() throws Exception {
    startMainServer(localIp, port);

    listener.onAfterTransport(new BootListener.BootEvent());

    Assertions.assertEquals(0, listener.localServerCount());
  }

  @Test
  public void missingRouterSkipsLocalListener() throws Exception {
    // main server started without capturing the Router
    if (vertx == null) {
      vertx = Vertx.vertx();
    }
    Router router = Router.router(vertx);
    router.route("/ping").handler(ctx -> ctx.response().setStatusCode(200).end("pong"));
    mainServer = vertx.createHttpServer(new HttpServerOptions().setHost(localIp).setPort(port));
    mainServer.requestHandler(router);
    mainServer.listen(port, localIp)
        .toCompletionStage()
        .toCompletableFuture()
        .get(10, TimeUnit.SECONDS);

    enableLocalService(localIp + ":" + port);
    listener.onAfterTransport(new BootListener.BootEvent());

    Assertions.assertEquals(0, listener.localServerCount());
  }

  @Test
  public void onBeforeCloseStopsLocalListener() throws Exception {
    startMainServer(localIp, port);
    enableLocalService(localIp + ":" + port);

    listener.onAfterTransport(new BootListener.BootEvent());
    awaitOk("http://127.0.0.1:" + port + "/ping");

    listener.onBeforeClose(new BootListener.BootEvent());
    awaitUnreachable("http://127.0.0.1:" + port + "/ping");
    Assertions.assertEquals(0, listener.localServerCount());

    // the main server on the external address is not affected
    Assertions.assertEquals(200, status("http://" + localIp + ":" + port + "/ping"));
  }

  @Test
  public void restartAfterCloseStartsNewLocalListener() throws Exception {
    startMainServer(localIp, port);
    enableLocalService(localIp + ":" + port);

    listener.onAfterTransport(new BootListener.BootEvent());
    awaitOk("http://127.0.0.1:" + port + "/ping");

    listener.onBeforeClose(new BootListener.BootEvent());
    awaitUnreachable("http://127.0.0.1:" + port + "/ping");

    // a real engine restart re-captures the Router through the dispatcher
    // (onBeforeClose releases it), simulate that before starting again
    new RouterHolderDispatcher().init(router);
    listener.onAfterTransport(new BootListener.BootEvent());
    Assertions.assertEquals(1, listener.localServerCount());
    awaitOk("http://127.0.0.1:" + port + "/ping");
  }
}
