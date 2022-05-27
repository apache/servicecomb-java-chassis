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
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.ParameterProcessor;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;

public abstract class AbstractSerializableParameterProcessor<SWAGGER_PARAMETER extends AbstractSerializableParameter<?>, ANNOTATION> implements
    ParameterProcessor<SWAGGER_PARAMETER, ANNOTATION> {
  @Override
  public void fillParameter(Swagger swagger, Operation operation, SWAGGER_PARAMETER parameter, JavaType type,
      ANNOTATION annotation) {
    SwaggerUtils.setParameterType(swagger, type, parameter);

    String defaultValue = readDefaultValue(annotation);
    if (StringUtils.isNotEmpty(defaultValue)) {
      parameter.setDefaultValue(defaultValue);
    }
  }

  protected String readDefaultValue(ANNOTATION annotation) {
    return "";
  }
}
