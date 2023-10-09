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
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.filter.ProviderFilter;
import org.apache.servicecomb.core.governance.MatchType;
import org.apache.servicecomb.governance.handler.CircuitBreakerHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCompletionStage;

public class ProviderCircuitBreakerFilter implements ProviderFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProviderCircuitBreakerFilter.class);

  private final CircuitBreakerHandler circuitBreakerHandler;

  @Autowired
  public ProviderCircuitBreakerFilter(CircuitBreakerHandler circuitBreakerHandler) {
    this.circuitBreakerHandler = circuitBreakerHandler;
  }

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.PROVIDER_SCHEDULE_FILTER_ORDER - 1890;
  }

  @Override
  public String getName() {
    return "provider-circuit-breaker";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    Supplier<CompletionStage<Response>> next = createBusinessCompletionStageSupplier(invocation, nextNode);
    DecorateCompletionStage<Response> dcs = Decorators.ofCompletionStage(next);
    GovernanceRequestExtractor request = MatchType.createGovHttpRequest(invocation);

    addCircuitBreaker(dcs, request);

    CompletableFuture<Response> future = new CompletableFuture<>();

    dcs.get().whenComplete((r, e) -> {
      if (e == null) {
        future.complete(r);
        return;
      }

      if (e instanceof CallNotPermittedException) {
        future.completeExceptionally(new InvocationException(429, "circuitBreaker is open.",
            new CommonExceptionData("circuitBreaker is open.")));
        LOGGER.warn("circuitBreaker is open by policy : {}", e.getMessage());
      } else {
        future.completeExceptionally(e);
      }
    });

    return future;
  }

  private void addCircuitBreaker(DecorateCompletionStage<Response> dcs, GovernanceRequestExtractor request) {
    CircuitBreaker circuitBreaker = circuitBreakerHandler.getActuator(request);
    if (circuitBreaker != null) {
      dcs.withCircuitBreaker(circuitBreaker);
    }
  }

  private Supplier<CompletionStage<Response>> createBusinessCompletionStageSupplier(Invocation invocation,
      FilterNode nextNode) {
    return () -> nextNode.onFilter(invocation);
  }
}
