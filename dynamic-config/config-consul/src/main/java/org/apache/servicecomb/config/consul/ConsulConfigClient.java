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

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.kv.model.GetValue;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.TaskScheduler;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;

import static org.apache.servicecomb.config.consul.ConsulConfig.PATH_APPLICATION;
import static org.apache.servicecomb.config.consul.ConsulConfig.PATH_ENVIRONMENT;
import static org.apache.servicecomb.config.consul.ConsulConfig.PATH_SERVICE;
import static org.apache.servicecomb.config.consul.ConsulConfig.PATH_TAG;
import static org.apache.servicecomb.config.consul.ConsulConfig.PATH_VERSION;

public class ConsulConfigClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsulConfigClient.class);

  public class GetDataRunnable implements Runnable {

    private Map<String, Object> dataMap;

    private ConsulConfigClient consulConfigClient;

    private String path;

    public GetDataRunnable(Map<String, Object> dataMap, ConsulConfigClient consulConfigClient, String path) {
      this.dataMap = dataMap;
      this.consulConfigClient = consulConfigClient;
      this.path = path;
    }

    @Override
    public void run() {
      try {
        dataMap.clear();
        dataMap.putAll(consulConfigClient.parseData(path));
        refreshConfigItems();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Resource
  private TaskScheduler taskScheduler;

  private ScheduledFuture<?> watchFuture;

  @Resource
  private UpdateHandler updateHandler;

  private ConsulConfig consulConfig;

  @Resource
  private Environment environment;

  private final Object lock = new Object();

  @Resource
  private ConsulClient consulClient;

  @Resource
  private ConsulConfigProperties consulConfigProperties;

  private Map<String, Object> environmentData = new HashMap<>();

  private Map<String, Object> applicationData = new HashMap<>();

  private Map<String, Object> serviceData = new HashMap<>();

  private Map<String, Object> versionData = new HashMap<>();

  private Map<String, Object> tagData = new HashMap<>();

  private Map<String, Object> allLast = new HashMap<>();

  public ConsulConfigClient(UpdateHandler updateHandler, Environment environment, ConsulConfigProperties consulConfigProperties, ConsulClient consulClient, TaskScheduler taskScheduler) {
    this.updateHandler = updateHandler;
    this.consulConfig = new ConsulConfig(environment);
    this.environment = environment;
    this.consulConfigProperties = consulConfigProperties;
    this.consulClient = consulClient;
    this.taskScheduler = taskScheduler;
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

  public void destroy() {
    LOGGER.info("consul client destroy");
    if (watchFuture != null) {
      watchFuture.cancel(true);
    }
  }

  private void addTagConfig(String env) {
    if (StringUtils.isEmpty(consulConfig.getInstanceTag())) {
      return;
    }
    String path = String.format(PATH_TAG, env,
        BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment),
        BootStrapProperties.readServiceVersion(environment),
        consulConfig.getInstanceTag());

    runTask(tagData, path);
    this.tagData = parseData(path);
  }

  private void addVersionConfig(String env) {
    String path = String.format(PATH_VERSION, env,
        BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment),
        BootStrapProperties.readServiceVersion(environment));

    runTask(versionData, path);
    this.versionData = parseData(path);
  }

  private void addServiceConfig(String env) {
    String path = String.format(PATH_SERVICE, env,
        BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment));

    runTask(serviceData, path);
    this.serviceData = parseData(path);
  }

  private void addApplicationConfig(String env) {
    String path = String.format(PATH_APPLICATION, env, BootStrapProperties.readApplication(environment));
    runTask(applicationData, path);
    this.applicationData = parseData(path);
  }

  private void addEnvironmentConfig(String env) {
    String path = String.format(PATH_ENVIRONMENT, env);

    runTask(environmentData, path);
    this.environmentData = parseData(path);
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
    GetValue getValue;
    if (StringUtils.isNotBlank(consulConfigProperties.getAclToken())) {
      getValue = consulClient.getKVValue(path, consulConfigProperties.getAclToken()).getValue();
    } else {
      getValue = consulClient.getKVValue(path).getValue();
    }
    if (getValue == null || StringUtils.isEmpty(getValue.getValue())) {
      return values;
    }
    String key = getValue.getKey();
    String decodedValue = getValue.getDecodedValue(StandardCharsets.UTF_8);
    if (key.endsWith(".yaml") || key.endsWith(".yml")) {
      YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
      yamlFactory.setResources(new ByteArrayResource(decodedValue.getBytes(StandardCharsets.UTF_8)));
      values.putAll(toMap(yamlFactory.getObject()));
    } else if (key.endsWith(".properties")) {
      Properties properties = new Properties();
      try {
        properties.load(new StringReader(decodedValue));
      } catch (IOException e) {
        LOGGER.error("load error");
      }
      values.putAll(toMap(properties));
    } else {
      values.put(key, decodedValue);
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

  private void runTask(Map<String, Object> serviceData, String path) {
    watchFuture = taskScheduler.scheduleWithFixedDelay(new GetDataRunnable(serviceData, this, path),
        Duration.ofMillis(consulConfigProperties.getDelayTime()));
  }
}
