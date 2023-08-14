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

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.ParameterGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ValueConstants;

public class RequestHeaderAnnotationProcessor extends
    SpringmvcParameterAnnotationsProcessor<RequestHeader> {
  @Override
  public Type getProcessType() {
    return RequestHeader.class;
  }

  @Override
  public String getParameterName(RequestHeader annotation) {
    String value = annotation.value();
    if (value.isEmpty()) {
      value = annotation.name();
    }
    if (StringUtils.isNotEmpty(value)) {
      return value;
    }
    return null;
  }

  @Override
  public void process(SwaggerGenerator swaggerGenerator, OperationGenerator operationGenerator,
      ParameterGenerator parameterGenerator, RequestHeader annotation) {
    parameterGenerator.setHttpParameterType(HttpParameterType.HEADER);
    if (StringUtils.isNotEmpty(getParameterName(annotation))) {
      parameterGenerator.getParameterGeneratorContext().setParameterName(getParameterName(annotation));
    }
    parameterGenerator.getParameterGeneratorContext().setRequired(annotation.required());
    if (!ValueConstants.DEFAULT_NONE.equals(annotation.defaultValue())) {
      parameterGenerator.getParameterGeneratorContext()
          .setDefaultValue(annotation.defaultValue());
      // if default value is set, must be required false.
      parameterGenerator.getParameterGeneratorContext().setRequired(false);
    }
  }
}
