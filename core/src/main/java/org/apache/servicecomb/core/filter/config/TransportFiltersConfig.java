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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

@Component
public class TransportFiltersConfig {
  public static final String FILTER_CHAINS_PREFIX = "servicecomb.filter-chains.";

  public static final String ROOT = FILTER_CHAINS_PREFIX + "transport-filters";

  private final Map<String, TransportFilterConfig> byName = new HashMap<>();

  private final Configuration config = (Configuration) DynamicPropertyFactory.getBackingConfigurationSource();

  public void load() {
    config.getKeys(ROOT).forEachRemaining(this::loadOneChain);
  }

  private void loadOneChain(String qualifiedKey) {
    String qualifiedName = qualifiedKey.substring(ROOT.length() + 1);
    int dotIdx = qualifiedName.indexOf('.');
    String name = qualifiedName.substring(0, dotIdx);
    String transport = qualifiedName.substring(dotIdx + 1);

    byName.computeIfAbsent(name, key -> new TransportFilterConfig())
        .setTransportFilters(transport, config.getList(qualifiedKey));
  }

  public TransportFilterConfig getConfig(String name) {
    return byName.get(name);
  }
}
