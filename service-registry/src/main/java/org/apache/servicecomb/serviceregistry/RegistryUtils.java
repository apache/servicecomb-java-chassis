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

package org.apache.servicecomb.serviceregistry;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManager;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceDefinition;
import org.apache.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.netflix.config.DynamicPropertyFactory;

public final class RegistryUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegistryUtils.class);

  private static ServiceRegistry serviceRegistry;

  // value is ip or {interface name}
  public static final String PUBLISH_ADDRESS = "servicecomb.service.publishAddress";

  private static final String PUBLISH_PORT = "servicecomb.{transport_name}.publishPort";

  private RegistryUtils() {
  }

  public static void init() {
    MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader();
    MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(loader.getConfigModels());
    serviceRegistry =
        ServiceRegistryFactory
            .getOrCreate(EventManager.eventBus, ServiceRegistryConfig.INSTANCE, microserviceDefinition);
    serviceRegistry.init();
  }

  public static void run() {
    serviceRegistry.run();
  }

  public static void destroy() {
    serviceRegistry.destroy();
  }

  /**
   * @deprecated Replace by {@link #destroy()}
   */
  @Deprecated
  public static void destory() {
    destroy();
  }

  public static ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

  public static void setServiceRegistry(ServiceRegistry serviceRegistry) {
    RegistryUtils.serviceRegistry = serviceRegistry;
  }

  public static ServiceRegistryClient getServiceRegistryClient() {
    return serviceRegistry.getServiceRegistryClient();
  }

  public static InstanceCacheManager getInstanceCacheManager() {
    return serviceRegistry.getInstanceCacheManager();
  }

  public static String getAppId() {
    return serviceRegistry.getMicroservice().getAppId();
  }

  public static Microservice getMicroservice() {
    return serviceRegistry.getMicroservice();
  }

  public static MicroserviceInstance getMicroserviceInstance() {
    return serviceRegistry.getMicroserviceInstance();
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
   * 对于配置为0.0.0.0的地址，通过查询网卡地址，转换为实际监听的地址。
   */
  public static String getPublishAddress(String schema, String address) {
    if (address == null) {
      return address;
    }

    try {
      URI originalURI = new URI(schema + "://" + address);
      IpPort ipPort = NetUtils.parseIpPort(originalURI.getAuthority());
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

  public static List<MicroserviceInstance> findServiceInstance(String appId, String serviceName,
      String versionRule) {
    return serviceRegistry.findServiceInstance(appId, serviceName, versionRule);
  }

  // update microservice instance properties
  public static boolean updateInstanceProperties(Map<String, String> instanceProperties) {
    return serviceRegistry.updateInstanceProperties(instanceProperties);
  }

  public static Microservice getMicroservice(String microserviceId) {
    return serviceRegistry.getRemoteMicroservice(microserviceId);
  }

  public static MicroserviceInstances findServiceInstances(String appId, String serviceName,
      String versionRule, String revision) {
    return serviceRegistry.findServiceInstances(appId, serviceName, versionRule, revision);
  }

  public static String calcSchemaSummary(String schemaContent) {
    return Hashing.sha256().newHasher().putString(schemaContent, Charsets.UTF_8).hash().toString();
  }
}
