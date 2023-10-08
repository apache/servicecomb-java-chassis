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
package org.apache.servicecomb.config.apollo;

import static org.apache.servicecomb.config.apollo.ConfigurationAction.CREATE;
import static org.apache.servicecomb.config.apollo.ConfigurationAction.DELETE;
import static org.apache.servicecomb.config.apollo.ConfigurationAction.SET;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.config.ConfigMapping;
import org.apache.servicecomb.config.DynamicPropertiesSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;

public class ApolloDynamicPropertiesSource implements DynamicPropertiesSource<Map<String, Object>> {
  public static final String SOURCE_NAME = "apollo";

  private static final Logger LOGGER = LoggerFactory.getLogger(ApolloDynamicPropertiesSource.class);

  private final Map<String, Object> valueCache = new ConcurrentHashMap<>();

  private final ApolloDynamicPropertiesSource.UpdateHandler updateHandler =
      new ApolloDynamicPropertiesSource.UpdateHandler();

  private ApolloConfig apolloConfig;

  @Autowired
  public void setApolloConfig(ApolloConfig apolloConfig) {
    this.apolloConfig = apolloConfig;
  }

  @Override
  public int getOrder() {
    return 300;
  }

  private void init() {
    ApolloClient apolloClient = new ApolloClient(updateHandler, apolloConfig);
    apolloClient.refreshApolloConfig();
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
    init();
    return new MapPropertySource(SOURCE_NAME, valueCache);
  }
}
