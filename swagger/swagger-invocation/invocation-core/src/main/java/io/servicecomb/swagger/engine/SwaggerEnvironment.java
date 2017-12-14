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
package io.servicecomb.swagger.engine;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.ReflectUtils;
import io.servicecomb.swagger.generator.core.SwaggerGenerator;
import io.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import io.servicecomb.swagger.generator.core.utils.ClassUtils;
import io.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentsMapper;
import io.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentsMapperFactory;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapper;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapperFactory;
import io.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapper;
import io.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapperFactory;
import io.servicecomb.swagger.invocation.response.producer.ProducerResponseMapper;
import io.servicecomb.swagger.invocation.response.producer.ProducerResponseMapperFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.Swagger;

@Component
public class SwaggerEnvironment {
  @Inject
  private ProducerArgumentsMapperFactory producerArgumentsFactory;

  @Inject
  private ProducerResponseMapperFactory producerResponseMapperFactory;

  @Inject
  private ConsumerArgumentsMapperFactory consumerArgumentsFactory;

  @Inject
  private ConsumerResponseMapperFactory consumerResponseMapperFactory;

  public ProducerArgumentsMapperFactory getProducerArgumentsFactory() {
    return producerArgumentsFactory;
  }

  public void setProducerArgumentsFactory(ProducerArgumentsMapperFactory producerArgumentsFactory) {
    this.producerArgumentsFactory = producerArgumentsFactory;
  }

  public ProducerResponseMapperFactory getProducerResponseMapperFactory() {
    return producerResponseMapperFactory;
  }

  public void setProducerResponseMapperFactory(ProducerResponseMapperFactory producerResponseMapperFactory) {
    this.producerResponseMapperFactory = producerResponseMapperFactory;
  }

  public ConsumerArgumentsMapperFactory getConsumerArgumentsFactory() {
    return consumerArgumentsFactory;
  }

  public void setConsumerArgumentsFactory(ConsumerArgumentsMapperFactory consumerArgumentsFactory) {
    this.consumerArgumentsFactory = consumerArgumentsFactory;
  }

  public ConsumerResponseMapperFactory getConsumerResponseMapperFactory() {
    return consumerResponseMapperFactory;
  }

  public void setConsumerResponseMapperFactory(ConsumerResponseMapperFactory consumerResponseMapperFactory) {
    this.consumerResponseMapperFactory = consumerResponseMapperFactory;
  }

  public SwaggerConsumer createConsumer(Class<?> consumerIntf) {
    // consumer与契约接口相同
    return createConsumer(consumerIntf, consumerIntf);
  }

  public SwaggerConsumer createConsumer(Class<?> consumerIntf, Class<?> swaggerIntf) {
    SwaggerConsumer consumer = new SwaggerConsumer();
    consumer.setConsumerIntf(consumerIntf);
    consumer.setSwaggerIntf(swaggerIntf);
    for (Method consumerMethod : consumerIntf.getMethods()) {
      String methodName = consumerMethod.getName();
      // consumer参数不一定等于swagger参数
      Method swaggerMethod = ReflectUtils.findMethod(swaggerIntf, methodName);
      if (swaggerMethod == null) {
        // consumer大于契约，非法
        String msg = String.format("consumer method %s:%s not exist in swagger.",
            consumerIntf.getName(),
            consumerMethod.getName());
        throw new Error(msg);
      }

      ConsumerArgumentsMapper argsMapper =
          consumerArgumentsFactory.createArgumentsMapper(swaggerMethod, consumerMethod);
      ConsumerResponseMapper responseMapper = consumerResponseMapperFactory.createResponseMapper(
          swaggerMethod.getGenericReturnType(),
          consumerMethod.getGenericReturnType());

      SwaggerConsumerOperation op = new SwaggerConsumerOperation();
      op.setName(methodName);
      op.setConsumerMethod(consumerMethod);
      op.setSwaggerMethod(swaggerMethod);
      op.setArgumentsMapper(argsMapper);
      op.setResponseMapper(responseMapper);

      consumer.addOperation(op);
    }

    return consumer;
  }

  public SwaggerProducer createProducer(Object producerInstance, Swagger swagger) {
    Class<?> producerCls = BeanUtils.getImplClassFromBean(producerInstance);
    Map<String, Method> visibleProducerMethods = retrieveVisibleMethods(producerCls);
    Class<?> swaggerIntf = ClassUtils.getOrCreateInterface(swagger, null, null);

    SwaggerProducer producer = new SwaggerProducer();
    producer.setProducerCls(producerCls);
    producer.setSwaggerIntf(swaggerIntf);
    for (Method swaggerMethod : swaggerIntf.getMethods()) {
      String methodName = swaggerMethod.getName();
      // producer参数不一定等于swagger参数
      Method producerMethod = visibleProducerMethods.getOrDefault(methodName, null);
      if (producerMethod == null) {
        // producer未实现契约，非法
        String msg = String.format("swagger method %s:%s not exist in producer.",
            swaggerIntf.getClass().getName(),
            methodName);
        throw new Error(msg);
      }

      ProducerArgumentsMapper argsMapper = producerArgumentsFactory.createArgumentsMapper(swaggerMethod,
          producerMethod);
      ProducerResponseMapper responseMapper = producerResponseMapperFactory.createResponseMapper(
          producerMethod.getGenericReturnType(),
          swaggerMethod.getGenericReturnType());

      SwaggerProducerOperation op = new SwaggerProducerOperation();
      op.setName(methodName);
      op.setProducerClass(producerCls);
      op.setProducerInstance(producerInstance);
      op.setProducerMethod(producerMethod);
      op.setSwaggerMethod(swaggerMethod);
      op.setArgumentsMapper(argsMapper);
      op.setResponseMapper(responseMapper);

      producer.addOperation(op);
    }

    return producer;
  }

  public SwaggerProducer createProducer(Object producerInstance) {
    Class<?> producerCls = BeanUtils.getImplClassFromBean(producerInstance);
    SwaggerGenerator producerGenerator = UnitTestSwaggerUtils.generateSwagger(producerCls);
    Swagger swagger = producerGenerator.getSwagger();

    return createProducer(producerInstance, swagger);
  }

  private Map<String, Method> retrieveVisibleMethods(Class<?> clazz) {
    Map<String, Method> visibleMethods = new HashMap<>();
    for (Method method : clazz.getMethods()) {
      if (method.isAnnotationPresent(ApiOperation.class) &&
          method.getAnnotation(ApiOperation.class).hidden()) {
        continue;
      }
      visibleMethods.put(method.getName(), method);
    }
    return visibleMethods;
  }
}
