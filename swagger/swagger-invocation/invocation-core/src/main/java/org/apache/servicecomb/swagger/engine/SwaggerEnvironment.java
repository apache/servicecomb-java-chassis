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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperations;
import org.apache.servicecomb.swagger.generator.core.utils.MethodUtils;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapper;
import org.apache.servicecomb.swagger.invocation.arguments.ContextArgumentMapperFactory;
import org.apache.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentsMapperCreator;
import org.apache.servicecomb.swagger.invocation.arguments.consumer.ConsumerContextArgumentMapperFactory;
import org.apache.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapper;
import org.apache.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapperCreator;
import org.apache.servicecomb.swagger.invocation.arguments.producer.ProducerContextArgumentMapperFactory;
import org.apache.servicecomb.swagger.invocation.response.ResponseMapperFactorys;
import org.apache.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapper;
import org.apache.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapperFactory;
import org.apache.servicecomb.swagger.invocation.response.producer.ProducerResponseMapper;
import org.apache.servicecomb.swagger.invocation.response.producer.ProducerResponseMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.models.Swagger;
import io.swagger.util.Json;

public class SwaggerEnvironment {
  private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerEnvironment.class);

  public SwaggerConsumer createConsumer(Class<?> consumerIntf, Swagger swagger) {
    Map<Class<?>, ContextArgumentMapperFactory> contextFactorys = SPIServiceUtils
        .getOrLoadSortedService(ConsumerContextArgumentMapperFactory.class)
        .stream()
        .collect(Collectors.toMap(ConsumerContextArgumentMapperFactory::getContextClass, Function.identity()));
    ResponseMapperFactorys<ConsumerResponseMapper> consumerResponseMapperFactorys =
        new ResponseMapperFactorys<>(ConsumerResponseMapperFactory.class);

    SwaggerOperations swaggerOperations = new SwaggerOperations(swagger);

    SwaggerConsumer consumer = new SwaggerConsumer();
    consumer.setConsumerIntf(consumerIntf);
    for (Method consumerMethod : MethodUtils.findSwaggerMethods(consumerIntf)) {
      String operationId = findOperationId(consumerMethod);
      SwaggerOperation swaggerOperation = swaggerOperations.findOperation(operationId);
      if (swaggerOperation == null) {
        // consumer method set is bigger than contract, it's invalid
        // but we need to support consumer upgrade before producer, so only log and ignore it.
        LOGGER.warn("consumer method {}:{} not exist in contract.",
            consumerIntf.getName(),
            consumerMethod.getName());
        continue;
      }

      ConsumerArgumentsMapperCreator creator = new ConsumerArgumentsMapperCreator(
          Json.mapper().getSerializationConfig(),
          contextFactorys,
          consumerIntf,
          consumerMethod,
          swaggerOperation);
      ArgumentsMapper argsMapper = creator.createArgumentsMapper();
      ConsumerResponseMapper responseMapper = consumerResponseMapperFactorys
          .createResponseMapper(consumerMethod.getGenericReturnType());

      SwaggerConsumerOperation op = new SwaggerConsumerOperation();
      op.setConsumerClass(consumerIntf);
      op.setConsumerMethod(consumerMethod);
      op.setSwaggerOperation(swaggerOperation);
      op.setArgumentsMapper(argsMapper);
      op.setResponseMapper(responseMapper);

      consumer.addOperation(op);
    }

    return consumer;
  }

  protected String findOperationId(Method consumerMethod) {
    return MethodUtils.findSwaggerMethodName(consumerMethod);
  }

  public SwaggerProducer createProducer(Object producerInstance, Swagger swagger) {
    return createProducer(producerInstance, null, swagger);
  }

  public SwaggerProducer createProducer(Object producerInstance, Class<?> schemaInterface, Swagger swagger) {
    Class<?> producerCls = targetSwaggerClass(producerInstance, schemaInterface);

    swagger = checkAndGenerateSwagger(producerCls, swagger);

    Map<Class<?>, ContextArgumentMapperFactory> contextFactories = SPIServiceUtils
        .getOrLoadSortedService(ProducerContextArgumentMapperFactory.class)
        .stream()
        .collect(Collectors.toMap(ProducerContextArgumentMapperFactory::getContextClass, Function.identity()));
    ResponseMapperFactorys<ProducerResponseMapper> producerResponseMapperFactorys =
        new ResponseMapperFactorys<>(ProducerResponseMapperFactory.class);

    SwaggerOperations swaggerOperations = new SwaggerOperations(swagger);

    Map<String, Method> visibleProducerMethods = MethodUtils.findSwaggerMethodsMapOfOperationId(producerCls);

    SwaggerProducer producer = new SwaggerProducer();
    producer.setSwagger(swagger);
    producer.setProducerCls(producerCls);
    producer.setProducerInstance(producerInstance);
    for (SwaggerOperation swaggerOperation : swaggerOperations.getOperations().values()) {
      String operationId = swaggerOperation.getOperationId();
      // producer参数不一定等于swagger参数
      Method producerMethod = visibleProducerMethods.getOrDefault(operationId, null);
      if (producerMethod == null) {
        // producer未实现契约，非法
        String msg = String.format("operationId %s not exist in producer %s.",
            operationId,
            producerInstance.getClass().getName());
        throw new IllegalStateException(msg);
      }

      ProducerArgumentsMapperCreator creator = new ProducerArgumentsMapperCreator(
          Json.mapper().getSerializationConfig(),
          contextFactories,
          producerCls,
          producerMethod,
          swaggerOperation);
      ProducerArgumentsMapper argsMapper = creator.createArgumentsMapper();
      ProducerResponseMapper responseMapper = producerResponseMapperFactorys.createResponseMapper(
          producerMethod.getGenericReturnType());

      SwaggerProducerOperation op = new SwaggerProducerOperation();
      op.setProducerClass(producerCls);
      op.setProducerInstance(producerInstance);
      op.setProducerMethod(producerMethod);
      op.setSwaggerOperation(swaggerOperation);
      op.setSwaggerParameterTypes(creator.getSwaggerParameterTypes());
      op.setArgumentsMapper(argsMapper);
      op.setResponseMapper(responseMapper);

      producer.addOperation(op);
    }

    return producer;
  }

  private Swagger checkAndGenerateSwagger(Class<?> swaggerClass, Swagger swagger) {
    if (swagger == null) {
      swagger = SwaggerGenerator.generate(swaggerClass);
    }
    return swagger;
  }

  private Class<?> targetSwaggerClass(Object producerInstance, Class<?> schemaInterface) {
    if (schemaInterface != null && !Object.class.equals(schemaInterface)) {
      return schemaInterface;
    }
    return BeanUtils.getImplClassFromBean(producerInstance);
  }
}
