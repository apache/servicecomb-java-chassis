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
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapper;
import org.apache.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapper;

public class SwaggerConsumerOperation {
  private Class<?> consumerClass;

  private Method consumerMethod;

  private String[] consumerParameterNames;

  private SwaggerOperation swaggerOperation;

  private ArgumentsMapper argumentsMapper;

  private ConsumerResponseMapper responseMapper;

  public Method getConsumerMethod() {
    return consumerMethod;
  }

  public void setConsumerMethod(Method consumerMethod) {
    this.consumerMethod = consumerMethod;

    this.consumerParameterNames = Arrays.stream(consumerMethod.getParameters())
        .map(Parameter::getName)
        .toArray(String[]::new);
  }

  public Class<?> getConsumerClass() {
    return consumerClass;
  }

  public void setConsumerClass(Class<?> consumerClass) {
    this.consumerClass = consumerClass;
  }

  public SwaggerOperation getSwaggerOperation() {
    return swaggerOperation;
  }

  public void setSwaggerOperation(SwaggerOperation swaggerOperation) {
    this.swaggerOperation = swaggerOperation;
  }

  public ArgumentsMapper getArgumentsMapper() {
    return argumentsMapper;
  }

  public void setArgumentsMapper(ArgumentsMapper argumentsMapper) {
    this.argumentsMapper = argumentsMapper;
  }

  public ConsumerResponseMapper getResponseMapper() {
    return responseMapper;
  }

  public void setResponseMapper(ConsumerResponseMapper responseMapper) {
    this.responseMapper = responseMapper;
  }

  public Map<String, Object> toInvocationArguments(Object[] args) {
    Map<String, Object> arguments = new HashMap<>();
    for (int i = 0; i < consumerParameterNames.length; i++) {
      arguments.put(consumerParameterNames[i], args[i]);
    }
    return arguments;
  }
}
