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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.AbstractFilter;
import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.core.filter.EdgeFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.governance.GovernanceConfiguration;
import org.apache.servicecomb.core.governance.MatchType;
import org.apache.servicecomb.core.governance.RetryContext;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.swagger.invocation.Response;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCompletionStage;
import io.github.resilience4j.retry.Retry;

public class RetryFilter extends AbstractFilter implements ConsumerFilter, EdgeFilter {
  private static final Object LOCK = new Object();

  private static volatile ScheduledExecutorService reactiveRetryPool;

  private static ScheduledExecutorService getOrCreateRetryPool() {
    if (reactiveRetryPool == null) {
      synchronized (LOCK) {
        if (reactiveRetryPool == null) {
          reactiveRetryPool = Executors.newScheduledThreadPool(2, new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
              Thread thread = new Thread(r, "reactive-retry-pool-thread-" + count.getAndIncrement());
              // avoid block shutdown
              thread.setDaemon(true);
              return thread;
            }
          });
        }
      }
    }
    return reactiveRetryPool;
  }

  private final RetryHandler retryHandler;

  @Autowired
  public RetryFilter(RetryHandler retryHandler) {
    this.retryHandler = retryHandler;
  }

  @Override
  public String getName() {
    return "retry";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    GovernanceRequestExtractor request = MatchType.createGovHttpRequest(invocation);
    Retry retry = retryHandler.getActuator(request);
    if (retry == null) {
      return nextNode.onFilter(invocation);
    }

    Supplier<CompletionStage<Response>> next = createBusinessCompletionStageSupplier(invocation, nextNode);
    DecorateCompletionStage<Response> dcs = Decorators.ofCompletionStage(next);
    dcs.withRetry(retry, getOrCreateRetryPool());
    CompletableFuture<Response> future = new CompletableFuture<>();
    dcs.get().whenComplete((r, e) -> {
      if (e == null) {
        future.complete(r);
        return;
      }

      future.completeExceptionally(e);
    });

    return future;
  }

  private Supplier<CompletionStage<Response>> createBusinessCompletionStageSupplier(Invocation invocation,
      FilterNode nextNode) {
    return () -> {
      updateRetryStatus(invocation);
      return nextNode.onFilter(invocation);
    };
  }

  private static void updateRetryStatus(Invocation invocation) {
    if (invocation.getLocalContext(RetryContext.RETRY_CONTEXT) != null) {
      if (invocation.getLocalContext(RetryContext.RETRY_LOAD_BALANCE) != null
          && (boolean) invocation.getLocalContext(RetryContext.RETRY_LOAD_BALANCE)) {
        // clear last server to avoid using user defined endpoint
        invocation.setEndpoint(null);
      }
      RetryContext retryContext = invocation.getLocalContext(RetryContext.RETRY_CONTEXT);
      retryContext.incrementRetry();
      return;
    }

    invocation.addLocalContext(RetryContext.RETRY_CONTEXT,
        new RetryContext(GovernanceConfiguration.getRetrySameServer(invocation.getMicroserviceName())));
  }

  @Override
  public int getOrder() {
    return Filter.CONSUMER_LOAD_BALANCE_ORDER - 1990;
  }
}
