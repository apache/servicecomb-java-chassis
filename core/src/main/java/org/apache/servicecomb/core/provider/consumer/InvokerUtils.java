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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response.Status;

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
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.config.DynamicPropertyFactory;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCompletionStage;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import io.vertx.core.Context;

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

  private static final boolean ENABLE_EVENT_LOOP_BLOCKING_CALL_CHECK =
      DynamicPropertyFactory.getInstance()
          .getBooleanProperty("servicecomb.invocation.enableEventLoopBlockingCallCheck", true).get();

  @SuppressWarnings({"unchecked"})
  public static <T> T syncInvoke(String microserviceName, String microserviceVersion, String transport,
      String schemaId, String operationId, Map<String, Object> swaggerArguments, Type responseType) {
    Invocation invocation = createInvocation(microserviceName, microserviceVersion, transport, schemaId, operationId,
        swaggerArguments, responseType);
    return (T) syncInvoke(invocation);
  }

  public static void reactiveInvoke(String microserviceName, String microserviceVersion, String transport,
      String schemaId, String operationId, Map<String, Object> swaggerArguments, Type responseType,
      AsyncResponse asyncResp) {
    Invocation invocation = createInvocation(microserviceName, microserviceVersion, transport, schemaId, operationId,
        swaggerArguments, responseType);
    reactiveInvoke(invocation, asyncResp);
  }

  public static <T> T syncInvoke(String microserviceName, String schemaId, String operationId,
      Map<String, Object> swaggerArguments, Type responseType) {
    return syncInvoke(microserviceName, null, null,
        schemaId, operationId, swaggerArguments, responseType);
  }

  public static void reactiveInvoke(String microserviceName, String schemaId, String operationId,
      Map<String, Object> swaggerArguments, Type responseType,
      AsyncResponse asyncResp) {
    reactiveInvoke(microserviceName, null, null,
        schemaId, operationId, swaggerArguments, responseType, asyncResp);
  }

  private static Invocation createInvocation(String microserviceName, String microserviceVersion, String transport,
      String schemaId, String operationId, Map<String, Object> swaggerArguments, Type responseType) {
    MicroserviceReferenceConfig microserviceReferenceConfig = SCBEngine.getInstance()
        .createMicroserviceReferenceConfig(microserviceName, microserviceVersion);
    MicroserviceMeta microserviceMeta = microserviceReferenceConfig.getLatestMicroserviceMeta();
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
   * use {@link #syncInvoke(String, String, String, Map, Type)} instead.
   *
   */
  @Deprecated
  public static Object syncInvoke(String microserviceName, String schemaId, String operationId,
      Map<String, Object> swaggerArguments) {
    return syncInvoke(microserviceName, null, null, schemaId, operationId, swaggerArguments);
  }

  /**
   *
   * use of this method , the response type can not be determined.
   * use {@link #syncInvoke(String, String, String, String, String, Map, Type)} instead.
   *
   */
  @Deprecated
  public static Object syncInvoke(String microserviceName, String microserviceVersion, String transport,
      String schemaId, String operationId, Map<String, Object> swaggerArguments) {
    return syncInvoke(microserviceName, microserviceVersion, transport, schemaId, operationId, swaggerArguments,
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
    GovernanceRequest request = MatchType.createGovHttpRequest(invocation);

    return decorateSyncRetry(invocation, request);
  }

  private static Response innerSyncInvokeImpl(Invocation invocation) throws Throwable {
    if (ENABLE_EVENT_LOOP_BLOCKING_CALL_CHECK && isInEventLoop()) {
      throw new IllegalStateException("Can not execute sync logic in event loop. ");
    }
    invocation.onStart(null, System.nanoTime());
    updateRetryStatus(invocation);
    SyncResponseExecutor respExecutor = new SyncResponseExecutor();
    invocation.setResponseExecutor(respExecutor);

    invocation.onStartHandlersRequest();
    invocation.next(respExecutor::setResponse);

    Response response = respExecutor.waitResponse(invocation);
    invocation.getInvocationStageTrace().finishHandlersResponse();
    invocation.onFinish(response);

    if (response.isFailed()) {
      // re-throw exception to make sure retry based on exception
      // for InvocationException, users can configure status code for retry
      // for 490, details error are wrapped, need re-throw

      if (!(response.getResult() instanceof InvocationException)) {
        throw (Throwable) response.getResult();
      }

      if (((InvocationException) response.getResult()).getStatusCode() == CONSUMER_INNER_STATUS_CODE) {
        throw (Throwable) response.getResult();
      }
    }

    return response;
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

  private static Response decorateSyncRetry(Invocation invocation, GovernanceRequest request) {
    try {
      // governance implementations.
      RetryHandler retryHandler = BeanUtils.getBean(RetryHandler.class);
      Retry retry = retryHandler.getActuator(request);
      if (retry != null) {
        CheckedFunction0<Response> supplier = Retry
            .decorateCheckedSupplier(retry, () -> innerSyncInvokeImpl(invocation));
        return Try.of(supplier).get();
      }

      if (isCompatibleRetryEnabled(invocation)) {
        // compatible implementation for retry in load balance module in old versions.
        retry = getOrCreateCompatibleRetry(invocation);
        CheckedFunction0<Response> supplier = Retry
            .decorateCheckedSupplier(retry, () -> innerSyncInvokeImpl(invocation));
        return Try.of(supplier).get();
      }

      // retry not enabled
      return innerSyncInvokeImpl(invocation);
    } catch (Throwable e) {
      String message = String.format("invoke failed, operation %s, trace id %s",
          invocation.getMicroserviceQualifiedName(),
          invocation.getTraceId());
      LOGGER.error(message, e);

      Response response = Response.createConsumerFail(e, message);
      invocation.onFinish(response);
      return response;
    }
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
        .waitDuration(Duration.ofMillis(0))
        .build();
    RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);
    return retryRegistry.retry(invocation.getMicroserviceName());
  }

  /**
   * This is an internal API, caller make sure already invoked SCBEngine.ensureStatusUp
   */
  public static void reactiveInvoke(Invocation invocation, AsyncResponse asyncResp) {
    invocation.setSync(false);

    Supplier<CompletionStage<Response>> next = reactiveInvokeImpl(invocation);
    DecorateCompletionStage<Response> dcs = Decorators.ofCompletionStage(next);
    GovernanceRequest request = MatchType.createGovHttpRequest(invocation);

    decorateReactiveRetry(invocation, dcs, request);

    dcs.get().whenComplete((r, e) -> {
      if (e == null) {
        asyncResp.complete(r);
        return;
      }

      String message = String.format("invoke failed, operation %s, trace id %s",
          invocation.getMicroserviceQualifiedName(),
          invocation.getTraceId());
      LOGGER.error(message, e);
      Response response = Response.createConsumerFail(e, message);
      invocation.onFinish(response);
      asyncResp.complete(response);
    });
  }

  private static void decorateReactiveRetry(Invocation invocation, DecorateCompletionStage<Response> dcs,
      GovernanceRequest request) {
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

  private static Supplier<CompletionStage<Response>> reactiveInvokeImpl(Invocation invocation) {
    return () -> {
      CompletableFuture<Response> result = new CompletableFuture<>();
      try {
        invocation.onStart(null, System.nanoTime());
        updateRetryStatus(invocation);

        ReactiveResponseExecutor respExecutor = new ReactiveResponseExecutor();
        invocation.setResponseExecutor(respExecutor);
        invocation.onStartHandlersRequest();
        invocation.next(ar -> {
          ContextUtils.setInvocationContext(invocation.getParentContext());

          invocation.getInvocationStageTrace().finishHandlersResponse();
          invocation.onFinish(ar);
          try {
            if (ar.isFailed()) {
              // re-throw exception to make sure retry based on exception
              // for InvocationException, users can configure status code for retry
              // for 490, details error are wrapped, need re-throw

              if (!(ar.getResult() instanceof InvocationException)) {
                result.completeExceptionally(ar.getResult());
                return;
              }

              if (((InvocationException) ar.getResult()).getStatusCode() == CONSUMER_INNER_STATUS_CODE) {
                result.completeExceptionally(ar.getResult());
                return;
              }
            }

            result.complete(ar);
          } finally {
            ContextUtils.removeInvocationContext();
          }
        });
      } catch (Throwable e) {
        result.completeExceptionally(e);
      }
      return result;
    };
  }

  public static boolean isSyncMethod(@Nonnull Method method) {
    return !isAsyncMethod(method);
  }

  public static boolean isAsyncMethod(@Nonnull Method method) {
    // currently only support CompletableFuture for async method definition
    return method.getReturnType().equals(CompletableFuture.class);
  }

  /**
   * This method is used in new Filter implementation to replace Handler
   * NOTE: this method should never throw exception directly
   */
  public static CompletableFuture<Response> invoke(Invocation invocation) {
    Supplier<CompletionStage<Response>> next = invokeImpl(invocation);
    DecorateCompletionStage<Response> dcs = Decorators.ofCompletionStage(next);
    GovernanceRequest request = MatchType.createGovHttpRequest(invocation);

    decorateReactiveRetry(invocation, dcs, request);

    CompletableFuture<Response> result = new CompletableFuture<>();
    dcs.get().whenComplete((r, e) -> {
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
        AsyncUtils.rethrow(ar.getResult());
        return;
      }

      if (((InvocationException) ar.getResult()).getStatusCode() == CONSUMER_INNER_STATUS_CODE) {
        AsyncUtils.rethrow(ar.getResult());
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
    if (!(response instanceof Response)) {
      return false;
    }
    Response resp = (Response) response;
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
