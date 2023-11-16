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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.InvocationRuntimeType;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import io.vertx.core.Context;
import jakarta.ws.rs.core.Response.Status;

public final class InvokerUtils {
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
    long startCreateInvocation = System.nanoTime();
    MicroserviceReferenceConfig microserviceReferenceConfig = SCBEngine.getInstance()
        .getOrCreateReferenceConfig(microserviceName);
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
    Invocation result = InvocationFactory
        .forConsumer(referenceConfig, operationMeta, invocationRuntimeType, swaggerArguments);
    result.getInvocationStageTrace().startCreateInvocation(startCreateInvocation);
    result.getInvocationStageTrace().finishCreateInvocation();
    return result;
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
    invocation.onStart(null);
    return invocation.getMicroserviceMeta().getConsumerFilterChain()
        .onFilter(invocation)
        .exceptionally(throwable -> toConsumerResponse(invocation, throwable))
        .whenComplete((response, throwable) -> invocation.onFinish(response));
  }
}
