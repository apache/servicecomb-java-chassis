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

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfigUtils;
import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.engine.SwaggerConsumerOperation;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.springframework.util.StringUtils;

public class Invoker implements InvocationHandler {
  // 原始数据
  private String microserviceName;

  private String schemaId;

  private Class<?> consumerIntf;

  // 生成的数据
  private SchemaMeta schemaMeta;

  private ReferenceConfig referenceConfig;

  private volatile SwaggerConsumer swaggerConsumer;

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

  protected void prepare() {
    referenceConfig = ReferenceConfigUtils.getForInvoke(microserviceName);
    MicroserviceMeta microserviceMeta = referenceConfig.getMicroserviceMeta();

    if (StringUtils.isEmpty(schemaId)) {
      // 未指定schemaId，看看consumer接口是否等于契约接口
      schemaMeta = microserviceMeta.findSchemaMeta(consumerIntf);
      if (schemaMeta == null) {
        // 尝试用consumer接口名作为schemaId
        schemaId = consumerIntf.getName();
        schemaMeta = microserviceMeta.ensureFindSchemaMeta(schemaId);
      }
    } else {
      schemaMeta = microserviceMeta.ensureFindSchemaMeta(schemaId);
    }

    this.swaggerConsumer = CseContext.getInstance().getSwaggerEnvironment().createConsumer(consumerIntf,
        schemaMeta.getSwaggerIntf());
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (swaggerConsumer == null) {
      synchronized (this) {
        if (swaggerConsumer == null) {
          prepare();
        }
      }
    }

    SwaggerConsumerOperation consumerOperation = swaggerConsumer.findOperation(method.getName());
    Invocation invocation =
        InvocationFactory
            .forConsumer(referenceConfig, schemaMeta, consumerOperation.getSwaggerMethod().getName(), null);

    consumerOperation.getArgumentsMapper().toInvocation(args, invocation);

    if (CompletableFuture.class.equals(method.getReturnType())) {
      return completableFutureInvoke(invocation, consumerOperation);
    }

    return syncInvoke(invocation, consumerOperation);
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
    CompletableFuture<Object> future = new CompletableFuture<>();
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
