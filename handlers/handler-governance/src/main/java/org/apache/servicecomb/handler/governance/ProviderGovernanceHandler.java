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

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.governance.MatchersManager;
import org.apache.servicecomb.governance.handler.BulkheadHandler;
import org.apache.servicecomb.governance.handler.CircuitBreakerHandler;
import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.apache.servicecomb.governance.marker.GovHttpRequest;
import org.apache.servicecomb.governance.policy.BulkheadPolicy;
import org.apache.servicecomb.governance.policy.CircuitBreakerPolicy;
import org.apache.servicecomb.governance.policy.RateLimitingPolicy;
import org.apache.servicecomb.governance.properties.BulkheadProperties;
import org.apache.servicecomb.governance.properties.CircuitBreakerProperties;
import org.apache.servicecomb.governance.properties.RateLimitProperties;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCompletionStage;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

public class ProviderGovernanceHandler implements Handler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProviderGovernanceHandler.class);

  private MatchersManager matchersManager = BeanUtils.getBean(MatchersManager.class);

  private RateLimitingHandler rateLimitingHandler = BeanUtils.getBean(RateLimitingHandler.class);

  private RateLimitProperties rateLimitProperties = BeanUtils.getBean(RateLimitProperties.class);

  private CircuitBreakerHandler circuitBreakerHandler = BeanUtils.getBean(CircuitBreakerHandler.class);

  private CircuitBreakerProperties circuitBreakerProperties = BeanUtils.getBean(CircuitBreakerProperties.class);

  private BulkheadHandler bulkheadHandler = BeanUtils.getBean(BulkheadHandler.class);

  private BulkheadProperties bulkheadProperties = BeanUtils.getBean(BulkheadProperties.class);

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {

    Supplier<CompletionStage<Response>> next = createBusinessCompletionStageSupplier(invocation);
    DecorateCompletionStage<Response> dcs = Decorators.ofCompletionStage(next);
    GovHttpRequest request = createGovHttpRequest(invocation);

    RateLimitingPolicy rateLimitingPolicy = matchersManager.match(request, rateLimitProperties.getParsedEntity());
    dcs.withRateLimiter(rateLimitingHandler.getActuator(rateLimitingPolicy));
    CircuitBreakerPolicy circuitBreakerPolicy = matchersManager
        .match(request, circuitBreakerProperties.getParsedEntity());
    dcs.withCircuitBreaker(circuitBreakerHandler.getActuator(circuitBreakerPolicy));
    BulkheadPolicy bulkheadPolicy = matchersManager.match(request, bulkheadProperties.getParsedEntity());
    dcs.withBulkhead(bulkheadHandler.getActuator(bulkheadPolicy));

    dcs.get().whenComplete((r, e) -> {
      if (e == null) {
        asyncResp.complete(r);
        return;
      }

      if (e instanceof RequestNotPermitted) {
        asyncResp.complete(
            Response.failResp(new InvocationException(429, "rate limited.", new CommonExceptionData("rate limited."))));
        LOGGER.warn("the request is rate limit by policy : {}", e.getMessage());
      } else if (e instanceof CallNotPermittedException) {
        asyncResp.complete(
            Response.failResp(new InvocationException(429, "circuitBreaker is open.",
                new CommonExceptionData("circuitBreaker is open."))));
        LOGGER.warn("circuitBreaker is open by policy : {}", e.getMessage());
      } else if (e instanceof BulkheadFullException) {
        asyncResp.complete(
            Response.failResp(new InvocationException(429, "bulkhead is full and does not permit further calls.",
                new CommonExceptionData("bulkhead is full and does not permit further calls."))));
        LOGGER.warn("bulkhead is full and does not permit further calls by policy : {}", e.getMessage());
      } else {
        asyncResp.complete(Response.createProducerFail(e));
      }
    });
  }

  private Supplier<CompletionStage<Response>> createBusinessCompletionStageSupplier(Invocation invocation) {
    return () -> {
      CompletableFuture<Response> result = new CompletableFuture<>();
      try {
        invocation.next(response -> {
          result.complete(response);
        });
      } catch (Exception e) {
        result.completeExceptionally(e);
      }
      return result;
    };
  }

  private GovHttpRequest createGovHttpRequest(Invocation invocation) {
    GovHttpRequest request = new GovHttpRequest(RegistrationManager.INSTANCE.getMicroservice().getServiceName(),
        RegistrationManager.INSTANCE.getMicroservice().getVersion());
    request.setUri(invocation.getSchemaId() + "." + invocation.getOperationName());
    request.setMethod(invocation.getOperationMeta().getHttpMethod());
    request.setHeaders(invocation.getContext());
    return request;
  }
}
