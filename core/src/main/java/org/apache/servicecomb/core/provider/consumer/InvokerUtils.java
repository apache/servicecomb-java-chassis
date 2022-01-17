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

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.InvocationRuntimeType;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.governance.GovernanceConfiguration;
import org.apache.servicecomb.core.governance.MatchType;
import org.apache.servicecomb.core.governance.RetryContext;
import org.apache.servicecomb.core.governance.ServiceCombInvocationContext;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.netflix.config.DynamicPropertyFactory;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCompletionStage;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import io.vertx.core.Context;
import io.vertx.core.VertxException;

public final class InvokerUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(InvokerUtils.class);

  private static final Map<Class<? extends Throwable>, List<String>> STRICT_RETRIABLE =
      ImmutableMap.<Class<? extends Throwable>, List<String>>builder()
          .put(ConnectException.class, Collections.emptyList())
          .put(SocketTimeoutException.class, Collections.emptyList())
          /*
           * deal with some special exceptions caused by the server side close the connection
           */
          .put(IOException.class, Collections.singletonList("Connection reset by peer"))
          .put(VertxException.class, Collections.singletonList("Connection was closed"))
          .put(NoRouteToHostException.class, Collections.emptyList())
          .build();

  private static final Object LOCK = new Object();

  private static ScheduledExecutorService reactiveRetryPool;

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
    invocation.onStart(null, System.nanoTime());

    GovernanceRequest request = MatchType.createGovHttpRequest(invocation);

    try {
      ServiceCombInvocationContext.setInvocationContext(invocation);
      return decorateSyncRetry(invocation, request);
    } finally {
      ServiceCombInvocationContext.removeInvocationContext();
    }
  }

  private static Response innerSyncInvokeImpl(Invocation invocation) {
    try {
      if (ENABLE_EVENT_LOOP_BLOCKING_CALL_CHECK && isInEventLoop()) {
        throw new IllegalStateException("Can not execute sync logic in event loop. ");
      }
      updateRetryStatus(invocation);
      SyncResponseExecutor respExecutor = new SyncResponseExecutor();
      invocation.setResponseExecutor(respExecutor);

      invocation.onStartHandlersRequest();
      invocation.next(respExecutor::setResponse);

      Response response = respExecutor.waitResponse(invocation);
      invocation.getInvocationStageTrace().finishHandlersResponse();
      invocation.onFinish(response);
      return response;
    } catch (Throwable e) {
      String msg =
          String.format("invoke failed, %s", invocation.getOperationMeta().getMicroserviceQualifiedName());
      LOGGER.error(msg, e);

      Response response = Response.createConsumerFail(e);
      invocation.onFinish(response);
      return response;
    }
  }

  private static void updateRetryStatus(Invocation invocation) {
    if (invocation.getHandlerIndex() != 0) {
      // for retry, reset index
      invocation.setHandlerIndex(0);
      RetryContext retryContext = invocation.getLocalContext(RetryContext.RETRY_CONTEXT);
      retryContext.incrementRetry();
      return;
    }

    invocation.addLocalContext(RetryContext.RETRY_CONTEXT,
        new RetryContext(GovernanceConfiguration.getRetrySameServer(invocation.getMicroserviceName())));
  }

  private static Response decorateSyncRetry(Invocation invocation, GovernanceRequest request) {
    // governance implementations.
    RetryHandler retryHandler = BeanUtils.getBean(RetryHandler.class);
    Retry retry = retryHandler.getActuator(request);
    if (retry != null) {
      CheckedFunction0<Response> supplier = Retry.decorateCheckedSupplier(retry, () -> innerSyncInvokeImpl(invocation));
      return Try.of(supplier).get();
    }

    if (isCompatibleRetryEnabled(invocation)) {
      // compatible implementation for retry in load balance module in old versions.
      retry = getOrCreateCompatibleRetry(invocation);
      CheckedFunction0<Response> supplier = Retry.decorateCheckedSupplier(retry, () -> innerSyncInvokeImpl(invocation));
      return Try.of(supplier).get();
    }

    // retry not enabled
    return innerSyncInvokeImpl(invocation);
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
    invocation.onStart(null, System.nanoTime());
    invocation.setSync(false);

    Supplier<CompletionStage<Response>> next = reactiveInvokeImpl(invocation);
    DecorateCompletionStage<Response> dcs = Decorators.ofCompletionStage(next);
    GovernanceRequest request = MatchType.createGovHttpRequest(invocation);

    try {
      ServiceCombInvocationContext.setInvocationContext(invocation);
      decorateReactiveRetry(invocation, dcs, request);
    } finally {
      ServiceCombInvocationContext.removeInvocationContext();
    }

    dcs.get().whenComplete((r, e) -> {
      if (e == null) {
        asyncResp.complete(r);
        return;
      }

      asyncResp.consumerFail(e);
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
        updateRetryStatus(invocation);

        ReactiveResponseExecutor respExecutor = new ReactiveResponseExecutor();
        invocation.setResponseExecutor(respExecutor);
        invocation.onStartHandlersRequest();
        invocation.next(ar -> {
          ContextUtils.setInvocationContext(invocation.getParentContext());
          try {
            invocation.getInvocationStageTrace().finishHandlersResponse();
            invocation.onFinish(ar);
            result.complete(ar);
          } finally {
            ContextUtils.removeInvocationContext();
          }
        });
      } catch (Throwable e) {
        invocation.getInvocationStageTrace().finishHandlersResponse();
        //if throw exception,we can use 500 for status code ?
        Response response = Response.createConsumerFail(e);
        invocation.onFinish(response);
        LOGGER.error("invoke failed, {}", invocation.getOperationMeta().getMicroserviceQualifiedName());
        result.complete(response);
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

    try {
      ServiceCombInvocationContext.setInvocationContext(invocation);
      decorateReactiveRetry(invocation, dcs, request);
    } finally {
      ServiceCombInvocationContext.removeInvocationContext();
    }

    CompletableFuture<Response> result = new CompletableFuture<>();
    dcs.get().whenComplete((r, e) -> {
      if (e != null) {
        result.completeExceptionally(e);
      } else {
        result.complete(r);
      }
    });
    return result;
  }

  private static Supplier<CompletionStage<Response>> invokeImpl(Invocation invocation) {
    invocation.onStart(null, System.nanoTime());
    return () -> {
      updateRetryStatus(invocation);
      invocation.onStartHandlersRequest();
      return invocation.getMicroserviceMeta().getFilterChain()
          .onFilter(invocation)
          .exceptionally(throwable -> toConsumerResponse(invocation, throwable))
          .whenComplete((response, throwable) -> finishInvocation(invocation, response));
    };
  }

  private static void finishInvocation(Invocation invocation, Response response) {
    processMetrics(invocation, response);

    if (response.isFailed()) {
      AsyncUtils.rethrow(response.getResult());
    }
  }

  private static void processMetrics(Invocation invocation, Response response) {
    invocation.getInvocationStageTrace().finishHandlersResponse();
    invocation.onFinish(response);
  }

  private static boolean canRetryForStatusCode(Object response) {
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

  private static boolean canRetryForException(Throwable throwableToSearchIn) {
    // retry on exception type on message match
    int infiniteLoopPreventionCounter = 10;
    while (throwableToSearchIn != null && infiniteLoopPreventionCounter > 0) {
      infiniteLoopPreventionCounter--;
      for (Entry<Class<? extends Throwable>, List<String>> c : STRICT_RETRIABLE.entrySet()) {
        Class<? extends Throwable> key = c.getKey();
        if (key.isAssignableFrom(throwableToSearchIn.getClass())) {
          if (c.getValue() == null || c.getValue().isEmpty()) {
            return true;
          } else {
            String msg = throwableToSearchIn.getMessage();
            for (String val : c.getValue()) {
              if (val.equals(msg)) {
                return true;
              }
            }
          }
        }
      }
      throwableToSearchIn = throwableToSearchIn.getCause();
    }
    return false;
  }
}
