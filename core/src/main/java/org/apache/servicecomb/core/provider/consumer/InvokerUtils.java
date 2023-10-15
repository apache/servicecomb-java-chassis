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

package org.apache.servicecomb.core.provider.consumer;

import static org.apache.servicecomb.core.exception.Exceptions.toConsumerResponse;
import static org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory.CONSUMER_INNER_STATUS_CODE;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.InvocationRuntimeType;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.governance.GovernanceConfiguration;
import org.apache.servicecomb.core.governance.MatchType;
import org.apache.servicecomb.core.governance.RetryContext;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.handler.ext.FailurePredictor;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCompletionStage;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vertx.core.Context;
import jakarta.ws.rs.core.Response.Status;

public final class InvokerUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(InvokerUtils.class);

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

  @SuppressWarnings({"unchecked"})
  public static <T> T syncInvoke(String microserviceName, String transport,
      String schemaId, String operationId, Map<String, Object> swaggerArguments, Type responseType) {
    Invocation invocation = createInvocation(microserviceName, transport, schemaId, operationId,
        swaggerArguments, responseType);
    return (T) syncInvoke(invocation);
  }

  public static void reactiveInvoke(String microserviceName, String transport,
      String schemaId, String operationId, Map<String, Object> swaggerArguments, Type responseType,
      AsyncResponse asyncResp) {
    Invocation invocation = createInvocation(microserviceName, transport, schemaId, operationId,
        swaggerArguments, responseType);
    reactiveInvoke(invocation, asyncResp);
  }

  public static <T> T syncInvoke(String microserviceName, String schemaId, String operationId,
      Map<String, Object> swaggerArguments, Type responseType) {
    return syncInvoke(microserviceName, null,
        schemaId, operationId, swaggerArguments, responseType);
  }

  public static void reactiveInvoke(String microserviceName, String schemaId, String operationId,
      Map<String, Object> swaggerArguments, Type responseType,
      AsyncResponse asyncResp) {
    reactiveInvoke(microserviceName, null,
        schemaId, operationId, swaggerArguments, responseType, asyncResp);
  }

  public static Invocation createInvocation(String microserviceName, String transport,
      String schemaId, String operationId, Map<String, Object> swaggerArguments, Type responseType) {
    MicroserviceReferenceConfig microserviceReferenceConfig = SCBEngine.getInstance()
        .createMicroserviceReferenceConfig(microserviceName);
    if (microserviceReferenceConfig == null) {
      throw new InvocationException(Status.INTERNAL_SERVER_ERROR,
          new CommonExceptionData(String.format("Failed to invoke service %s. Maybe service"
              + " not registered or no active instance.", microserviceName)));
    }
    MicroserviceMeta microserviceMeta = microserviceReferenceConfig.getMicroserviceMeta();
    SchemaMeta schemaMeta = microserviceMeta.ensureFindSchemaMeta(schemaId);
    OperationMeta operationMeta = schemaMeta.ensureFindOperation(operationId);

    ReferenceConfig referenceConfig = microserviceReferenceConfig.createReferenceConfig(transport, operationMeta);
    InvocationRuntimeType invocationRuntimeType = operationMeta.buildBaseConsumerRuntimeType();
    invocationRuntimeType.setSuccessResponseType(responseType);
    return InvocationFactory
        .forConsumer(referenceConfig, operationMeta, invocationRuntimeType, swaggerArguments);
  }

  /**
   *
   * use of this method , the response type can not be determined.
   * use {@link #syncInvoke(String, String, String, String, Map, Type)} instead.
   *
   */
  @Deprecated
  public static Object syncInvoke(String microserviceName, String transport,
      String schemaId, String operationId, Map<String, Object> swaggerArguments) {
    return syncInvoke(microserviceName, transport, schemaId, operationId, swaggerArguments,
        null);
  }

  /**
   * This is an internal API, caller make sure already invoked SCBEngine.ensureStatusUp
   */
  public static Object syncInvoke(Invocation invocation) throws InvocationException {
    Response response = innerSyncInvoke(invocation);
    if (response.isSucceed()) {
      return response.getResult();
    }
    throw ExceptionFactory.convertConsumerException(response.getResult());
  }

  public static boolean isInEventLoop() {
    return Context.isOnEventLoopThread();
  }

  /**
   * This is an internal API, caller make sure already invoked SCBEngine.ensureStatusUp
   */
  public static Response innerSyncInvoke(Invocation invocation) {
    if (isInEventLoop() &&
        SCBEngine.getInstance()
            .getEnvironment()
            .getProperty("servicecomb.invocation.enableEventLoopBlockingCallCheck", boolean.class, true)) {
      throw new IllegalStateException("Can not execute sync logic in event loop.");
    }
    return toSync(invoke(invocation), invocation.getWaitTime());
  }

  private static void updateRetryStatus(Invocation invocation) {
    if (invocation.isFinished()) {
      invocation.reset();
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

  private static boolean isCompatibleRetryEnabled(Invocation invocation) {
    // maxAttempts must be greater than or equal to 1
    return GovernanceConfiguration.isRetryEnabled(invocation.getMicroserviceName())
        && GovernanceConfiguration.getRetryNextServer(invocation.getMicroserviceName())
        + GovernanceConfiguration.getRetrySameServer(invocation.getMicroserviceName()) > 0;
  }

  private static Retry getOrCreateCompatibleRetry(Invocation invocation) {
    RetryConfig retryConfig = RetryConfig.custom()
        // max attempts include the first call
        .maxAttempts(GovernanceConfiguration.getRetryNextServer(invocation.getMicroserviceName())
            + GovernanceConfiguration.getRetrySameServer(invocation.getMicroserviceName()) + 1)
        .retryOnResult(InvokerUtils::canRetryForStatusCode)
        .retryOnException(InvokerUtils::canRetryForException)
        .waitDuration(Duration.ofMillis(1))
        .build();
    RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);
    return retryRegistry.retry(invocation.getMicroserviceName());
  }

  /**
   * This is an internal API, caller make sure already invoked SCBEngine.ensureStatusUp
   */
  public static void reactiveInvoke(Invocation invocation, AsyncResponse asyncResp) {
    invoke(invocation).whenComplete((r, e) -> {
      if (e == null) {
        asyncResp.complete(r);
      } else {
        asyncResp.consumerFail(e);
      }
    });
  }

  private static void decorateReactiveRetry(Invocation invocation, DecorateCompletionStage<Response> dcs,
      GovernanceRequestExtractor request) {
    // governance implementations.
    RetryHandler retryHandler = BeanUtils.getBean(RetryHandler.class);
    Retry retry = retryHandler.getActuator(request);
    if (retry != null) {
      dcs.withRetry(retry, getOrCreateRetryPool());
    }

    if (isCompatibleRetryEnabled(invocation)) {
      // compatible implementation for retry in load balance module in old versions.
      retry = getOrCreateCompatibleRetry(invocation);
      dcs.withRetry(retry, getOrCreateRetryPool());
    }
  }

  public static boolean isSyncMethod(Method method) {
    return !isAsyncMethod(method);
  }

  public static boolean isAsyncMethod(Method method) {
    // currently only support CompletableFuture for async method definition
    return method.getReturnType().equals(CompletableFuture.class);
  }

  public static <T> T toSync(CompletableFuture<T> future, long waitInMillis) {
    try {
      if (waitInMillis > 0) {
        return future.get(waitInMillis, TimeUnit.MILLISECONDS);
      }
      return future.get();
    } catch (ExecutionException executionException) {
      throw AsyncUtils.rethrow(executionException.getCause());
    } catch (TimeoutException timeoutException) {
      throw new InvocationException(Status.REQUEST_TIMEOUT,
          new CommonExceptionData("Invocation Timeout."), timeoutException);
    } catch (Throwable e) {
      throw AsyncUtils.rethrow(e);
    }
  }

  /**
   * This method is used in new Filter implementation to replace Handler
   * NOTE: this method should never throw exception directly
   */
  public static CompletableFuture<Response> invoke(Invocation invocation) {
    Supplier<CompletionStage<Response>> next = invokeImpl(invocation);
    DecorateCompletionStage<Response> dcs = Decorators.ofCompletionStage(next);
    GovernanceRequestExtractor request = MatchType.createGovHttpRequest(invocation);

    decorateReactiveRetry(invocation, dcs, request);

    CompletableFuture<Response> result = new CompletableFuture<>();
    dcs.get().whenComplete((r, e) -> {
      ContextUtils.setInvocationContext(invocation.getParentContext());

      if (e == null) {
        result.complete(r);
        return;
      }

      String message = String.format("invoke failed, operation %s, trace id %s",
          invocation.getMicroserviceQualifiedName(),
          invocation.getTraceId());
      LOGGER.error(message, e);
      Response response = Response.createConsumerFail(e, message);
      invocation.onFinish(response);
      result.complete(response);
    });
    return result;
  }

  private static Supplier<CompletionStage<Response>> invokeImpl(Invocation invocation) {
    return () -> {
      invocation.onStart(null, System.nanoTime());
      updateRetryStatus(invocation);
      invocation.onStartHandlersRequest();
      return invocation.getMicroserviceMeta().getFilterChain()
          .onFilter(invocation)
          .exceptionally(throwable -> toConsumerResponse(invocation, throwable))
          .whenComplete((response, throwable) -> finishInvocation(invocation, response));
    };
  }

  private static void finishInvocation(Invocation invocation, Response ar) {
    invocation.getInvocationStageTrace().finishHandlersResponse();
    invocation.onFinish(ar);

    if (ar.isFailed()) {
      // re-throw exception to make sure retry based on exception
      // for InvocationException, users can configure status code for retry
      // for 490, details error are wrapped, need re-throw

      if (!(ar.getResult() instanceof InvocationException)) {
        throw AsyncUtils.rethrow(ar.getResult());
      }

      if (((InvocationException) ar.getResult()).getStatusCode() == CONSUMER_INNER_STATUS_CODE) {
        throw AsyncUtils.rethrow(ar.getResult());
      }
    }
  }

  @VisibleForTesting
  static boolean canRetryForException(Throwable e) {
    if (e instanceof InvocationException && ((InvocationException) e).getStatusCode() == Status.SERVICE_UNAVAILABLE
        .getStatusCode()) {
      return true;
    }
    return FailurePredictor.canRetryForException(FailurePredictor.STRICT_RETRIABLE, e);
  }

  @VisibleForTesting
  static boolean canRetryForStatusCode(Object response) {
    // retry on status code 503
    if (!(response instanceof Response resp)) {
      return false;
    }
    if (!resp.isFailed()) {
      return false;
    }
    if (resp.getResult() instanceof InvocationException) {
      InvocationException e = resp.getResult();
      return e.getStatusCode() == 503;
    }
    return false;
  }
}
