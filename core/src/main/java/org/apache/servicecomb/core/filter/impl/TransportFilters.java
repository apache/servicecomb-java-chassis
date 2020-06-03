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
package org.apache.servicecomb.core.filter.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterMeta;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.swagger.invocation.Response;

/**
 * Internal use only, will not publish by {@link DefaultFilterProvider}
 */
@FilterMeta(name = "transport-filters")
public class TransportFilters implements Filter {
  private Map<String, FilterNode> chainByTransport = new HashMap<>();

  public Map<String, FilterNode> getChainByTransport() {
    return chainByTransport;
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    FilterNode filterNode = chainByTransport.get(invocation.getTransport().getName());
    if (filterNode == null) {
      return nextNode.onFilter(invocation);
    }
    
    return filterNode.onFilter(invocation);
  }
}
