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

package org.apache.servicecomb.config.kie.archaius.sources;

import static com.netflix.config.WatchedUpdateResult.createIncremental;

import com.google.common.collect.ImmutableMap;
import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.WatchedUpdateListener;
import com.netflix.config.WatchedUpdateResult;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.ConfigMapping;
import org.apache.servicecomb.config.kie.client.KieClient;
import org.apache.servicecomb.config.kie.client.KieConfig;
import org.apache.servicecomb.config.spi.ConfigCenterConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieConfigurationSourceImpl implements ConfigCenterConfigurationSource {

  private static final Logger LOGGER = LoggerFactory.getLogger(KieConfigurationSourceImpl.class);

  private static final String KIE_CONFIG_URL_KEY = "servicecomb.kie.serverUri";

  private final Map<String, Object> valueCache = new ConcurrentHashMap<>();

  private List<WatchedUpdateListener> listeners = new CopyOnWriteArrayList<>();

  private UpdateHandler updateHandler = new UpdateHandler();

  private KieClient kieClient;

  @Override
  public boolean isValidSource(Configuration localConfiguration) {
    if (localConfiguration.getProperty(KIE_CONFIG_URL_KEY) == null) {
      LOGGER.warn("Kie configuration source is not configured!");
      return false;
    }
    return true;
  }

  @Override
  public void init(Configuration localConfiguration) {
    KieConfig.setFinalConfig((ConcurrentCompositeConfiguration) localConfiguration);
    kieClient = new KieClient(updateHandler);
    kieClient.refreshKieConfig();
  }

  @Override
  public void destroy() {
    if (kieClient == null) {
      return;
    }
    kieClient.destroy();
  }

  @Override
  public void addUpdateListener(WatchedUpdateListener watchedUpdateListener) {
    listeners.add(watchedUpdateListener);
  }

  @Override
  public void removeUpdateListener(WatchedUpdateListener watchedUpdateListener) {
    listeners.remove(watchedUpdateListener);
  }

  @Override
  public Map<String, Object> getCurrentData() throws Exception {
    return valueCache;
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
        updateConfiguration(
            createIncremental(null, ImmutableMap.<String, Object>copyOf(configuration),
                null));
      } else if ("delete".equals(action)) {
        configuration.keySet().forEach(valueCache::remove);
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
