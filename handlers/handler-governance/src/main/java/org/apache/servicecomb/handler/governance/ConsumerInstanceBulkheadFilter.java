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

package org.apache.servicecomb.handler.governance;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.governance.MatchType;
import org.apache.servicecomb.governance.handler.InstanceBulkheadHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCompletionStage;
import jakarta.ws.rs.core.Response.Status;

public class ConsumerInstanceBulkheadFilter implements ConsumerFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerInstanceBulkheadFilter.class);

  private final InstanceBulkheadHandler instanceBulkheadHandler;

  @Autowired
  public ConsumerInstanceBulkheadFilter(InstanceBulkheadHandler instanceBulkheadHandler) {
    this.instanceBulkheadHandler = instanceBulkheadHandler;
  }

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.CONSUMER_LOAD_BALANCE_ORDER + 1060;
  }

  @Override
  public String getName() {
    return "instance-bulkhead";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    if (invocation.getEndpoint() == null) {
      return CompletableFuture.failedFuture(new InvocationException(Status.INTERNAL_SERVER_ERROR,
          new CommonExceptionData("instance bulkhead should work after load balancer.")));
    }
    Supplier<CompletionStage<Response>> next = createBusinessCompletionStageSupplier(invocation, nextNode);
    DecorateCompletionStage<Response> dcs = Decorators.ofCompletionStage(next);
    GovernanceRequestExtractor request = MatchType.createGovHttpRequest(invocation);

    addBulkhead(dcs, request);

    CompletableFuture<Response> future = new CompletableFuture<>();

    dcs.get().whenComplete((r, e) -> {
      if (e == null) {
        future.complete(r);
        return;
      }

      if (e instanceof BulkheadFullException) {
        // return 503 so that consumer can retry
        future.completeExceptionally(new InvocationException(503, "bulkhead is full and does not permit further calls.",
            new CommonExceptionData("bulkhead is full and does not permit further calls.")));
        LOGGER.warn("bulkhead is full and does not permit further calls by policy : {}", e.getMessage());
      } else {
        future.completeExceptionally(e);
      }
    });

    return future;
  }

  private void addBulkhead(DecorateCompletionStage<Response> dcs, GovernanceRequestExtractor request) {
    Bulkhead bulkhead = instanceBulkheadHandler.getActuator(request);
    if (bulkhead != null) {
      dcs.withBulkhead(bulkhead);
    }
  }

  private Supplier<CompletionStage<Response>> createBusinessCompletionStageSupplier(Invocation invocation,
      FilterNode nextNode) {
    return () -> nextNode.onFilter(invocation);
  }
}
