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
import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.ParameterGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerParameterAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;

public class RawJsonRequestBodyProcessor extends
    SwaggerParameterAnnotationProcessor<RawJsonRequestBody> {
  @Override
  public Class<?> getProcessType() {
    return RawJsonRequestBody.class;
  }

  @Override
  public String getParameterName(RawJsonRequestBody annotation) {
    if (StringUtils.isNotEmpty(annotation.value())) {
      return annotation.value();
    } else if (StringUtils.isNotEmpty(annotation.name())) {
      return annotation.name();
    }
    return null;
  }

  @Override
  public void process(SwaggerGenerator swaggerGenerator, OperationGenerator operationGenerator,
      ParameterGenerator parameterGenerator, RawJsonRequestBody annotation) {
    parameterGenerator.setHttpParameterType(HttpParameterType.BODY);

    if (StringUtils.isNotEmpty(getParameterName(annotation))) {
      parameterGenerator.getParameterGeneratorContext().setParameterName(getParameterName(annotation));
    }

    parameterGenerator.getParameterGeneratorContext().setRequired(annotation.required());
    parameterGenerator.getParameterGeneratorContext().setRawJson(true);
  }
}
