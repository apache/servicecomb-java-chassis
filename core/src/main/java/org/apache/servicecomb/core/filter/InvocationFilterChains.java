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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.servicecomb.core.filter.config.InvocationFilterChainsConfig;
import org.apache.servicecomb.core.filter.config.TransportChainsConfig;
import org.apache.servicecomb.core.filter.impl.TransportFilters;

public class InvocationFilterChains {
  private final Map<String, Filter> filters = new HashMap<>();

  private List<Object> resolvedFrameworkConfig;

  private List<Object> resolvedDefaultConfig;

  private final Map<String, List<Object>> resolvedMicroserviceConfig = new HashMap<>();

  private FilterNode defaultChain;

  private final Map<String, FilterNode> microserviceChains = new HashMap<>();

  public Collection<Filter> getFilters() {
    return filters.values();
  }

  public void addFilter(Filter filter) {
    filters.put(filter.getName(), filter);
  }

  public List<Object> getResolvedFrameworkConfig() {
    return resolvedFrameworkConfig;
  }

  public List<Object> getResolvedDefaultConfig() {
    return resolvedDefaultConfig;
  }

  public Map<String, List<Object>> getResolvedMicroserviceConfig() {
    return resolvedMicroserviceConfig;
  }

  public void resolve(Function<List<String>, List<Object>> resolver,
      InvocationFilterChainsConfig config) {
    resolvedFrameworkConfig = resolver.apply(config.getFrameworkChain());
    resolvedDefaultConfig = resolver.apply(config.getDefaultChain());

    defaultChain = createChain(resolvedDefaultConfig);
    for (Entry<String, List<String>> entry : config.getMicroserviceChains().entrySet()) {
      List<Object> resolveConfig = resolver.apply(entry.getValue());

      resolvedMicroserviceConfig.put(entry.getKey(), resolveConfig);
      microserviceChains.put(entry.getKey(), createChain(resolveConfig));
    }
  }

  private <T> FilterNode createChain(List<T> chain) {
    List<Filter> filters = createFilters(chain);
    return FilterNode.buildChain(filters);
  }

  private <T> List<Filter> createFilters(List<T> chain) {
    return chain.stream()
        .map(this::findFilter)
        .collect(Collectors.toList());
  }

  private Filter findFilter(Object filterConfig) {
    if (filterConfig instanceof TransportChainsConfig) {
      return createTransportFilter((TransportChainsConfig) filterConfig);
    }

    Filter filter = filters.get(filterConfig);
    if (filter == null) {
      throw new IllegalStateException("failed to find filter, name=" + filterConfig);
    }
    return filter;
  }

  private Filter createTransportFilter(TransportChainsConfig config) {
    TransportFilters transportFilters = new TransportFilters();
    for (Entry<String, List<String>> entry : config.getChainByTransport().entrySet()) {
      List<Filter> filters = createFilters(entry.getValue());
      transportFilters.getChainByTransport().put(entry.getKey(), FilterNode.buildChain(filters));
    }
    return transportFilters;
  }

  public FilterNode findChain(String microserviceName) {
    return microserviceChains.getOrDefault(microserviceName, defaultChain);
  }
}
