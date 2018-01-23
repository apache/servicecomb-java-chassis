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

package org.apache.servicecomb.config.archaius.sources;

import static com.netflix.config.WatchedUpdateResult.createIncremental;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.ConfigMapping;
import org.apache.servicecomb.config.client.ConfigCenterClient;
import org.apache.servicecomb.config.client.ConfigCenterConfig;
import org.apache.servicecomb.config.spi.ConfigCenterConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.WatchedUpdateListener;
import com.netflix.config.WatchedUpdateResult;

/**
 * Created by on 2017/1/9.
 */
public class ConfigCenterConfigurationSourceImpl implements ConfigCenterConfigurationSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCenterConfigurationSourceImpl.class);

  private final Map<String, Object> valueCache = Maps.newConcurrentMap();

  private final List<WatchedUpdateListener> listeners = new CopyOnWriteArrayList<>();

  private UpdateHandler updateHandler = new UpdateHandler();

  public ConfigCenterConfigurationSourceImpl() {
  }

  private void init() {
    ConfigCenterClient configCenterClient = new ConfigCenterClient(updateHandler);
    configCenterClient.connectServer();
  }

  @Override
  public void init(Configuration localConfiguration) {
    ConfigCenterConfig.setConcurrentCompositeConfiguration((ConcurrentCompositeConfiguration) localConfiguration);
    init();
  }

  public void addUpdateListener(WatchedUpdateListener watchedUpdateListener) {
    if (watchedUpdateListener != null) {
      listeners.add(watchedUpdateListener);
    }
  }

  public void removeUpdateListener(WatchedUpdateListener watchedUpdateListener) {
    if (watchedUpdateListener != null) {
      listeners.remove(watchedUpdateListener);
    }
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

  public Map<String, Object> getCurrentData()
      throws Exception {
    return valueCache;
  }

  public List<WatchedUpdateListener> getCurrentListeners() {
    return listeners;
  }

  public class UpdateHandler {

    public void handle(String action, Map<String, Object> parseConfigs) {
      if (parseConfigs == null || parseConfigs.isEmpty()) {
        return;
      }
      Map<String, Object> configuration = ConfigMapping.getConvertedMap(parseConfigs);
      if ("create".equals(action)) {
        valueCache.putAll(configuration);
        updateConfiguration(createIncremental(ImmutableMap.<String, Object>copyOf(configuration),
            null,
            null));
      } else if ("set".equals(action)) {
        valueCache.putAll(configuration);
        updateConfiguration(createIncremental(null, ImmutableMap.<String, Object>copyOf(configuration),
            null));
      } else if ("delete".equals(action)) {
        for (String itemKey : configuration.keySet()) {
          valueCache.remove(itemKey);
        }
        updateConfiguration(createIncremental(null,
            null,
            ImmutableMap.<String, Object>copyOf(configuration)));
      } else {
        LOGGER.error("action: {} is invalid.", action);
        return;
      }
      LOGGER.warn("Config value cache changed: action:{}; item:{}", action, configuration.keySet());
    }
  }
}
