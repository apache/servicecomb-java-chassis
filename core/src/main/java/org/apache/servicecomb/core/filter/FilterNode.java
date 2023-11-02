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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.apache.servicecomb.swagger.invocation.Response;

public class FilterNode {
  public static final FilterNode EMPTY = new FilterNode(null) {
    @Override
    public CompletableFuture<Response> onFilter(Invocation invocation) {
      return CompletableFuture.completedFuture(Response.ok(null));
    }
  };

  public static FilterNode buildChain(Filter... filters) {
    return buildChain(Arrays.asList(filters));
  }

  public static FilterNode buildChain(List<Filter> filters) {
    List<FilterNode> filterNodes = filters.stream()
        .map(FilterNode::new).toList();

    for (int idx = 0; idx < filterNodes.size() - 1; idx++) {
      FilterNode currentNode = filterNodes.get(idx);
      FilterNode nextNode = filterNodes.get(idx + 1);
      currentNode.setNextNode(nextNode);
    }

    return filterNodes.get(0);
  }

  private final Filter filter;

  private FilterNode nextNode;

  public FilterNode(Filter filter) {
    this.filter = filter;
  }

  private void setNextNode(FilterNode nextNode) {
    this.nextNode = nextNode;
  }

  public CompletableFuture<Response> onFilter(Invocation invocation) {
    // When transport name is empty, maybe edge and transport filters need to be executed.
    // And we can't set Endpoint before load balance in edge.
    if (invocation.getTransportName() != null && !filter.enabledForTransport(invocation.getTransportName())) {
      return nextNode.onFilter(invocation);
    }

    return AsyncUtils.tryCatchSupplierFuture(() -> filter.onFilter(invocation, nextNode))
        .thenApply(this::rethrowExceptionInResponse);
  }

  private Response rethrowExceptionInResponse(Response response) {
    if (response.isFailed() && response.getResult() instanceof Throwable) {
      throw AsyncUtils.rethrow(response.getResult());
    }

    return response;
  }
}
