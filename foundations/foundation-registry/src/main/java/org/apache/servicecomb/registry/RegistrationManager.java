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

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URIBuilder;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.registry.api.registry.BasePath;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.Registration;
import org.apache.servicecomb.registry.swagger.SwaggerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

public class RegistrationManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationManager.class);

  // value is ip or {interface name}
  public static final String PUBLISH_ADDRESS = "servicecomb.service.publishAddress";

  private static final String PUBLISH_PORT = "servicecomb.{transport_name}.publishPort";

  public static RegistrationManager INSTANCE = new RegistrationManager();

  private final List<Registration> registrationList;

  private final Registration primary;

  private static SwaggerLoader swaggerLoader = new SwaggerLoader();

  private RegistrationManager() {
    registrationList = SPIServiceUtils.getOrLoadSortedService(Registration.class)
        .stream()
        .filter((registration -> registration.enabled()))
        .collect(Collectors.toList());
    if (registrationList.isEmpty()) {
      LOGGER.warn("No registration is enabled. Fix this if only in unit tests.");
      primary = null;
    } else {
      primary = registrationList.get(0);
    }
  }

  public MicroserviceInstance getMicroserviceInstance() {
    return primary.getMicroserviceInstance();
  }

  public Microservice getMicroservice() {
    return primary.getMicroservice();
  }

  public String getAppId() {
    return primary.getAppId();
  }

  public SwaggerLoader getSwaggerLoader() {
    return swaggerLoader;
  }

  public void updateMicroserviceInstanceStatus(
      MicroserviceInstanceStatus status) {
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

  public void addBasePath(Collection<BasePath> basePaths) {
    registrationList
        .forEach(registration -> registration.addBasePath(basePaths));
  }

  public void destroy() {
    registrationList.forEach(registration -> registration.destroy());
  }

  public void run() {
    registrationList.forEach(registration -> registration.run());
  }


  public void init() {
    registrationList.forEach(discovery -> discovery.init());
  }

  public static String getPublishAddress() {
    String publicAddressSetting =
        DynamicPropertyFactory.getInstance().getStringProperty(PUBLISH_ADDRESS, "").get();
    publicAddressSetting = publicAddressSetting.trim();
    if (publicAddressSetting.isEmpty()) {
      return NetUtils.getHostAddress();
    }

    // placeholder is network interface name
    if (publicAddressSetting.startsWith("{") && publicAddressSetting.endsWith("}")) {
      return NetUtils
          .ensureGetInterfaceAddress(publicAddressSetting.substring(1, publicAddressSetting.length() - 1))
          .getHostAddress();
    }

    return publicAddressSetting;
  }

  public static String getPublishHostName() {
    String publicAddressSetting =
        DynamicPropertyFactory.getInstance().getStringProperty(PUBLISH_ADDRESS, "").get();
    publicAddressSetting = publicAddressSetting.trim();
    if (publicAddressSetting.isEmpty()) {
      return NetUtils.getHostName();
    }

    if (publicAddressSetting.startsWith("{") && publicAddressSetting.endsWith("}")) {
      return NetUtils
          .ensureGetInterfaceAddress(publicAddressSetting.substring(1, publicAddressSetting.length() - 1))
          .getHostName();
    }

    return publicAddressSetting;
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

    if (publicAddressSetting.isEmpty()) {
      InetSocketAddress socketAddress = ipPort.getSocketAddress();
      if (socketAddress.getAddress().isAnyLocalAddress()) {
        String host = NetUtils.getHostAddress();
        LOGGER.warn("address {}, auto select a host address to publish {}:{}, maybe not the correct one",
            socketAddress,
            host,
            socketAddress.getPort());
        return new IpPort(host, ipPort.getPort());
      }

      return ipPort;
    }

    if (publicAddressSetting.startsWith("{") && publicAddressSetting.endsWith("}")) {
      publicAddressSetting = NetUtils
          .ensureGetInterfaceAddress(
              publicAddressSetting.substring(1, publicAddressSetting.length() - 1))
          .getHostAddress();
    }

    String publishPortKey = PUBLISH_PORT.replace("{transport_name}", schema);
    int publishPortSetting = DynamicPropertyFactory.getInstance()
        .getIntProperty(publishPortKey, 0)
        .get();
    int publishPort = publishPortSetting == 0 ? ipPort.getPort() : publishPortSetting;
    return new IpPort(publicAddressSetting, publishPort);
  }
}
