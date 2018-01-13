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

package org.apache.servicecomb.swagger.generator.springmvc.processor.annotation;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.models.Operation;

public class RequestMappingMethodAnnotationProcessor extends AbstractHttpMethodMappingAnnotationProcessor {

  @Override
  public void process(Object annotation, OperationGenerator operationGenerator) {
    RequestMapping requestMapping = (RequestMapping) annotation;
    Operation operation = operationGenerator.getOperation();

    // path/value是等同的
    this.processPath(requestMapping.path(), operationGenerator);
    this.processPath(requestMapping.value(), operationGenerator);
    this.processMethod(requestMapping.method(), operationGenerator);

    this.processConsumes(requestMapping.consumes(), operation);
    this.processProduces(requestMapping.produces(), operation);

    if (StringUtils.isEmpty(operationGenerator.getHttpMethod())
        && StringUtils.isEmpty(operationGenerator.getSwaggerGenerator().getHttpMethod())) {
      throw new Error("HttpMethod must not both be empty in class and method");
    }
  }

  private void processMethod(RequestMethod[] requestMethods, OperationGenerator operationGenerator) {
    if (null == requestMethods || requestMethods.length == 0) {
      return;
    }

    if (requestMethods.length > 1) {
      throw new Error(
          String.format("not allowed multi http method for %s:%s",
              operationGenerator.getProviderMethod().getDeclaringClass().getName(),
              operationGenerator.getProviderMethod().getName()));
    }

    super.processMethod(requestMethods[0], operationGenerator);
  }
}
