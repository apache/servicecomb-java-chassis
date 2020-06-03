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
package org.apache.servicecomb.core.filter.config;

import static org.apache.servicecomb.core.filter.config.TransportFiltersConfig.FILTER_CHAINS_PREFIX;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.swagger.invocation.InvocationType;

import com.netflix.config.DynamicPropertyFactory;

public class FilterChainsConfig {
  private final List<Object> defaultChain;

  private final Map<String, List<Object>> microserviceChains = new HashMap<>();

  private final TransportFiltersConfig transportFiltersConfig;

  public FilterChainsConfig(TransportFiltersConfig transportFiltersConfig, InvocationType type) {
    this.transportFiltersConfig = transportFiltersConfig;

    Configuration config = (Configuration) DynamicPropertyFactory.getBackingConfigurationSource();
    String root = FILTER_CHAINS_PREFIX + type.name().toLowerCase(Locale.US);
    defaultChain = resolve(ConfigUtil.getStringList(config, root + ".default"));
    loadMicroserviceChains(config, root + ".policies");
  }

  public List<Object> getDefaultChain() {
    return defaultChain;
  }

  public Map<String, List<Object>> getMicroserviceChains() {
    return microserviceChains;
  }

  public List<Object> findChain(String microservice) {
    return microserviceChains.getOrDefault(microservice, defaultChain);
  }

  private void loadMicroserviceChains(Configuration config, String policiesRoot) {
    config.getKeys(policiesRoot).forEachRemaining(qualifiedKey -> {
      String microserviceName = qualifiedKey.substring(policiesRoot.length() + 1);
      List<String> chain = ConfigUtil.getStringList(config, qualifiedKey);

      microserviceChains.put(microserviceName, resolve(chain));
    });
  }

  private List<Object> resolve(List<String> rawChain) {
    return rawChain.stream()
        .map(value -> {
          TransportFilterConfig config = transportFiltersConfig.getConfig(value);
          return config == null ? value : config;
        })
        .collect(Collectors.toList());
  }
}
