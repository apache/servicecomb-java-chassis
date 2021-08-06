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

import javax.annotation.Nonnull;

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
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.Context;

public final class InvokerUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(InvokerUtils.class);

  private static boolean enableEventLoopBlockingCallCheck =
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
    Invocation invocation = InvocationFactory
        .forConsumer(referenceConfig, operationMeta, invocationRuntimeType, swaggerArguments);
    return invocation;
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
   * @param invocation
   * @return contract result
   * @throws InvocationException
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
   * @param invocation
   * @return servicecomb response object
   */
  public static Response innerSyncInvoke(Invocation invocation) {
    try {
      if (enableEventLoopBlockingCallCheck && isInEventLoop()) {
        throw new IllegalStateException("Can not execute sync logic in event loop. ");
      }
      invocation.onStart(null, System.nanoTime());
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

  /**
   * This is an internal API, caller make sure already invoked SCBEngine.ensureStatusUp
   * @param invocation
   * @param asyncResp
   */
  public static void reactiveInvoke(Invocation invocation, AsyncResponse asyncResp) {
    try {
      invocation.onStart(null, System.nanoTime());
      invocation.setSync(false);

      ReactiveResponseExecutor respExecutor = new ReactiveResponseExecutor();
      invocation.setResponseExecutor(respExecutor);

      invocation.onStartHandlersRequest();
      invocation.next(ar -> {
        ContextUtils.setInvocationContext(invocation.getParentContext());
        try {
          invocation.getInvocationStageTrace().finishHandlersResponse();
          invocation.onFinish(ar);
          asyncResp.handle(ar);
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
      asyncResp.handle(response);
    }
  }

  public static boolean isSyncMethod(@Nonnull Method method) {
    return !isAsyncMethod(method);
  }

  public static boolean isAsyncMethod(@Nonnull Method method) {
    // todo: should be extendable to support other reactive return type, eg: rxJava / project-reactor
    return method.getReturnType().equals(CompletableFuture.class);
  }

  /**
   * should never throw exception directly
   *
   * @param invocation invocation
   * @return CompletableFuture<Response>
   */
  public static CompletableFuture<Response> invoke(Invocation invocation) {
    invocation.onStart(null, System.nanoTime());
    invocation.onStartHandlersRequest();
    return invocation.getMicroserviceMeta().getFilterChain()
        .onFilter(invocation)
        .exceptionally(throwable -> toConsumerResponse(invocation, throwable))
        .whenComplete((response, throwable) -> finishInvocation(invocation, response));
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
}
