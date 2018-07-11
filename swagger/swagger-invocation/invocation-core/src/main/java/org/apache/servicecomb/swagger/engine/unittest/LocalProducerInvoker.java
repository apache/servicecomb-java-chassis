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
package org.apache.servicecomb.swagger.engine.unittest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.engine.SwaggerConsumerOperation;
import org.apache.servicecomb.swagger.engine.SwaggerProducer;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;

public class LocalProducerInvoker implements InvocationHandler {
  private Object proxy;

  private SwaggerConsumer consumer;

  private SwaggerProducer producer;

  private SwaggerInvocation invocation;

  private Response producerResponse;

  public LocalProducerInvoker(SwaggerConsumer consumer, SwaggerProducer producer) {
    this.consumer = consumer;
    this.producer = producer;

    proxy = Proxy.newProxyInstance(consumer.getConsumerIntf().getClassLoader(),
        new Class<?>[] {consumer.getConsumerIntf()},
        this);
  }

  @SuppressWarnings("unchecked")
  public <T> T getProxy() {
    return (T) proxy;
  }

  public SwaggerInvocation getInvocation() {
    return invocation;
  }

  public <T> T getSwaggerArgument(int idx) {
    return invocation.getSwaggerArgument(idx);
  }

  public String getContext(String key) {
    return invocation.getContext(key);
  }

  public Response getProducerResponse() {
    return producerResponse;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    invocation = new SwaggerInvocation();

    SwaggerConsumerOperation consumerOp = consumer.findOperation(method.getName());
    SwaggerProducerOperation producerOp = producer.findOperation(consumerOp.getSwaggerMethod().getName());

    consumerOp.getArgumentsMapper().toInvocation(args, invocation);

    CompletableFuture<Object> future = new CompletableFuture<>();
    producerOp.invoke(invocation, ar -> {
      producerResponse = ar;
      Object realResult = consumerOp.getResponseMapper().mapResponse(producerResponse);
      future.complete(realResult);
    });

    if (CompletableFuture.class.equals(method.getReturnType())) {
      return future;
    }

    return future.get();
  }
}
