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

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.generator.MethodAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

public class ApiOperationProcessor implements MethodAnnotationProcessor<Operation> {
  public Type getProcessType() {
    return Operation.class;
  }

  @Override
  public void process(SwaggerGenerator swaggerGenerator,
      OperationGenerator operationGenerator, Operation apiOperationAnnotation) {
    io.swagger.v3.oas.models.Operation operation = operationGenerator.getOperation();

    operationGenerator.setHttpMethod(apiOperationAnnotation.method());

    if (!StringUtils.isEmpty(apiOperationAnnotation.summary())) {
      operation.setSummary(apiOperationAnnotation.summary());
    }

    if (!StringUtils.isEmpty(apiOperationAnnotation.description())) {
      operation.setDescription(apiOperationAnnotation.description());
    }

    operation.setOperationId(apiOperationAnnotation.operationId());
    if (operation.getExtensions() == null) {
      operation.setExtensions(AnnotationUtils.extensionsModel(apiOperationAnnotation.extensions()));
    } else {
      operation.getExtensions().putAll(AnnotationUtils.extensionsModel(apiOperationAnnotation.extensions()));
    }

    operation.setRequestBody(AnnotationUtils.requestBodyModel(apiOperationAnnotation.requestBody()));

    convertTags(apiOperationAnnotation.tags(), operation);

    parseResponses(operationGenerator, apiOperationAnnotation);
  }

  private void parseResponses(OperationGenerator operationGenerator, Operation apiOperationAnnotation) {
    if (apiOperationAnnotation.responses() != null && apiOperationAnnotation.responses().length > 0) {
      for (ApiResponse apiResponse : apiOperationAnnotation.responses()) {
        if (operationGenerator.getOperation().getResponses() == null) {
          operationGenerator.getOperation().setResponses(new ApiResponses());
        }
        operationGenerator.getOperation().getResponses().addApiResponse(
            AnnotationUtils.responseCodeModel(apiResponse), AnnotationUtils.apiResponseModel(apiResponse));
      }
    }
  }

  private void convertTags(String[] tags, io.swagger.v3.oas.models.Operation operation) {
    if (tags == null || tags.length == 0) {
      return;
    }

    for (String tag : tags) {
      if (StringUtils.isEmpty(tag)) {
        continue;
      }

      operation.addTagsItem(tag);
    }
  }
}
