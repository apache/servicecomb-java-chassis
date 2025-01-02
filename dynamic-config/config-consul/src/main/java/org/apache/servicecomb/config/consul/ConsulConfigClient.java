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

package org.apache.servicecomb.config.consul;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.jetbrains.annotations.NotNull;
import org.kiwiproject.consul.Consul;
import org.kiwiproject.consul.KeyValueClient;
import org.kiwiproject.consul.cache.KVCache;
import org.kiwiproject.consul.model.kv.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.apache.servicecomb.config.consul.ConsulConfig.PATH_APPLICATION;
import static org.apache.servicecomb.config.consul.ConsulConfig.PATH_ENVIRONMENT;
import static org.apache.servicecomb.config.consul.ConsulConfig.PATH_SERVICE;
import static org.apache.servicecomb.config.consul.ConsulConfig.PATH_TAG;
import static org.apache.servicecomb.config.consul.ConsulConfig.PATH_VERSION;

public class ConsulConfigClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsulConfigClient.class);

  public class GetDataRunnable implements Runnable {

    private Map<String, Object> dataMap;

    private String path;

    public GetDataRunnable(String path, Map<String, Object> dataMap) {
      this.dataMap = dataMap;
      this.path = path;
    }

    @Override
    public void run() {
      try {
        if (path.equals("tagData")) {
          tagData = dataMap;
        } else if (path.equals("versionData")) {
          versionData = dataMap;
        } else if (path.equals("serviceData")) {
          serviceData = dataMap;
        } else if (path.equals("applicationData")) {
          applicationData = dataMap;
        } else {
          environmentData = dataMap;
        }
        refreshConfigItems();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private ConsulDynamicPropertiesSource.UpdateHandler updateHandler;

  private ConsulConfig consulConfig;

  private Environment environment;

  private final Object lock = new Object();

  private Consul consulClient;

  private KeyValueClient kvClient;

  private ConsulConfigProperties consulConfigProperties;

  private Map<String, Object> environmentData = new HashMap<>();

  private Map<String, Object> applicationData = new HashMap<>();

  private Map<String, Object> serviceData = new HashMap<>();

  private Map<String, Object> versionData = new HashMap<>();

  private Map<String, Object> tagData = new HashMap<>();

  private Map<String, Object> allLast = new HashMap<>();

  public ConsulConfigClient(ConsulDynamicPropertiesSource.UpdateHandler updateHandler, Environment environment, ConsulConfigProperties consulConfigProperties, Consul consulClient) {
    this.updateHandler = updateHandler;
    this.consulConfig = new ConsulConfig(environment);
    this.environment = environment;
    this.consulConfigProperties = consulConfigProperties;
    this.consulClient = consulClient;
    this.kvClient = consulClient.keyValueClient();
  }

  public void refreshConsulConfig() {
    String env = BootStrapProperties.readServiceEnvironment(environment);
    if (StringUtils.isEmpty(env)) {
      env = ConsulConfig.CONSUL_DEFAULT_ENVIRONMENT;
    }
    addEnvironmentConfig(env);
    addApplicationConfig(env);
    addServiceConfig(env);
    addVersionConfig(env);
    addTagConfig(env);

    refreshConfigItems();
  }

  private void addTagConfig(String env) {
    if (StringUtils.isBlank(consulConfig.getInstanceTag())) {
      return;
    }
    String path = String.format(PATH_TAG, env,
        BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment),
        BootStrapProperties.readServiceVersion(environment),
        consulConfig.getInstanceTag());

    this.tagData = parseData(path);
    try (KVCache cache = KVCache.newCache(kvClient, path, consulConfig.getConsulWatchSeconds())) {
      cache.addListener(newValues -> {
        Optional<Value> newValue = newValues.values().stream()
            .filter(value -> value.getKey().equals(path))
            .findAny();

        newValue.ifPresent(value -> {
          Optional<String> decodedValue = newValue.get().getValueAsString();
          decodedValue.ifPresent(v -> new Thread(new GetDataRunnable("tagData", getValues(path))).start());
        });
      });
      cache.start();
    }
  }

  private void addVersionConfig(String env) {
    String path = String.format(PATH_VERSION, env,
        BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment),
        BootStrapProperties.readServiceVersion(environment));

    this.versionData = parseData(path);
    try (KVCache cache = KVCache.newCache(kvClient, path, consulConfig.getConsulWatchSeconds())) {
      cache.addListener(newValues -> {
        Optional<Value> newValue = newValues.values().stream()
            .filter(value -> value.getKey().equals(path))
            .findAny();

        newValue.ifPresent(value -> {
          Optional<String> decodedValue = newValue.get().getValueAsString();
          decodedValue.ifPresent(v -> new Thread(new GetDataRunnable("versionData", getValues(path))).start());
        });
      });
      cache.start();
    }
  }

  private void addServiceConfig(String env) {
    String path = String.format(PATH_SERVICE, env,
        BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment));

    this.serviceData = parseData(path);
    try (KVCache cache = KVCache.newCache(kvClient, path, consulConfig.getConsulWatchSeconds())) {
      cache.addListener(newValues -> {
        Optional<Value> newValue = newValues.values().stream()
            .filter(value -> value.getKey().equals(path))
            .findAny();

        newValue.ifPresent(value -> {
          Optional<String> decodedValue = newValue.get().getValueAsString();
          decodedValue.ifPresent(v -> new Thread(new GetDataRunnable("serviceData", getValues(path))).start());
        });
      });
      cache.start();
    }
  }

  private void addApplicationConfig(String env) {
    String path = String.format(PATH_APPLICATION, env, BootStrapProperties.readApplication(environment));
    this.applicationData = parseData(path);
    try (KVCache cache = KVCache.newCache(kvClient, path, consulConfig.getConsulWatchSeconds())) {
      cache.addListener(newValues -> {
        Optional<Value> newValue = newValues.values().stream()
            .filter(value -> value.getKey().equals(path))
            .findAny();

        newValue.ifPresent(value -> {
          Optional<String> decodedValue = newValue.get().getValueAsString();
          decodedValue.ifPresent(v -> new Thread(new GetDataRunnable("applicationData", getValues(path))).start());
        });
      });
      cache.start();
    }
  }

  private void addEnvironmentConfig(String env) {
    String path = String.format(PATH_ENVIRONMENT, env);

    this.environmentData = parseData(path);
    try (KVCache cache = KVCache.newCache(kvClient, path, consulConfig.getConsulWatchSeconds())) {
      cache.addListener(newValues -> {
        Optional<Value> newValue = newValues.values().stream()
            .filter(value -> value.getKey().equals(path))
            .findAny();

        newValue.ifPresent(value -> {
          Optional<String> decodedValue = newValue.get().getValueAsString();
          decodedValue.ifPresent(v -> new Thread(new GetDataRunnable("environmentData", getValues(path))).start());
        });
      });
      cache.start();
    }
  }

  public Map<String, Object> parseData(String path) {
    try {
      return getValues(path);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    }
    return new HashMap<>();
  }

  private Map<String, Object> getValues(String path) {
    Map<String, Object> values = new HashMap<>();
    KeyValueClient keyValueClient = consulClient.keyValueClient();
    String decodedValue = keyValueClient.getValueAsString(path).orElseThrow();
    if (StringUtils.isBlank(decodedValue)) {
      return values;
    }
    return getValues(path, decodedValue);
  }

  private @NotNull Map<String, Object> getValues(String path, String decodedValue) {
    Map<String, Object> values = new HashMap<>();
    if (path.endsWith(".yaml") || path.endsWith(".yml")) {
      YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
      yamlFactory.setResources(new ByteArrayResource(decodedValue.getBytes(StandardCharsets.UTF_8)));
      values.putAll(toMap(yamlFactory.getObject()));
    } else if (path.endsWith(".properties")) {
      Properties properties = new Properties();
      try {
        properties.load(new StringReader(decodedValue));
      } catch (IOException e) {
        LOGGER.error(e.getMessage(), e);
      }
      values.putAll(toMap(properties));
    } else {
      values.put(path, decodedValue);
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
