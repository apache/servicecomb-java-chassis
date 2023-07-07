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

import java.lang.reflect.Type;

import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;
import org.springframework.web.bind.annotation.RequestAttribute;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

public class RequestAttributeAnnotationProcessor extends
    AbstractSpringmvcSerializableParameterProcessor<RequestAttribute> {
  @Override
  public Type getProcessType() {
    return RequestAttribute.class;
  }

  @Override
  public String getParameterName(RequestAttribute annotation) {
    throw new IllegalStateException("RequestAttribute is not supported by open api.");
  }

  @Override
  public HttpParameterType getHttpParameterType(RequestAttribute parameterAnnotation) {
    throw new IllegalStateException("RequestAttribute is not supported by open api.");
  }

  @Override
  public void fillParameter(OpenAPI swagger, Operation operation, Parameter parameter, JavaType type,
      RequestAttribute requestAttribute) {
    throw new IllegalStateException("RequestAttribute is not supported by open api.");
  }

  @Override
  public void fillRequestBody(OpenAPI swagger, Operation operation, RequestBody requestBody, JavaType type,
      RequestAttribute requestAttribute) {
    throw new IllegalStateException("RequestAttribute is not supported by open api.");
  }
}
