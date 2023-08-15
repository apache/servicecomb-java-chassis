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
package org.apache.servicecomb.swagger.generator.core.processor.annotation;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.MethodAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.springframework.util.CollectionUtils;

import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public class ApiResponseMethodProcessor implements MethodAnnotationProcessor<ApiResponse> {
  @Override
  public Type getProcessType() {
    return ApiResponse.class;
  }

  @Override
  public void process(SwaggerGenerator swaggerGenerator, OperationGenerator operationGenerator,
      ApiResponse apiResponse) {
    List<String> produces = new ArrayList<>();
    String responseCode = apiResponse.responseCode();
    if (StringUtils.isEmpty(responseCode) || "default".equals(responseCode)) {
      responseCode = SwaggerConst.SUCCESS_KEY;
    }
    Class<?> type = null;
    for (Content content : apiResponse.content()) {
      if (StringUtils.isNotEmpty(content.mediaType())) {
        produces.add(content.mediaType());
      }
      if (content.schema() != null && content.schema().implementation() != Void.class) {
        type = content.schema().implementation();
      }
    }
    operationGenerator.getOperationGeneratorContext().updateResponse(responseCode,
        type == null ? null : SwaggerUtils.resolveTypeSchemas(swaggerGenerator.getOpenAPI(), type));
    if (StringUtils.isNotEmpty(apiResponse.description())) {
      operationGenerator.getOperationGeneratorContext().updateResponseDescription(responseCode,
          apiResponse.description());
    }
    for (Header header : apiResponse.headers()) {
      if (header.schema() == null || header.schema().implementation() == Void.class) {
        throw new IllegalArgumentException("@ApiResponse header schema implementation must be defined.");
      }
      if (StringUtils.isEmpty(header.name())) {
        throw new IllegalArgumentException("@ApiResponse header name must be defined.");
      }
      operationGenerator.getOperationGeneratorContext().updateResponseHeader(responseCode,
          header.name(), AnnotationUtils.schemaModel(swaggerGenerator.getOpenAPI(), header.schema()));
    }
    if (!CollectionUtils.isEmpty(produces)) {
      operationGenerator.getOperationGeneratorContext().updateProduces(produces);
    }
  }
}
