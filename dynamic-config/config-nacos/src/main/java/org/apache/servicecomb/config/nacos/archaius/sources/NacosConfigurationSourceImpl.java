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

package org.apache.servicecomb.config.nacos.archaius.sources;

import static com.netflix.config.WatchedUpdateResult.createIncremental;
import static org.apache.servicecomb.config.nacos.client.ConfigurationAction.CREATE;
import static org.apache.servicecomb.config.nacos.client.ConfigurationAction.DELETE;
import static org.apache.servicecomb.config.nacos.client.ConfigurationAction.SET;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.ConfigMapping;
import org.apache.servicecomb.config.nacos.client.ConfigurationAction;
import org.apache.servicecomb.config.nacos.client.NacosClient;
import org.apache.servicecomb.config.nacos.client.NacosConfig;
import org.apache.servicecomb.config.spi.ConfigCenterConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.netflix.config.WatchedUpdateListener;
import com.netflix.config.WatchedUpdateResult;

public class NacosConfigurationSourceImpl implements ConfigCenterConfigurationSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCenterConfigurationSource.class);

  private final Map<String, Object> valueCache = new ConcurrentHashMap<>();

  private List<WatchedUpdateListener> listeners = new CopyOnWriteArrayList<>();

  public NacosConfigurationSourceImpl() {
  }

  private final UpdateHandler updateHandler = new UpdateHandler();

  @Override
  public int getOrder() {
    return ORDER_BASE * 4;
  }

  @Override
  public boolean isValidSource(Configuration localConfiguration) {
    if (localConfiguration.getProperty(NacosConfig.SERVER_ADDR) == null) {
      LOGGER.warn("Nacos configuration source is not configured!");
      return false;
    }
    return true;
  }

  @Override
  public void init(Configuration localConfiguration) {
    NacosConfig.setConcurrentCompositeConfiguration(localConfiguration);
    init();
  }

  private void init() {
    NacosClient nacosClient = new NacosClient(updateHandler);
    nacosClient.refreshNacosConfig();
  }

  @Override
  public void addUpdateListener(WatchedUpdateListener watchedUpdateListener) {
    listeners.add(watchedUpdateListener);
  }

  @Override
  public void removeUpdateListener(WatchedUpdateListener watchedUpdateListener) {
    listeners.remove(watchedUpdateListener);
  }

  private void updateConfiguration(WatchedUpdateResult result) {
    for (WatchedUpdateListener l : listeners) {
      try {
        l.updateConfiguration(result);
      } catch (Throwable ex) {
        LOGGER.error("Error in invoking WatchedUpdateListener", ex);
      }
    }
  }

  @Override
  public Map<String, Object> getCurrentData() throws Exception {
    return valueCache;
  }

  public List<WatchedUpdateListener> getCurrentListeners() {
    return listeners;
  }

  public class UpdateHandler {
    public void handle(ConfigurationAction action, Map<String, Object> config) {
      if (config == null || config.isEmpty()) {
        return;
      }
      Map<String, Object> configuration = ConfigMapping.getConvertedMap(config);
      if (CREATE.equals(action)) {
        valueCache.putAll(configuration);

        updateConfiguration(createIncremental(ImmutableMap.copyOf(configuration),
            null,
            null));
      } else if (SET.equals(action)) {
        valueCache.putAll(configuration);

        updateConfiguration(createIncremental(null,
            ImmutableMap.copyOf(configuration),
            null));
      } else if (DELETE.equals(action)) {
        configuration.keySet().forEach(valueCache::remove);
        updateConfiguration(createIncremental(null,
            null,
            ImmutableMap.copyOf(configuration)));
      } else {
        LOGGER.error("action: {} is invalid.", action.name());
        return;
      }
      LOGGER.warn("Config value cache changed: action:{}; item:{}", action.name(), configuration.keySet());
    }
  }
}
