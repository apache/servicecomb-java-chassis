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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.provider.consumer.SyncResponseExecutor;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.governance.MatchersManager;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.marker.GovHttpRequest;
import org.apache.servicecomb.governance.policy.RetryPolicy;
import org.apache.servicecomb.governance.properties.RetryProperties;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCompletionStage;

public class ConsumerGovernanceHandler implements Handler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerGovernanceHandler.class);

  private MatchersManager matchersManager = BeanUtils.getBean(MatchersManager.class);

  private RetryHandler retryHandler = BeanUtils.getBean(RetryHandler.class);

  private RetryProperties retryProperties = BeanUtils.getBean(RetryProperties.class);

  private static final ScheduledExecutorService RETRY_POOL = Executors.newScheduledThreadPool(2, new ThreadFactory() {
    private AtomicInteger count = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r, "governance-retry-pool-thread-" + count.getAndIncrement());
      // avoid block shutdown
      thread.setDaemon(true);
      return thread;
    }
  });

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    Supplier<CompletionStage<Response>> next = createBusinessCompletionStageSupplier(invocation);
    DecorateCompletionStage<Response> dcs = Decorators.ofCompletionStage(next);
    GovHttpRequest request = createGovHttpRequest(invocation);

    try {
      ServiceCombInvocationContext.setInvocationContext(invocation);
      addRetry(dcs, request);
    } finally {
      ServiceCombInvocationContext.removeInvocationContext();
    }

    final SyncResponseExecutor originalExecutor;
    final Executor newExecutor;
    if (invocation.getResponseExecutor() instanceof SyncResponseExecutor) {
      originalExecutor = (SyncResponseExecutor) invocation.getResponseExecutor();
      newExecutor = command -> {
        // retry的场景，对于同步调用, 同步调用的主线程已经被挂起，无法再主线程中进行重试;
        // 重试的场景，主线程等待响应线程唤醒。因此需要转换主线程，响应唤醒新的主线程，在重试逻辑成功后，再唤醒原来的主线程。
        // 重试也不能在网络线程（event-loop）中进行，未被保护的阻塞操作会导致网络线程挂起
        RETRY_POOL.submit(command);
      };
      invocation.setResponseExecutor(newExecutor);
    } else {
      originalExecutor = null;
      newExecutor = null;
    }

    dcs.get().whenComplete((r, e) -> {
      if (e == null) {
        if (originalExecutor != null) {
          originalExecutor.execute(() -> {
            asyncResp.complete(r);
          });
        } else {
          asyncResp.complete(r);
        }
        return;
      }

      if (originalExecutor != null) {
        originalExecutor.execute(() -> {
          asyncResp.consumerFail(e);
        });
      } else {
        asyncResp.consumerFail(e);
      }
    });
  }

  private void addRetry(DecorateCompletionStage<Response> dcs, GovHttpRequest request) {
    RetryPolicy retryPolicy = matchersManager.match(request, retryProperties.getParsedEntity());
    if (retryPolicy != null) {
      dcs.withRetry(retryHandler.getActuator(retryPolicy), RETRY_POOL);
    }
  }

  private Supplier<CompletionStage<Response>> createBusinessCompletionStageSupplier(Invocation invocation) {
    final int currentHandler = invocation.getHandlerIndex();
    final AtomicBoolean isRetryHolder = new AtomicBoolean(false);

    return () -> {
      CompletableFuture<Response> result = new CompletableFuture<>();
      if (isRetryHolder.getAndSet(true)) {
        invocation.setHandlerIndex(currentHandler);
        LOGGER.info("retry operation {}, trace id {}",
            invocation.getOperationMeta().getMicroserviceQualifiedName(), invocation.getTraceId());
      }
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
