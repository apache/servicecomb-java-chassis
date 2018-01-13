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
package org.apache.servicecomb.swagger.engine;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;
import org.apache.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentsMapper;
import org.apache.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentsMapperFactory;
import org.apache.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapper;
import org.apache.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapperFactory;
import org.apache.servicecomb.swagger.invocation.converter.ConverterMgr;
import org.apache.servicecomb.swagger.invocation.response.ResponseMapperFactorys;
import org.apache.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapper;
import org.apache.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapperFactory;
import org.apache.servicecomb.swagger.invocation.response.producer.ProducerResponseMapper;
import org.apache.servicecomb.swagger.invocation.response.producer.ProducerResponseMapperFactory;
import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiOperation;
import io.swagger.models.Swagger;

@Component
public class SwaggerEnvironment {
  @Inject
  private ProducerArgumentsMapperFactory producerArgumentsFactory;

  private ResponseMapperFactorys<ProducerResponseMapper> producerResponseMapperFactorys =
      new ResponseMapperFactorys<>(ProducerResponseMapperFactory.class);

  @Inject
  private ConsumerArgumentsMapperFactory consumerArgumentsFactory;

  private ResponseMapperFactorys<ConsumerResponseMapper> consumerResponseMapperFactorys =
      new ResponseMapperFactorys<>(ConsumerResponseMapperFactory.class);

  @Inject
  public void setConverterMgr(ConverterMgr converterMgr) {
    consumerResponseMapperFactorys.setConverterMgr(converterMgr);
    producerResponseMapperFactorys.setConverterMgr(converterMgr);
  }

  public ProducerArgumentsMapperFactory getProducerArgumentsFactory() {
    return producerArgumentsFactory;
  }

  public void setProducerArgumentsFactory(ProducerArgumentsMapperFactory producerArgumentsFactory) {
    this.producerArgumentsFactory = producerArgumentsFactory;
  }

  public ConsumerArgumentsMapperFactory getConsumerArgumentsFactory() {
    return consumerArgumentsFactory;
  }

  public void setConsumerArgumentsFactory(ConsumerArgumentsMapperFactory consumerArgumentsFactory) {
    this.consumerArgumentsFactory = consumerArgumentsFactory;
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
      String swaggerMethodName = findSwaggerMethodName(consumerMethod);
      // consumer参数不一定等于swagger参数
      Method swaggerMethod = ReflectUtils.findMethod(swaggerIntf, swaggerMethodName);
      if (swaggerMethod == null) {
        // consumer大于契约，非法
        String msg = String.format("consumer method %s:%s not exist in swagger.",
            consumerIntf.getName(),
            consumerMethod.getName());
        throw new Error(msg);
      }

      ConsumerArgumentsMapper argsMapper =
          consumerArgumentsFactory.createArgumentsMapper(swaggerMethod, consumerMethod);
      ConsumerResponseMapper responseMapper = consumerResponseMapperFactorys.createResponseMapper(
          swaggerMethod.getGenericReturnType(),
          consumerMethod.getGenericReturnType());

      SwaggerConsumerOperation op = new SwaggerConsumerOperation();
      op.setName(consumerMethod.getName());
      op.setConsumerMethod(consumerMethod);
      op.setSwaggerMethod(swaggerMethod);
      op.setArgumentsMapper(argsMapper);
      op.setResponseMapper(responseMapper);

      consumer.addOperation(op);
    }

    return consumer;
  }

  protected String findSwaggerMethodName(Method consumerMethod) {
    ApiOperation apiOperationAnnotation = consumerMethod.getAnnotation(ApiOperation.class);
    if (apiOperationAnnotation == null || StringUtils.isEmpty(apiOperationAnnotation.nickname())) {
      return consumerMethod.getName();
    }

    return apiOperationAnnotation.nickname();
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
      ProducerResponseMapper responseMapper = producerResponseMapperFactorys.createResponseMapper(
          swaggerMethod.getGenericReturnType(),
          producerMethod.getGenericReturnType());

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
      String methodName = method.getName();
      ApiOperation apiOperationAnnotation = method.getAnnotation(ApiOperation.class);
      if (apiOperationAnnotation != null) {
        if (apiOperationAnnotation.hidden()) {
          continue;
        }

        if (StringUtils.isNotEmpty(apiOperationAnnotation.nickname())) {
          methodName = apiOperationAnnotation.nickname();
        }
      }

      visibleMethods.put(methodName, method);
    }
    return visibleMethods;
  }
}
