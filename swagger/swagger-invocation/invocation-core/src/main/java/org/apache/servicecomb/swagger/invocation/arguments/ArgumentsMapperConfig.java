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

package org.apache.servicecomb.swagger.invocation.arguments;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;

import io.swagger.models.Operation;

public class ArgumentsMapperConfig {
  // input
  private Method swaggerMethod;

  private Method providerMethod;

  private Operation swaggerOperation;

  private SwaggerGeneratorContext swaggerGeneratorContext;

  // output
  private List<ArgumentMapper> argumentMapperList = new ArrayList<>();

  public Method getSwaggerMethod() {
    return swaggerMethod;
  }

  public void setSwaggerMethod(Method swaggerMethod) {
    this.swaggerMethod = swaggerMethod;
  }

  public Method getProviderMethod() {
    return providerMethod;
  }

  public void setProviderMethod(Method providerMethod) {
    this.providerMethod = providerMethod;
  }

  public Operation getSwaggerOperation() {
    return swaggerOperation;
  }

  public void setSwaggerOperation(Operation swaggerOperation) {
    this.swaggerOperation = swaggerOperation;
  }

  public SwaggerGeneratorContext getSwaggerGeneratorContext() {
    return swaggerGeneratorContext;
  }

  public void setSwaggerGeneratorContext(
      SwaggerGeneratorContext swaggerGeneratorContext) {
    this.swaggerGeneratorContext = swaggerGeneratorContext;
  }

  public List<ArgumentMapper> getArgumentMapperList() {
    return argumentMapperList;
  }

  public void setArgumentMapperList(List<ArgumentMapper> argumentMapperList) {
    this.argumentMapperList = argumentMapperList;
  }

  public void addArgumentMapper(ArgumentMapper argumentMapper) {
    argumentMapperList.add(argumentMapper);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ArgumentsMapperConfig{");
    sb.append("swaggerMethod=").append(swaggerMethod);
    sb.append(", providerMethod=").append(providerMethod);
    sb.append(", swaggerOperation=").append(swaggerOperation);
    sb.append(", swaggerGeneratorContext=").append(swaggerGeneratorContext);
    sb.append(", argumentMapperList=").append(argumentMapperList);
    sb.append('}');
    return sb.toString();
  }
}
