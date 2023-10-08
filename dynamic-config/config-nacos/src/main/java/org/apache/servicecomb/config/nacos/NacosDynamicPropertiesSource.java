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
package org.apache.servicecomb.config.nacos;

import static org.apache.servicecomb.config.nacos.ConfigurationAction.CREATE;
import static org.apache.servicecomb.config.nacos.ConfigurationAction.DELETE;
import static org.apache.servicecomb.config.nacos.ConfigurationAction.SET;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.servicecomb.config.ConfigMapping;
import org.apache.servicecomb.config.DynamicPropertiesSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.config.WatchedUpdateListener;

public class NacosDynamicPropertiesSource implements DynamicPropertiesSource<Map<String, Object>> {
  public static final String SOURCE_NAME = "kie";

  private static final Logger LOGGER = LoggerFactory.getLogger(NacosDynamicPropertiesSource.class);

  private final Map<String, Object> valueCache = new ConcurrentHashMap<>();

  private final List<WatchedUpdateListener> listeners = new CopyOnWriteArrayList<>();

  public NacosDynamicPropertiesSource() {
  }

  private final UpdateHandler updateHandler = new UpdateHandler();

  @VisibleForTesting
  UpdateHandler getUpdateHandler() {
    return updateHandler;
  }


  private void init(Environment environment) {
    NacosClient nacosClient = new NacosClient(updateHandler);
    nacosClient.refreshNacosConfig();
  }

  public class UpdateHandler {
    public void handle(ConfigurationAction action, Map<String, Object> config) {
      if (config == null || config.isEmpty()) {
        return;
      }
      Map<String, Object> configuration = ConfigMapping.getConvertedMap(config);
      if (CREATE.equals(action)) {
        valueCache.putAll(configuration);
      } else if (SET.equals(action)) {
        valueCache.putAll(configuration);
      } else if (DELETE.equals(action)) {
        configuration.keySet().forEach(valueCache::remove);
      } else {
        LOGGER.error("action: {} is invalid.", action.name());
        return;
      }
      LOGGER.warn("Config value cache changed: action:{}; item:{}", action.name(), configuration.keySet());
    }
  }

  @Override
  public EnumerablePropertySource<Map<String, Object>> create(Environment environment) {
    init(environment);
    return new MapPropertySource(SOURCE_NAME, valueCache);
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
