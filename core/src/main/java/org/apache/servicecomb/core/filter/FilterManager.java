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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.filter.config.TransportFilterConfig;
import org.apache.servicecomb.core.filter.impl.TransportFilters;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FilterManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(FilterManager.class);

  interface Factory {
    Filter create();
  }

  private SCBEngine engine;

  private final List<FilterProvider> providers = new ArrayList<>(
      SPIServiceUtils.getOrLoadSortedService(FilterProvider.class));

  private final Map<String, Factory> factoryMap = new HashMap<>();

  private final List<String> consumerFilters = new ArrayList<>();

  private final List<String> producerFilters = new ArrayList<>();

  @Autowired(required = false)
  public void addProviders(Collection<FilterProvider> providers) {
    this.providers.addAll(providers);
  }

  public List<String> getConsumerFilters() {
    return consumerFilters;
  }

  public List<String> getProducerFilters() {
    return producerFilters;
  }

  public void init(SCBEngine engine) {
    this.engine = engine;
    List<Class<? extends Filter>> filterClasses = providers.stream()
        .flatMap(provider -> provider.getFilters().stream())
        .collect(Collectors.toList());

    for (Class<? extends Filter> filterClass : filterClasses) {
      FilterMeta meta = filterClass.getAnnotation(FilterMeta.class);
      Factory factory = buildFactory(filterClass, meta);

      if (factoryMap.put(meta.name(), factory) != null) {
        throw new IllegalStateException(
            String.format("duplicated filter, name=%s, class=%s", meta.name(), filterClass.getName()));
      }

      if (Arrays.binarySearch(meta.invocationType(), InvocationType.CONSUMER) >= 0) {
        consumerFilters.add(meta.name());
      }
      if (Arrays.binarySearch(meta.invocationType(), InvocationType.PRODUCER) >= 0) {
        producerFilters.add(meta.name());
      }
    }
  }

  public List<Filter> createFilters(List<Object> chain) {
    return chain.stream()
        .map(filterConfig -> {
          Filter filter = createFilter(filterConfig);
          filter.init(engine);
          return filter;
        })
        .collect(Collectors.toList());
  }

  private Filter createFilter(Object filterConfig) {
    if (filterConfig instanceof String) {
      return createFilterByName((String) filterConfig);
    }

    if (filterConfig instanceof TransportFilterConfig) {
      return createTransportFilter((TransportFilterConfig) filterConfig);
    }

    throw new IllegalStateException("not support create filter by " + filterConfig);
  }

  private Filter createTransportFilter(TransportFilterConfig config) {
    TransportFilters transportFilters = new TransportFilters();
    for (Entry<String, List<Object>> entry : config.getFiltersByTransport().entrySet()) {
      List<Filter> filters = createFilters(entry.getValue());
      transportFilters.getChainByTransport().put(entry.getKey(), FilterNode.buildChain(filters));
    }
    return transportFilters;
  }

  private Filter createFilterByName(String filterName) {
    Factory factory = factoryMap.get(filterName);
    if (factory != null) {
      return factory.create();
    }

    throw new IllegalStateException("filter not exist, name=" + filterName);
  }

  private Factory buildFactory(Class<? extends Filter> filterClass, FilterMeta meta) {
    if (meta.shareable()) {
      Filter filter = createFilter(filterClass);
      return () -> filter;
    }

    return () -> createFilter(filterClass);
  }

  private Filter createFilter(Class<? extends Filter> filterClass) {
    try {
      Filter filter = filterClass.newInstance();
      injectSpringBean(filter);
      filter.init(engine);
      return filter;
    } catch (Exception e) {
      throw new IllegalStateException("failed to create filter.", e);
    }
  }

  private void injectSpringBean(Filter filter) {
    if (engine == null || engine.getApplicationContext() == null) {
      LOGGER.error("engine or application context is null, only allowed when UT.");
      return;
    }

    engine.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(filter);
  }
}
