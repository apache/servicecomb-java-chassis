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

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

public class FilterChainsManager {
  private InvocationFilterChains consumerChains;

  private InvocationFilterChains providerChains;

  private InvocationFilterChains edgeChains;

  @Autowired
  public FilterChainsManager setEdgeFilters(List<EdgeFilter> filters) {
    edgeChains = new InvocationFilterChains(filters);
    return this;
  }

  @Autowired
  public FilterChainsManager setConsumerFilters(List<ConsumerFilter> filters) {
    consumerChains = new InvocationFilterChains(filters);
    return this;
  }

  @Autowired
  public FilterChainsManager setProviderFilters(List<ProviderFilter> filters) {
    providerChains = new InvocationFilterChains(filters);
    return this;
  }

  public FilterChainsManager init() {
    return this;
  }

  public FilterNode findConsumerChain(String application, String serviceName) {
    return consumerChains.findChain(application, serviceName);
  }

  public FilterNode findProducerChain(String application, String serviceName) {
    return providerChains.findChain(application, serviceName);
  }

  public FilterNode findEdgeChain(String application, String serviceName) {
    return edgeChains.findChain(application, serviceName);
  }

  public String collectResolvedChains() {
    StringBuilder sb = new StringBuilder();

    appendLine(sb, "consumer: ");
    appendLine(sb, "  filters: %s", collectFilterNames(consumerChains));

    appendLine(sb, "producer: ");
    appendLine(sb, "  filters: %s", collectFilterNames(providerChains));

    appendLine(sb, "edge: ");
    appendLine(sb, "  filters: %s", collectFilterNames(edgeChains));

    return deleteLast(sb, 1).toString();
  }

  private List<String> collectFilterNames(InvocationFilterChains chains) {
    return chains.getFilters().stream()
        .map(filter -> filter.getName() + "(" + filter.getOrder() + ")")
        .collect(Collectors.toList());
  }
}
