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

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

public class RequestParamAnnotationProcessor extends
    AbstractSpringmvcSerializableParameterProcessor<RequestParam> {
  @Override
  public Type getProcessType() {
    return RequestParam.class;
  }

  @Override
  public String getParameterName(RequestParam annotation) {
    return annotation.value();
  }

  @Override
  public HttpParameterType getHttpParameterType(RequestParam parameterAnnotation) {
    return HttpParameterType.QUERY;
  }

  @Override
  public void fillParameter(OpenAPI swagger, Operation operation, Parameter queryParameter, JavaType type,
      RequestParam requestParam) {
    Schema schema = SwaggerUtils.resolveTypeSchemas(swagger, type);
    queryParameter.setSchema(schema);
    queryParameter.setRequired(requestParam.required());
    if (!ValueConstants.DEFAULT_NONE.equals(requestParam.defaultValue())) {
      schema.setDefault(requestParam.defaultValue());
    }
  }

  @Override
  public void fillRequestBody(OpenAPI swagger, Operation operation, RequestBody requestBody, String parameterName,
      JavaType type, RequestParam requestParam) {

  }
}
