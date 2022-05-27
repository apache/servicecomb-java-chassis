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
import org.apache.servicecomb.swagger.generator.core.processor.parameter.AbstractSerializableParameterProcessor;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ValueConstants;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;

public abstract class AbstractSpringmvcSerializableParameterProcessor<SWAGGER_PARAMETER extends AbstractSerializableParameter<?>, ANNOTATION> extends
    AbstractSerializableParameterProcessor<SWAGGER_PARAMETER, ANNOTATION> {
  @Override
  public void fillParameter(Swagger swagger, Operation operation, SWAGGER_PARAMETER parameter, JavaType type,
      ANNOTATION annotation) {
    super.fillParameter(swagger, operation, parameter, type, annotation);

    Object defaultValue = parameter.getDefaultValue();
    if (!ObjectUtils.isEmpty(defaultValue)) {
      parameter.setRequired(false);
      return;
    }
    parameter.setRequired(readRequired(annotation));
  }

  protected abstract boolean readRequired(ANNOTATION annotation);

  @Override
  protected String readDefaultValue(ANNOTATION annotation) {
    String defaultValue = pureReadDefaultValue(annotation);
    if (StringUtils.isEmpty(defaultValue) || defaultValue.equals(ValueConstants.DEFAULT_NONE)) {
      return "";
    }
    return defaultValue;
  }

  protected abstract String pureReadDefaultValue(ANNOTATION annotation);
}
