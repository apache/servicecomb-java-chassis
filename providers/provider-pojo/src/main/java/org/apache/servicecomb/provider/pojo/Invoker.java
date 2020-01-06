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

package org.apache.servicecomb.provider.pojo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.core.provider.consumer.MicroserviceReferenceConfig;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.provider.pojo.definition.PojoConsumerMeta;
import org.apache.servicecomb.provider.pojo.definition.PojoConsumerOperationMeta;
import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.engine.SwaggerConsumerOperation;
import org.apache.servicecomb.swagger.generator.core.utils.MethodUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.context.InvocationContextCompletableFuture;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Invoker implements InvocationHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(Invoker.class);

  protected SCBEngine scbEngine;

  protected String appId;

  protected String microserviceName;

  // can be null, should find SchemaMeta by consumerIntf in this time
  protected String schemaId;

  protected Class<?> consumerIntf;

  // not always equals codec meta
  // for highway, codec meta is relate to target instance
  //  to avoid limit producer to only allow append parameter
  protected PojoConsumerMeta consumerMeta;

  @SuppressWarnings("unchecked")
  public static <T> T createProxy(String microserviceName, String schemaId, Class<?> consumerIntf) {
    Invoker invoker = new Invoker(microserviceName, schemaId, consumerIntf);
    return (T) Proxy.newProxyInstance(consumerIntf.getClassLoader(), new Class<?>[] {consumerIntf}, invoker);
  }

  public Invoker(String microserviceName, String schemaId, Class<?> consumerIntf) {
    this.microserviceName = microserviceName;
    this.schemaId = schemaId;
    this.consumerIntf = consumerIntf;
  }

  private void ensureStatusUp() {
    if (scbEngine == null) {
      if (SCBEngine.getInstance() == null) {
        String message =
            "The request is rejected. Cannot process the request due to SCBEngine not ready.";
        LOGGER.warn(message);
        throw new InvocationException(Status.SERVICE_UNAVAILABLE, message);
      }

      this.scbEngine = SCBEngine.getInstance();
      this.appId = scbEngine.parseAppId(microserviceName);
    }

    scbEngine.ensureStatusUp();
  }

  private boolean isNeedRefresh() {
    return consumerMeta == null || consumerMeta.isExpired();
  }

  protected SchemaMeta findSchemaMeta(MicroserviceMeta microserviceMeta) {
    // if present schemaId, just use it
    if (StringUtils.isNotEmpty(schemaId)) {
      return microserviceMeta.findSchemaMeta(schemaId);
    }

    // not present schemaId, try interface first
    SchemaMeta schemaMeta = microserviceMeta.findSchemaMeta(consumerIntf);
    if (schemaMeta != null) {
      return schemaMeta;
    }

    // try interface name second
    return microserviceMeta.findSchemaMeta(consumerIntf.getName());
  }

  private PojoConsumerMeta refreshMeta() {
    MicroserviceReferenceConfig microserviceReferenceConfig = scbEngine
        .createMicroserviceReferenceConfig(microserviceName);
    MicroserviceMeta microserviceMeta = microserviceReferenceConfig.getLatestMicroserviceMeta();

    SchemaMeta schemaMeta = findSchemaMeta(microserviceMeta);
    if (schemaMeta == null) {
      throw new IllegalStateException(
          String.format(
              "Schema not exist, microserviceName=%s, schemaId=%s, consumer interface=%s; "
                  + "new producer not running or not deployed.",
              microserviceName,
              schemaId,
              consumerIntf.getName()));
    }

    SwaggerConsumer swaggerConsumer = scbEngine.getSwaggerEnvironment()
        .createConsumer(consumerIntf, schemaMeta.getSwagger());
    return new PojoConsumerMeta(microserviceReferenceConfig, swaggerConsumer, schemaMeta);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) {
    ensureStatusUp();
    if (isNeedRefresh()) {
      synchronized (this) {
        if (isNeedRefresh()) {
          this.consumerMeta = refreshMeta();
        }
      }
    }

    PojoConsumerOperationMeta pojoConsumerOperationMeta = consumerMeta
        .findOperationMeta(MethodUtils.findSwaggerMethodName(method));
    if (pojoConsumerOperationMeta == null) {
      throw new IllegalStateException(
          String.format(
              "Consumer method %s:%s not exist in contract, microserviceName=%s, schemaId=%s; "
                  + "new producer not running or not deployed.",
              consumerIntf.getName(),
              method.getName(),
              microserviceName,
              schemaId));
    }

    SwaggerConsumerOperation consumerOperation = pojoConsumerOperationMeta.getSwaggerConsumerOperation();
    OperationMeta operationMeta = pojoConsumerOperationMeta.getOperationMeta();
    Invocation invocation = InvocationFactory.forConsumer(
        findReferenceConfig(operationMeta),
        operationMeta,
        null);
    invocation.setResponsesMeta(pojoConsumerOperationMeta.getResponsesMeta());
    invocation.setSwaggerArguments(args);

    if (CompletableFuture.class.equals(method.getReturnType())) {
      return completableFutureInvoke(invocation, consumerOperation);
    }

    return syncInvoke(invocation, consumerOperation);
  }

  protected ReferenceConfig findReferenceConfig(OperationMeta operationMeta) {
    return consumerMeta.getMicroserviceReferenceConfig().createReferenceConfig(operationMeta);
  }

  protected Object syncInvoke(Invocation invocation, SwaggerConsumerOperation consumerOperation) {
    Response response = InvokerUtils.innerSyncInvoke(invocation);
    if (response.isSuccessed()) {
      return consumerOperation.getResponseMapper().mapResponse(response);
    }

    throw ExceptionFactory.convertConsumerException(response.getResult());
  }

  protected CompletableFuture<Object> completableFutureInvoke(Invocation invocation,
      SwaggerConsumerOperation consumerOperation) {
    CompletableFuture<Object> future = new InvocationContextCompletableFuture<>(invocation);
    InvokerUtils.reactiveInvoke(invocation, response -> {
      if (response.isSuccessed()) {
        Object result = consumerOperation.getResponseMapper().mapResponse(response);
        future.complete(result);
        return;
      }

      future.completeExceptionally(response.getResult());
    });
    return future;
  }
}
