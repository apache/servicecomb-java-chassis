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

import org.apache.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentsMapper;
import org.apache.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapper;

public class SwaggerConsumerOperation {
  private String name;

  private Method consumerMethod;

  private Method swaggerMethod;

  private ConsumerArgumentsMapper argumentsMapper;

  private ConsumerResponseMapper responseMapper;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Method getConsumerMethod() {
    return consumerMethod;
  }

  public void setConsumerMethod(Method consumerMethod) {
    this.consumerMethod = consumerMethod;
  }

  public Method getSwaggerMethod() {
    return swaggerMethod;
  }

  public void setSwaggerMethod(Method swaggerMethod) {
    this.swaggerMethod = swaggerMethod;
  }

  public ConsumerArgumentsMapper getArgumentsMapper() {
    return argumentsMapper;
  }

  public void setArgumentsMapper(ConsumerArgumentsMapper argumentsMapper) {
    this.argumentsMapper = argumentsMapper;
  }

  public ConsumerResponseMapper getResponseMapper() {
    return responseMapper;
  }

  public void setResponseMapper(ConsumerResponseMapper responseMapper) {
    this.responseMapper = responseMapper;
  }
}
