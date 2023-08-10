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

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.ParameterGenerator;
import org.apache.servicecomb.swagger.generator.ParameterTypeProcessor;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.model.HttpParameterType;

import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.core.MediaType;

public class PartParameterTypeProcessor implements ParameterTypeProcessor {
  @Override
  public Type getProcessType() {
    return TypeFactory.defaultInstance().constructType(Part.class);
  }

  @Override
  public void process(SwaggerGenerator swaggerGenerator, OperationGenerator operationGenerator,
      ParameterGenerator parameterGenerator) {
    if (!parameterGenerator.getParameterGeneratorContext().getSupportedConsumes()
        .contains(MediaType.MULTIPART_FORM_DATA)) {
      throw new IllegalArgumentException("Part type must declare consumes " + MediaType.MULTIPART_FORM_DATA);
    }
    if (parameterGenerator.getHttpParameterType() != HttpParameterType.FORM) {
      throw new IllegalArgumentException("Part type must declare as form parameter.");
    }
    if (StringUtils.isEmpty(parameterGenerator.getParameterGeneratorContext().getParameterName())) {
      throw new IllegalArgumentException("Name is required for Part parameter.");
    }
    parameterGenerator.getParameterGeneratorContext().updateConsumes(List.of(MediaType.MULTIPART_FORM_DATA));

    formParameterSchema(parameterGenerator);
  }

  protected void formParameterSchema(ParameterGenerator parameterGenerator) {
    parameterGenerator.getParameterGeneratorContext().setSchema(new FileSchema());
  }
}
