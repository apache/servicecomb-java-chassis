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

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.governance.MatchType;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.policy.CircuitBreakerPolicy;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCompletionStage;

public class ConsumerInstanceIsolationHandler implements Handler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerInstanceIsolationHandler.class);

  private final InstanceIsolationHandler instanceIsolationHandler = BeanUtils.getBean(InstanceIsolationHandler.class);

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    if (invocation.getEndpoint() == null) {
      invocation.next(asyncResp);
      return;
    }
    Supplier<CompletionStage<Response>> next = createBusinessCompletionStageSupplier(invocation);
    DecorateCompletionStage<Response> dcs = Decorators.ofCompletionStage(next);
    GovernanceRequestExtractor request = MatchType.createGovHttpRequest(invocation);

    CircuitBreakerPolicy circuitBreakerPolicy = instanceIsolationHandler.matchPolicy(request);
    if (circuitBreakerPolicy != null && circuitBreakerPolicy.isForceOpen()) {
      asyncResp.consumerFail(new InvocationException(Status.SERVICE_UNAVAILABLE,
          "Policy " + circuitBreakerPolicy.getName() + " forced open and deny requests"));
      return;
    }
    addCircuitBreaker(dcs, request);

    dcs.get().whenComplete((r, e) -> {
      if (e == null) {
        asyncResp.complete(r);
        return;
      }

      if (e instanceof CallNotPermittedException) {
        LOGGER.warn("instance isolation circuitBreaker is open by policy : {}", e.getMessage());
        EventManager.post(createInstanceIsolatedEvent(circuitBreakerPolicy, request));
        // return 503 so that consumer can retry
        asyncResp.complete(
            Response.failResp(new InvocationException(503, "instance isolation circuitBreaker is open.",
                new CommonExceptionData("instance isolation circuitBreaker is open."))));
      } else {
        asyncResp.complete(Response.createProducerFail(e));
      }
    });
  }

  private Object createInstanceIsolatedEvent(CircuitBreakerPolicy circuitBreakerPolicy,
      GovernanceRequestExtractor requestExtractor) {
    return new InstanceIsolatedEvent(requestExtractor.instanceId(),
        Duration.parse(circuitBreakerPolicy.getWaitDurationInOpenState()));
  }

  private void addCircuitBreaker(DecorateCompletionStage<Response> dcs, GovernanceRequestExtractor request) {
    CircuitBreaker circuitBreaker = instanceIsolationHandler.getActuator(request);
    if (circuitBreaker != null) {
      dcs.withCircuitBreaker(circuitBreaker);
    }
  }

  private Supplier<CompletionStage<Response>> createBusinessCompletionStageSupplier(Invocation invocation) {
    return () -> {
      CompletableFuture<Response> result = new CompletableFuture<>();
      try {
        invocation.next(result::complete);
      } catch (Exception e) {
        result.completeExceptionally(e);
      }
      return result;
    };
  }
}
