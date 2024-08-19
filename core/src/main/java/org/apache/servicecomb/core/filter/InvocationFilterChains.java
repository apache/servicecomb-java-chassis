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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

public class InvocationFilterChains {

  private final List<? extends Filter> filters;

  private final Map<String, Map<String, FilterNode>> microserviceChains = new ConcurrentHashMapEx<>();

  public InvocationFilterChains(List<? extends Filter> filters) {
    this.filters = filters;
  }

  public List<? extends Filter> getFilters() {
    return filters;
  }

  public FilterNode findChain(String application, String serviceName) {
    return microserviceChains.computeIfAbsent(application, key -> new ConcurrentHashMapEx<>())
        .computeIfAbsent(serviceName, (serviceNameInner) -> {
          List<Filter> serviceFilters = filters.stream()
              .filter(e -> e.enabledForMicroservice(application, serviceName))
              .sorted(Comparator.comparingInt(a -> a.getOrder(application, serviceName)))
              .collect(Collectors.toList());
          return FilterNode.buildChain(serviceFilters);
        });
  }
}
