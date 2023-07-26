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

package org.apache.servicecomb.registry;

import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.client.utils.URIBuilder;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.registry.api.LifeCycle;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.Registration;
import org.apache.servicecomb.registry.api.RegistrationInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.json.jackson.JacksonFactory;

public class RegistrationManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationManager.class);

  // value is ip or {interface name}
  public static final String PUBLISH_ADDRESS = "servicecomb.service.publishAddress";

  private static final String PUBLISH_PORT = "servicecomb.{transport_name}.publishPort";

  private final List<Registration<? extends RegistrationInstance>> registrationList;

  public RegistrationManager(List<Registration<? extends RegistrationInstance>> registrationList) {
    if (registrationList == null) {
      this.registrationList = Collections.emptyList();
      return;
    }
    this.registrationList = registrationList;
  }

  public void updateMicroserviceInstanceStatus(MicroserviceInstanceStatus status) {
    registrationList
        .forEach(registration -> registration.updateMicroserviceInstanceStatus(status));
  }

  public void addSchema(String schemaId, String content) {
    registrationList
        .forEach(registration -> registration.addSchema(schemaId, content));
  }

  public void addEndpoint(String endpoint) {
    registrationList
        .forEach(registration -> registration.addEndpoint(endpoint));
  }

  public void destroy() {
    registrationList.forEach(LifeCycle::destroy);
  }

  public void run() {
    registrationList.forEach(LifeCycle::run);
  }

  public void init() {
    registrationList.forEach(LifeCycle::init);
  }

  /**
   * In the case that listening address configured as 0.0.0.0, the publish address will be determined
   * by the query result for the net interfaces.
   *
   * @return the publish address, or {@code null} if the param {@code address} is null.
   */
  public static String getPublishAddress(String schema, String address) {
    if (address == null) {
      return address;
    }

    try {
      URI originalURI = new URI(schema + "://" + address);
      IpPort ipPort = NetUtils.parseIpPort(originalURI);
      if (ipPort == null) {
        LOGGER.warn("address {} not valid.", address);
        return null;
      }

      IpPort publishIpPort = genPublishIpPort(schema, ipPort);
      URIBuilder builder = new URIBuilder(originalURI);
      return builder.setHost(publishIpPort.getHostOrIp()).setPort(publishIpPort.getPort()).build().toString();
    } catch (URISyntaxException e) {
      LOGGER.warn("address {} not valid.", address);
      return null;
    }
  }

  private static IpPort genPublishIpPort(String schema, IpPort ipPort) {
    String publicAddressSetting = DynamicPropertyFactory.getInstance()
        .getStringProperty(PUBLISH_ADDRESS, "")
        .get();
    publicAddressSetting = publicAddressSetting.trim();

    String publishPortKey = PUBLISH_PORT.replace("{transport_name}", schema);
    int publishPortSetting = DynamicPropertyFactory.getInstance()
        .getIntProperty(publishPortKey, 0)
        .get();
    int publishPort = publishPortSetting == 0 ? ipPort.getPort() : publishPortSetting;

    if (publicAddressSetting.isEmpty()) {
      InetSocketAddress socketAddress = ipPort.getSocketAddress();
      if (socketAddress.getAddress().isAnyLocalAddress()) {
        String host = NetUtils.getHostAddress();
        if (Inet6Address.class.isInstance(socketAddress.getAddress())) {
          host = NetUtils.getIpv6HostAddress();
        }
        LOGGER.warn("address {}, auto select a host address to publish {}:{}, maybe not the correct one",
            socketAddress,
            host,
            publishPort);
        return new IpPort(host, publishPort);
      }

      return ipPort;
    }

    if (publicAddressSetting.startsWith("{") && publicAddressSetting.endsWith("}")) {
      publicAddressSetting = NetUtils
          .ensureGetInterfaceAddress(
              publicAddressSetting.substring(1, publicAddressSetting.length() - 1))
          .getHostAddress();
    }

    return new IpPort(publicAddressSetting, publishPort);
  }

  public String info() {
    StringBuilder result = new StringBuilder();
    AtomicBoolean first = new AtomicBoolean(true);
    registrationList.forEach(registration -> {
      if (first.getAndSet(false)) {
        result.append("App ID: " + registration.getMicroserviceInstance().getApplication() + "\n");
        result.append("Service Name: " + registration.getMicroserviceInstance().getServiceName() + "\n");
        result.append("Version: " + registration.getMicroserviceInstance().getVersion() + "\n");
        result.append("Environment: " + registration.getMicroserviceInstance().getEnvironment() + "\n");
        result.append("Endpoints: " + getEndpoints(registration.getMicroserviceInstance().getEndpoints()) + "\n");
        result.append("Registration implementations:\n");
      }

      result.append("  name:" + registration.name() + "\n");
      result.append("    Instance ID: " + registration.getMicroserviceInstance().getInstanceId() + "\n");
    });
    return result.toString();
  }

  private String getEndpoints(List<String> endpoints) {
    return JacksonFactory.CODEC.toString(endpoints);
  }
}
