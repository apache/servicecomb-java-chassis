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
package org.apache.servicecomb.swagger.generator.core.processor.parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.apache.servicecomb.swagger.generator.ParameterProcessor;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JavaType;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;

public class JsonViewProcessor implements ParameterProcessor<Parameter, Annotation> {

  @Override
  public Type getProcessType() {
    return JsonView.class;
  }

  @Override
  public String getParameterName(Annotation parameterAnnotation) {
    return null;
  }

  @Override
  public HttpParameterType getHttpParameterType(Annotation parameterAnnotation) {
    return null;
  }

  @Override
  public void fillParameter(Swagger swagger, Operation operation, Parameter parameter, JavaType type,
      Annotation annotation) {
    if (!(annotation instanceof JsonView)) {
      throw new IllegalArgumentException(
          "JsonViewProcessor only support process parameter with annotation @JsonView");
    }
    Class<?>[] jvValue = ((JsonView) annotation).value();
    if (jvValue.length != 1) {
      throw new IllegalArgumentException(
          "@JsonView only supported for exactly 1 class argument ");
    }
    parameter.getVendorExtensions().put(SwaggerConst.EXT_JSON_VIEW, jvValue[0].getName());
  }
}