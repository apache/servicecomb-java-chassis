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
import static org.apache.servicecomb.config.client.ConfigurationAction.CREATE;
import static org.apache.servicecomb.config.client.ConfigurationAction.DELETE;
import static org.apache.servicecomb.config.client.ConfigurationAction.SET;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.validation.constraints.NotNull;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.ConfigMapping;
import org.apache.servicecomb.config.client.ApolloClient;
import org.apache.servicecomb.config.client.ApolloConfig;
import org.apache.servicecomb.config.client.ConfigurationAction;
import org.apache.servicecomb.config.spi.ConfigCenterConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.netflix.config.WatchedUpdateListener;
import com.netflix.config.WatchedUpdateResult;

public class ApolloConfigurationSourceImpl implements ConfigCenterConfigurationSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCenterConfigurationSource.class);

  private final Map<String, Object> valueCache = new ConcurrentHashMap<>();

  private List<WatchedUpdateListener> listeners = new CopyOnWriteArrayList<>();

  public ApolloConfigurationSourceImpl() {
  }

  private final UpdateHandler updateHandler = new UpdateHandler();

  @Override
  public void init(Configuration localConfiguration) {
    ApolloConfig.setConcurrentCompositeConfiguration(localConfiguration);
    init();
  }

  private void init() {
    ApolloClient apolloClient = new ApolloClient(updateHandler);
    apolloClient.refreshApolloConfig();
  }

  @Override
  public void addUpdateListener(@NotNull WatchedUpdateListener watchedUpdateListener) {
    listeners.add(watchedUpdateListener);
  }

  @Override
  public void removeUpdateListener(@NotNull WatchedUpdateListener watchedUpdateListener) {
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
        for (String itemKey : configuration.keySet()) {
          valueCache.remove(itemKey);
        }
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
