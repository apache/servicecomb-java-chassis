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
package org.apache.servicecomb.swagger.generator;

import java.lang.reflect.Type;

import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

public interface ParameterProcessor<ANNOTATION> {
  Type getProcessType();

  default JavaType getProcessJavaType() {
    return TypeFactory.defaultInstance().constructType(getProcessType());
  }

  String getParameterName(ANNOTATION parameterAnnotation);

  default Type getGenericType(ANNOTATION parameterAnnotation) {
    return null;
  }

  HttpParameterType getHttpParameterType(ANNOTATION parameterAnnotation);

  void fillParameter(OpenAPI swagger, Operation operation, Parameter parameter, JavaType type,
      ANNOTATION annotation);

  default void fillParameter(OpenAPI swagger, Operation operation, Parameter parameter, Type type,
      ANNOTATION annotation) {
    fillParameter(swagger, operation, parameter, TypeFactory.defaultInstance().constructType(type), annotation);
  }

  void fillRequestBody(OpenAPI swagger, Operation operation, RequestBody parameter, JavaType type,
      ANNOTATION annotation);

  default void fillRequestBody(OpenAPI swagger, Operation operation, RequestBody parameter, Type type,
      ANNOTATION annotation) {
    fillRequestBody(swagger, operation, parameter, TypeFactory.defaultInstance().constructType(type), annotation);
  }
}
