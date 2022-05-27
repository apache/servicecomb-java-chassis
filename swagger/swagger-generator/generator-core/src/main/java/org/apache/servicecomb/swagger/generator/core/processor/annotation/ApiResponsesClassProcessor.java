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

import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.findClassAnnotationProcessor;

import java.lang.reflect.Type;

import org.apache.servicecomb.swagger.generator.ClassAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public class ApiResponsesClassProcessor implements ClassAnnotationProcessor<ApiResponses> {
  @Override
  public Type getProcessType() {
    return ApiResponses.class;
  }

  @Override
  public void process(SwaggerGenerator swaggerGenerator, ApiResponses apiResponses) {
    ClassAnnotationProcessor<ApiResponse> processor = findClassAnnotationProcessor(ApiResponse.class);
    for (ApiResponse apiResponse : apiResponses.value()) {
      processor.process(swaggerGenerator, apiResponse);
    }
  }
}
