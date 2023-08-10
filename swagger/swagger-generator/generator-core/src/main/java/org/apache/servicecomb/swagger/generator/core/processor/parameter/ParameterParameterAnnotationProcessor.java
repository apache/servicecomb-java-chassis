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
import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.ParameterGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerParameterAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;

import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

public class ParameterParameterAnnotationProcessor
    extends SwaggerParameterAnnotationProcessor<io.swagger.v3.oas.annotations.Parameter> {
  @Override
  public Class<?> getProcessType() {
    return io.swagger.v3.oas.annotations.Parameter.class;
  }

  @Override
  public void process(SwaggerGenerator swaggerGenerator, OperationGenerator operationGenerator,
      ParameterGenerator parameterGenerator, io.swagger.v3.oas.annotations.Parameter annotation) {
    if (annotation.schema() != null
        && annotation.schema().implementation() != null
        && annotation.schema().implementation() != Void.class) {
      parameterGenerator.getParameterGeneratorContext()
          .setParameterType(TypeFactory.defaultInstance().constructType(annotation.schema().implementation()));
    }

    if (StringUtils.isNotEmpty(annotation.name())) {
      parameterGenerator.getParameterGeneratorContext().setParameterName(annotation.name());
    }

    if (annotation.in() != null && annotation.in() != ParameterIn.DEFAULT) {
      parameterGenerator.setHttpParameterType(HttpParameterType.from(annotation.in()));
    }

    if (Explode.TRUE.equals(annotation.explode())) {
      parameterGenerator.getParameterGeneratorContext().setExplode(true);
    }
    if (Explode.FALSE.equals(annotation.explode())) {
      parameterGenerator.getParameterGeneratorContext().setExplode(false);
    }
    parameterGenerator.getParameterGeneratorContext().setRequired(annotation.required());
  }
}
