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

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.extend.annotations.RawJsonRequestBody;
import org.apache.servicecomb.swagger.generator.ParameterProcessor;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;

public class RawJsonRequestBodyProcessor implements ParameterProcessor<BodyParameter, RawJsonRequestBody> {
  @Override
  public Class<?> getProcessType() {
    return RawJsonRequestBody.class;
  }

  @Override
  public String getParameterName(RawJsonRequestBody rawJsonRequestBody) {
    if (StringUtils.isNotEmpty(rawJsonRequestBody.value())) {
      return rawJsonRequestBody.value();
    }
    if (StringUtils.isNotEmpty(rawJsonRequestBody.name())) {
      return rawJsonRequestBody.name();
    }
    return null;
  }

  @Override
  public HttpParameterType getHttpParameterType(RawJsonRequestBody parameterAnnotation) {
    return HttpParameterType.BODY;
  }

  @Override
  public void fillParameter(Swagger swagger, Operation operation, BodyParameter parameter, JavaType type,
      RawJsonRequestBody annotation) {
    parameter.setVendorExtension("x-raw-json", true);
    parameter.setRequired(annotation.required());
  }
}
