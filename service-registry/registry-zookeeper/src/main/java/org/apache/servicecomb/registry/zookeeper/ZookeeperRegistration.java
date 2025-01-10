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
package org.apache.servicecomb.registry.zookeeper;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.DataCenterProperties;
import org.apache.servicecomb.registry.RegistrationId;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.Registration;
import org.apache.zookeeper.server.auth.DigestLoginModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class ZookeeperRegistration implements Registration<ZookeeperRegistrationInstance> {
  static class ZookeeperSASLConfig extends Configuration {
    AppConfigurationEntry entry;

    public ZookeeperSASLConfig(String username,
        String password) {
      Map<String, String> options = new HashMap<>();
      options.put("username", username);
      options.put("password", password);
      this.entry = new AppConfigurationEntry(
          DigestLoginModule.class.getName(),
          AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
          options
      );
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
      AppConfigurationEntry[] array = new AppConfigurationEntry[1];
      array[0] = entry;
      return array;
    }
  }

  private Environment environment;

  private ZookeeperRegistryProperties zookeeperRegistryProperties;

  private DataCenterProperties dataCenterProperties;

  private String basePath;

  private CuratorFramework client;

  private ServiceInstance<ZookeeperInstance> instance;

  private RegistrationId registrationId;

  private ServiceDiscovery<ZookeeperInstance> dis;

  @Autowired
  @SuppressWarnings("unused")
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Autowired
  @SuppressWarnings("unused")
  public void setZookeeperRegistryProperties(ZookeeperRegistryProperties zookeeperRegistryProperties) {
    this.zookeeperRegistryProperties = zookeeperRegistryProperties;
  }

  @Autowired
  public void setRegistrationId(RegistrationId registrationId) {
    this.registrationId = registrationId;
  }

  @Autowired
  @SuppressWarnings("unused")
  public void setDataCenterProperties(DataCenterProperties dataCenterProperties) {
    this.dataCenterProperties = dataCenterProperties;
  }

  @Override
  public void init() {
    String env = BootStrapProperties.readServiceEnvironment(environment);
    if (StringUtils.isEmpty(env)) {
      env = ZookeeperConst.ZOOKEEPER_DEFAULT_ENVIRONMENT;
    }
    basePath = String.format(ZookeeperConst.ZOOKEEPER_DISCOVERY_ROOT, env);
    ZookeeperInstance zookeeperInstance = new ZookeeperInstance();
    zookeeperInstance.setInstanceId(registrationId.getInstanceId());
    zookeeperInstance.setEnvironment(env);
    zookeeperInstance.setApplication(BootStrapProperties.readApplication(environment));
    zookeeperInstance.setServiceName(BootStrapProperties.readServiceName(environment));
    zookeeperInstance.setAlias(BootStrapProperties.readServiceAlias(environment));
    zookeeperInstance.setDescription(BootStrapProperties.readServiceDescription(environment));
    if (StringUtils.isNotEmpty(dataCenterProperties.getName())) {
      DataCenterInfo dataCenterInfo = new DataCenterInfo();
      dataCenterInfo.setName(dataCenterProperties.getName());
      dataCenterInfo.setRegion(dataCenterProperties.getRegion());
      dataCenterInfo.setAvailableZone(dataCenterProperties.getAvailableZone());
      zookeeperInstance.setDataCenterInfo(dataCenterInfo);
    }
    zookeeperInstance.setProperties(BootStrapProperties.readServiceProperties(environment));
    zookeeperInstance.setVersion(BootStrapProperties.readServiceVersion(environment));

    zookeeperInstance.setStatus(
        MicroserviceInstanceStatus.valueOf(BootStrapProperties.readServiceInstanceInitialStatus(environment)));
    try {
      this.instance = ServiceInstance.<ZookeeperInstance>builder().name(zookeeperInstance.getServiceName())
          .id(zookeeperInstance.getInstanceId()).payload(zookeeperInstance).build();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void run() {
    CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
        .connectString(zookeeperRegistryProperties.getConnectString())
        .sessionTimeoutMs(zookeeperRegistryProperties.getSessionTimeoutMillis())
        .retryPolicy(new ExponentialBackoffRetry(1000, 3));
    String authSchema = zookeeperRegistryProperties.getAuthenticationSchema();
    if (StringUtils.isNotEmpty(authSchema)) {
      if (!"digest".equals(authSchema)) {
        throw new IllegalStateException("Not supported schema now. " + authSchema);
      }
      if (zookeeperRegistryProperties.getAuthenticationInfo() == null) {
        throw new IllegalStateException("Auth info can not be empty. ");
      }

      String[] authInfo = zookeeperRegistryProperties.getAuthenticationInfo().split(":");
      Configuration.setConfiguration(new ZookeeperDiscovery.ZookeeperSASLConfig(authInfo[0], authInfo[1]));
    }
    client = builder.build();
    client.start();
    JsonInstanceSerializer<ZookeeperInstance> serializer =
        new JsonInstanceSerializer<>(ZookeeperInstance.class);
    dis = ServiceDiscoveryBuilder.builder(ZookeeperInstance.class)
        .client(client)
        .basePath(basePath + "/" + BootStrapProperties.readApplication(environment))
        .serializer(serializer)
        .thisInstance(instance)
        .build();
    try {
      dis.start();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void destroy() {
    if (client != null) {
      CloseableUtils.closeQuietly(client);
    }
  }

  @Override
  public String name() {
    return ZookeeperConst.ZOOKEEPER_REGISTRY_NAME;
  }

  @Override
  public ZookeeperRegistrationInstance getMicroserviceInstance() {
    return new ZookeeperRegistrationInstance(instance.getPayload());
  }

  @Override
  public boolean updateMicroserviceInstanceStatus(MicroserviceInstanceStatus status) {
    this.instance.getPayload().setStatus(status);
    try {
      dis.updateService(instance);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return true;
  }

  @Override
  public void addSchema(String schemaId, String content) {
    if (zookeeperRegistryProperties.isEnableSwaggerRegistration()) {
      instance.getPayload().addSchema(schemaId, content);
    }
  }

  @Override
  public void addEndpoint(String endpoint) {
    instance.getPayload().addEndpoint(endpoint);
  }

  @Override
  public void addProperty(String key, String value) {
    instance.getPayload().addProperty(key, value);
  }

  @Override
  public boolean enabled() {
    return zookeeperRegistryProperties.isEnabled();
  }
}
