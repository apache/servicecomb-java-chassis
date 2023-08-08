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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.servicecomb.swagger.invocation.InvocationType;

public class InvocationFilterChains {
  private final Map<String, Filter> filters = new HashMap<>();

  private final Map<String, FilterNode> microserviceChains = new HashMap<>();

  private final InvocationType invocationType;

  public InvocationFilterChains(InvocationType invocationType) {
    this.invocationType = invocationType;
  }

  public Collection<Filter> getFilters() {
    return filters.values();
  }

  public void addFilter(Filter filter) {
    filters.put(filter.getName(), filter);
  }

  public FilterNode findChain(String application, String serviceName) {
    FilterNode filterNode = microserviceChains.get(serviceName);
    if (filterNode == null) {
      List<Filter> serviceFilters = filters.entrySet().stream()
          .filter(e -> e.getValue().isEnabledForMicroservice(application, serviceName))
          .map(e -> e.getValue())
          .collect(Collectors.toList());
      serviceFilters.sort(Comparator.comparingInt(a -> a.getOrder(invocationType, application, serviceName)));
      filterNode = FilterNode.buildChain(serviceFilters);
      microserviceChains.put(serviceName, filterNode);
    }
    return filterNode;
  }
}
