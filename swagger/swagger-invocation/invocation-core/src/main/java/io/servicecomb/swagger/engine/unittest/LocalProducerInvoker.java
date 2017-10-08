/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.swagger.engine.unittest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import io.servicecomb.swagger.engine.SwaggerConsumer;
import io.servicecomb.swagger.engine.SwaggerConsumerOperation;
import io.servicecomb.swagger.engine.SwaggerProducer;
import io.servicecomb.swagger.engine.SwaggerProducerOperation;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.SwaggerInvocation;

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

  public <T> T getContext(String key) {
    return invocation.getContext(key);
  }

  public Response getProducerResponse() {
    return producerResponse;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    invocation = new SwaggerInvocation();

    SwaggerConsumerOperation consumerOp = consumer.findOperation(method.getName());
    SwaggerProducerOperation producerOp = producer.findOperation(method.getName());

    consumerOp.getArgumentsMapper().toInvocation(args, invocation);
    producerResponse = producerOp.doInvoke(invocation);
    return consumerOp.getResponseMapper().mapResponse(producerResponse);
  }
}
