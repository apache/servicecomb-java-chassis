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
package org.apache.servicecomb.core.filter;

import static org.apache.servicecomb.foundation.common.utils.StringBuilderUtils.appendLine;
import static org.apache.servicecomb.foundation.common.utils.StringBuilderUtils.deleteLast;
import static org.apache.servicecomb.swagger.invocation.InvocationType.CONSUMER;
import static org.apache.servicecomb.swagger.invocation.InvocationType.PRODUCER;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.filter.config.FilterChainsConfig;
import org.apache.servicecomb.core.filter.config.TransportFiltersConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FilterChainsManager {
  private TransportFiltersConfig transportFiltersConfig;

  private FilterManager filterManager;

  private FilterChainsConfig consumerChainsConfig;

  private FilterChainsConfig producerChainsConfig;

  private boolean enabled;

  @Autowired
  public FilterChainsManager setTransportFiltersConfig(TransportFiltersConfig transportFiltersConfig) {
    this.transportFiltersConfig = transportFiltersConfig;
    return this;
  }

  @Value("${servicecomb.filter-chains.enabled:false}")
  public FilterChainsManager setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public FilterManager getFilterManager() {
    return filterManager;
  }

  @Autowired
  public FilterChainsManager setFilterManager(FilterManager filterManager) {
    this.filterManager = filterManager;
    return this;
  }

  public FilterChainsManager init(SCBEngine engine) {
    transportFiltersConfig.load();
    filterManager.init(engine);

    consumerChainsConfig = new FilterChainsConfig(transportFiltersConfig, CONSUMER);
    producerChainsConfig = new FilterChainsConfig(transportFiltersConfig, PRODUCER);

    return this;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public FilterChainsManager addProviders(FilterProvider... providers) {
    return addProviders(Arrays.asList(providers));
  }

  public FilterChainsManager addProviders(Collection<FilterProvider> providers) {
    filterManager.addProviders(providers);
    return this;
  }

  public FilterNode createConsumerFilterChain(String microservice) {
    return createFilterNode(consumerChainsConfig, microservice);
  }

  public FilterNode createProducerFilterChain(String microservice) {
    return createFilterNode(producerChainsConfig, microservice);
  }

  public List<Filter> createConsumerFilters(String microservice) {
    return createFilters(consumerChainsConfig, microservice);
  }

  public List<Filter> createProducerFilters(String microservice) {
    return createFilters(producerChainsConfig, microservice);
  }

  public String collectResolvedChains() {
    StringBuilder sb = new StringBuilder();

    appendLine(sb, "consumer: ");
    appendLine(sb, "  filters: %s", filterManager.getConsumerFilters());
    collectChainsByInvocationType(sb, consumerChainsConfig);

    appendLine(sb, "producer: ");
    appendLine(sb, "  filters: %s", filterManager.getProducerFilters());
    collectChainsByInvocationType(sb, producerChainsConfig);

    return deleteLast(sb, 1).toString();
  }

  private void collectChainsByInvocationType(StringBuilder sb, FilterChainsConfig chainsConfig) {
    appendLine(sb, "  chains:");
    appendLine(sb, "    default: %s", chainsConfig.getDefaultChain());
    for (Entry<String, List<Object>> entry : chainsConfig.getMicroserviceChains().entrySet()) {
      appendLine(sb, "    %s: %s", entry.getKey(), entry.getValue());
    }
  }

  private FilterNode createFilterNode(FilterChainsConfig chainsConfig, String microservice) {
    if (!enabled) {
      return FilterNode.EMPTY;
    }

    List<Filter> filters = createFilters(chainsConfig, microservice);
    return FilterNode.buildChain(filters);
  }

  private List<Filter> createFilters(FilterChainsConfig chainsConfig, String microservice) {
    if (!enabled) {
      return Collections.emptyList();
    }

    List<Object> chain = chainsConfig.findChain(microservice);
    return filterManager.createFilters(chain);
  }
}
