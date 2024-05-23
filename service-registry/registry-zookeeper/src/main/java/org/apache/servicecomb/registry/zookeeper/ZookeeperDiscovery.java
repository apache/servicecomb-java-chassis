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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.details.ServiceCacheListener;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.registry.api.Discovery;
import org.apache.zookeeper.server.auth.DigestLoginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class ZookeeperDiscovery implements Discovery<ZookeeperDiscoveryInstance> {
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

  private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperDiscovery.class);

  private final Map<String, Map<String, ServiceCache<ZookeeperInstance>>> serviceDiscoveries =
      new ConcurrentHashMapEx<>();

  private final Map<String, ServiceDiscovery<ZookeeperInstance>> serviceNameDiscoveries =
      new ConcurrentHashMapEx<>();

  private Environment environment;

  private ZookeeperRegistryProperties zookeeperRegistryProperties;

  private String basePath;

  private CuratorFramework client;

  private InstanceChangedListener<ZookeeperDiscoveryInstance> instanceChangedListener;

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

  @Override
  public String name() {
    return ZookeeperConst.ZOOKEEPER_REGISTRY_NAME;
  }

  @Override
  public boolean enabled(String application, String serviceName) {
    return environment.getProperty(String.format(ZookeeperConst.ZOOKEEPER_DISCOVERY_ENABLED, application, serviceName),
        boolean.class, true);
  }

  @Override
  public List<ZookeeperDiscoveryInstance> findServiceInstances(String application, String serviceName) {
    try {
      ServiceCache<ZookeeperInstance> discovery = serviceDiscoveries.computeIfAbsent(application, app ->
          new ConcurrentHashMapEx<>()).computeIfAbsent(serviceName, name -> {
        JsonInstanceSerializer<ZookeeperInstance> serializer =
            new JsonInstanceSerializer<>(ZookeeperInstance.class);
        ServiceDiscovery<ZookeeperInstance> dis = ServiceDiscoveryBuilder.builder(ZookeeperInstance.class)
            .client(client)
            .basePath(basePath + "/" + application)
            .serializer(serializer)
            .build();
        ServiceCache<ZookeeperInstance> cache =
            dis.serviceCacheBuilder().name(serviceName).build();
        cache.addListener(new ServiceCacheListener() {
          @Override
          public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
            LOGGER.warn("zookeeper discovery state changed {}", connectionState);
          }

          @Override
          public void cacheChanged() {
            instanceChangedListener.onInstanceChanged(name(), application, serviceName,
                toDiscoveryInstances(cache.getInstances()));
          }
        });
        try {
          CountDownLatch latch = cache.startImmediate();
          if (!latch.await(5000, TimeUnit.SECONDS)) {
            throw new IllegalStateException("cache start failed.");
          }
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
        return cache;
      });
      List<ServiceInstance<ZookeeperInstance>> instances = discovery.getInstances();
      return toDiscoveryInstances(instances);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public List<String> findServices(String application) {
    try {
      ServiceDiscovery<ZookeeperInstance> discovery = serviceNameDiscoveries
          .computeIfAbsent(application, app -> {
            JsonInstanceSerializer<ZookeeperInstance> serializer =
                new JsonInstanceSerializer<>(ZookeeperInstance.class);
            ServiceDiscovery<ZookeeperInstance> dis = ServiceDiscoveryBuilder.builder(ZookeeperInstance.class)
                .client(client)
                .basePath(basePath + "/" + application)
                .serializer(serializer)
                .build();
            try {
              dis.start();
            } catch (Exception e) {
              throw new IllegalStateException(e);
            }
            return dis;
          });
      return discovery.queryForNames().stream().toList();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private List<ZookeeperDiscoveryInstance> toDiscoveryInstances(
      List<ServiceInstance<ZookeeperInstance>> instances) {
    return instances.stream().map(instance ->
        new ZookeeperDiscoveryInstance(instance.getPayload())).collect(Collectors.toList());
  }

  @Override
  public void setInstanceChangedListener(InstanceChangedListener<ZookeeperDiscoveryInstance> instanceChangedListener) {
    this.instanceChangedListener = instanceChangedListener;
  }

  @Override
  public void init() {
    String env = BootStrapProperties.readServiceEnvironment(environment);
    if (StringUtils.isEmpty(env)) {
      env = ZookeeperConst.ZOOKEEPER_DEFAULT_ENVIRONMENT;
    }
    basePath = String.format(ZookeeperConst.ZOOKEEPER_DISCOVERY_ROOT, env);
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
      Configuration.setConfiguration(new ZookeeperSASLConfig(authInfo[0], authInfo[1]));
    }
    client = builder.build();
    client.start();
  }

  @Override
  public void destroy() {
    if (client != null) {
      CloseableUtils.closeQuietly(client);
    }
  }

  @Override
  public boolean enabled() {
    return zookeeperRegistryProperties.isEnabled();
  }
}
