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

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.governance.MatchType;
import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.policy.CircuitBreakerPolicy;
import org.apache.servicecomb.registry.DiscoveryManager;
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
import jakarta.ws.rs.core.Response.Status;

public class ConsumerInstanceIsolationFilter implements ConsumerFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerInstanceIsolationFilter.class);

  private final InstanceIsolationHandler instanceIsolationHandler;

  private DiscoveryManager discoveryManager;

  @Autowired
  public ConsumerInstanceIsolationFilter(InstanceIsolationHandler instanceIsolationHandler) {
    this.instanceIsolationHandler = instanceIsolationHandler;
  }

  @Autowired
  public void setDiscoveryManager(DiscoveryManager discoveryManager) {
    this.discoveryManager = discoveryManager;
  }

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.CONSUMER_LOAD_BALANCE_ORDER + 1050;
  }

  @Override
  public String getName() {
    return "instance-isolation";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    if (invocation.getEndpoint() == null) {
      return CompletableFuture.failedFuture(new InvocationException(Status.INTERNAL_SERVER_ERROR,
          new CommonExceptionData("instance isolation should work after load balancer.")));
    }

    GovernanceRequestExtractor request = MatchType.createGovHttpRequest(invocation);
    CircuitBreakerPolicy circuitBreakerPolicy = instanceIsolationHandler.matchPolicy(request);
    if (circuitBreakerPolicy != null && circuitBreakerPolicy.isForceOpen()) {
      return CompletableFuture.failedFuture(new InvocationException(Status.SERVICE_UNAVAILABLE,
          new CommonExceptionData("Policy \" + circuitBreakerPolicy.getName() + \" forced open and deny requests\"")));
    }

    Supplier<CompletionStage<Response>> next = createBusinessCompletionStageSupplier(invocation, nextNode);
    DecorateCompletionStage<Response> dcs = Decorators.ofCompletionStage(next);

    addCircuitBreaker(dcs, request);

    CompletableFuture<Response> future = new CompletableFuture<>();

    dcs.get().whenComplete((r, e) -> {
      if (e == null) {
        future.complete(r);
        return;
      }

      if (e instanceof CallNotPermittedException) {
        LOGGER.warn("instance isolation circuitBreaker is open by policy : {}", e.getMessage());
        discoveryManager.onInstanceIsolated(invocation.getEndpoint().getMicroserviceInstance(),
            Duration.parse(circuitBreakerPolicy.getWaitDurationInOpenState()).toMillis());
        // return 503 so that consumer can retry
        future.complete(Response.failResp(new InvocationException(Status.SERVICE_UNAVAILABLE,
            new CommonExceptionData("instance isolation circuitBreaker is open."))));
      } else {
        future.completeExceptionally(e);
      }
    });

    return future;
  }

  private void addCircuitBreaker(DecorateCompletionStage<Response> dcs, GovernanceRequestExtractor request) {
    CircuitBreaker circuitBreaker = instanceIsolationHandler.getActuator(request);
    if (circuitBreaker != null) {
      dcs.withCircuitBreaker(circuitBreaker);
    }
  }

  private Supplier<CompletionStage<Response>> createBusinessCompletionStageSupplier(Invocation invocation,
      FilterNode nextNode) {
    return () -> nextNode.onFilter(invocation);
  }
}
