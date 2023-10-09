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
import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCompletionStage;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

public class ProviderRateLimitingFilter implements ProviderFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProviderRateLimitingFilter.class);

  private final RateLimitingHandler rateLimitingHandler;

  @Autowired
  public ProviderRateLimitingFilter(RateLimitingHandler rateLimitingHandler) {
    this.rateLimitingHandler = rateLimitingHandler;
  }

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return Filter.PROVIDER_SCHEDULE_FILTER_ORDER - 1900;
  }

  @Override
  public String getName() {
    return "provider-rate-limiting";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {

    Supplier<CompletionStage<Response>> next = createBusinessCompletionStageSupplier(invocation, nextNode);
    DecorateCompletionStage<Response> dcs = Decorators.ofCompletionStage(next);
    GovernanceRequestExtractor request = MatchType.createGovHttpRequest(invocation);

    addRateLimiting(dcs, request);

    CompletableFuture<Response> future = new CompletableFuture<>();

    dcs.get().whenComplete((r, e) -> {
      if (e == null) {
        future.complete(r);
        return;
      }

      if (e instanceof RequestNotPermitted) {
        future.completeExceptionally(
            new InvocationException(429, "rate limited.", new CommonExceptionData("rate limited.")));
        LOGGER.warn("the request is rate limit by policy : {}", e.getMessage());
      } else {
        future.completeExceptionally(e);
      }
    });

    return future;
  }

  private void addRateLimiting(DecorateCompletionStage<Response> dcs, GovernanceRequestExtractor request) {
    RateLimiter rateLimiter = rateLimitingHandler.getActuator(request);
    if (rateLimiter != null) {
      dcs.withRateLimiter(rateLimiter);
    }
  }

  private Supplier<CompletionStage<Response>> createBusinessCompletionStageSupplier(Invocation invocation,
      FilterNode nextNode) {
    return () -> nextNode.onFilter(invocation);
  }
}
