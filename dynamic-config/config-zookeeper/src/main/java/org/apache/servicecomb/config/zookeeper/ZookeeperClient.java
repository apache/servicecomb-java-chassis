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
package org.apache.servicecomb.config.zookeeper;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.zookeeper.ZookeeperDynamicPropertiesSource.UpdateHandler;
import org.apache.zookeeper.server.auth.DigestLoginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;

public class ZookeeperClient {
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

  private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperClient.class);

  public static final String PATH_ENVIRONMENT = "/servicecomb/config/environment/%s";

  public static final String PATH_APPLICATION = "/servicecomb/config/application/%s/%s";

  public static final String PATH_SERVICE = "/servicecomb/config/service/%s/%s/%s";

  public static final String PATH_VERSION = "/servicecomb/config/version/%s/%s/%s/%s";

  public static final String PATH_TAG = "/servicecomb/config/tag/%s/%s/%s/%s/%s";

  private final UpdateHandler updateHandler;

  private final ZookeeperConfig zookeeperConfig;

  private final Environment environment;

  private final Object lock = new Object();

  private Map<String, Object> environmentData = new HashMap<>();

  private Map<String, Object> applicationData = new HashMap<>();

  private Map<String, Object> serviceData = new HashMap<>();

  private Map<String, Object> versionData = new HashMap<>();

  private Map<String, Object> tagData = new HashMap<>();

  private Map<String, Object> allLast = new HashMap<>();


  public ZookeeperClient(UpdateHandler updateHandler, Environment environment) {
    this.updateHandler = updateHandler;
    this.zookeeperConfig = new ZookeeperConfig(environment);
    this.environment = environment;
  }

  public void refreshZookeeperConfig() throws Exception {
    CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
        .connectString(zookeeperConfig.getConnectString())
        .sessionTimeoutMs(zookeeperConfig.getSessionTimeoutMillis())
        .retryPolicy(new ExponentialBackoffRetry(1000, 3));
    String authSchema = zookeeperConfig.getAuthSchema();
    if (StringUtils.isNotEmpty(authSchema)) {
      if (!"digest".equals(authSchema)) {
        throw new IllegalStateException("Not supported schema now. " + authSchema);
      }
      if (zookeeperConfig.getAuthInfo() == null) {
        throw new IllegalStateException("Auth info can not be empty. ");
      }

      String[] authInfo = zookeeperConfig.getAuthInfo().split(":");
      Configuration.setConfiguration(new ZookeeperSASLConfig(authInfo[0], authInfo[1]));
    }
    CuratorFramework client = builder.build();
    client.start();

    String env = BootStrapProperties.readServiceEnvironment(environment);
    if (StringUtils.isEmpty(env)) {
      env = ZookeeperConfig.ZOOKEEPER_DEFAULT_ENVIRONMENT;
    }
    addEnvironmentConfig(env, client);
    addApplicationConfig(env, client);
    addServiceConfig(env, client);
    addVersionConfig(env, client);
    addTagConfig(env, client);

    refreshConfigItems();
  }

  private void addTagConfig(String env, CuratorFramework client) throws Exception {
    if (StringUtils.isEmpty(zookeeperConfig.getInstanceTag())) {
      return;
    }
    String path = String.format(PATH_TAG, env,
        BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment),
        BootStrapProperties.readServiceVersion(environment),
        zookeeperConfig.getInstanceTag());
    CuratorCache cache = CuratorCache.builder(client, path).build();
    cache.listenable().addListener((type, oldData, newData) -> {
      try {
        this.tagData = parseData(client, path);
        refreshConfigItems();
      } catch (Exception e) {
        LOGGER.error("process event failed", e);
      }
    });
    cache.start();
    this.tagData = parseData(client, path);
  }

  private void addVersionConfig(String env, CuratorFramework client) throws Exception {
    String path = String.format(PATH_VERSION, env,
        BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment),
        BootStrapProperties.readServiceVersion(environment));
    CuratorCache cache = CuratorCache.builder(client, path).build();
    cache.listenable().addListener((type, oldData, newData) -> {
      try {
        this.versionData = parseData(client, path);
        refreshConfigItems();
      } catch (Exception e) {
        LOGGER.error("process event failed", e);
      }
    });
    cache.start();
    this.versionData = parseData(client, path);
  }

  private void addServiceConfig(String env, CuratorFramework client) throws Exception {
    String path = String.format(PATH_SERVICE, env,
        BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment));
    CuratorCache cache = CuratorCache.builder(client, path).build();
    cache.listenable().addListener((type, oldData, newData) -> {
      try {
        this.serviceData = parseData(client, path);
        refreshConfigItems();
      } catch (Exception e) {
        LOGGER.error("process event failed", e);
      }
    });
    cache.start();
    this.serviceData = parseData(client, path);
  }

  private void addApplicationConfig(String env, CuratorFramework client) throws Exception {
    String path = String.format(PATH_APPLICATION, env, BootStrapProperties.readApplication(environment));
    CuratorCache cache = CuratorCache.builder(client, path).build();
    cache.listenable().addListener((type, oldData, newData) -> {
      try {
        this.applicationData = parseData(client, path);
        refreshConfigItems();
      } catch (Exception e) {
        LOGGER.error("process event failed", e);
      }
    });
    cache.start();
    this.applicationData = parseData(client, path);
  }

  private void addEnvironmentConfig(String env, CuratorFramework client) throws Exception {
    String path = String.format(PATH_ENVIRONMENT, env);
    CuratorCache cache = CuratorCache.builder(client, path).build();
    cache.listenable().addListener((type, oldData, newData) -> {
      try {
        this.environmentData = parseData(client, path);
        refreshConfigItems();
      } catch (Exception e) {
        LOGGER.error("process event failed", e);
      }
    });
    cache.start();
    this.environmentData = parseData(client, path);
  }

  private Map<String, Object> parseData(CuratorFramework client, String path) throws Exception {
    Map<String, Object> values = new HashMap<>();

    if (client.checkExists().forPath(path) != null) {
      client.getChildren().forPath(path).stream().sorted().forEach(item -> {
        try {
          byte[] data = client.getData().forPath(path + "/" + item);
          if (item.endsWith(".yaml") || item.endsWith(".yml")) {
            YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
            yamlFactory.setResources(new ByteArrayResource(data));
            values.putAll(toMap(yamlFactory.getObject()));
          } else if (item.endsWith(".properties")) {
            Properties properties = new Properties();
            properties.load(new StringReader(new String(data, StandardCharsets.UTF_8)));
            values.putAll(toMap(properties));
          } else {
            values.put(item, new String(data, StandardCharsets.UTF_8));
          }
        } catch (Exception e) {
          LOGGER.error("", e);
        }
      });
    }

    return values;
  }

  private void refreshConfigItems() {
    synchronized (lock) {
      Map<String, Object> all = new HashMap<>();
      all.putAll(environmentData);
      all.putAll(applicationData);
      all.putAll(serviceData);
      all.putAll(versionData);
      all.putAll(tagData);
      updateHandler.handle(all, allLast);
      this.allLast = all;
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> toMap(Properties properties) {
    if (properties == null) {
      return Collections.emptyMap();
    }
    Map<String, Object> result = new HashMap<>();
    Enumeration<String> keys = (Enumeration<String>) properties.propertyNames();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      Object value = properties.getProperty(key);
      result.put(key, value);
    }
    return result;
  }
}
