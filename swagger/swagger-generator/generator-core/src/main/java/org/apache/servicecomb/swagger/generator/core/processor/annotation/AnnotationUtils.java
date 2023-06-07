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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.servicecomb.swagger.generator.core.processor.annotation.models.ResponseConfig;
import org.apache.servicecomb.swagger.generator.core.processor.annotation.models.ResponseHeaderConfig;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Schema;

public final class AnnotationUtils {
  private AnnotationUtils() {

  }

  public static void appendDefinition(OpenAPI swagger, Map<String, Schema> newDefinitions) {
    if (newDefinitions.isEmpty()) {
      return;
    }

    Map<String, Schema> definitions = swagger.getComponents().getSchemas();
    if (definitions == null) {
      definitions = new LinkedHashMap<>();
      swagger.getComponents().schemas(definitions);
    }

    definitions.putAll(newDefinitions);
  }

  private static ResponseConfig convert(io.swagger.v3.oas.annotations.Operation apiOperation) {
    // TODO: should convert?
    return null;
  }

  private static ResponseConfig convert(ApiResponse apiResponse) {
    // TODO: should convert?
    return null;
  }

  public static ResponseHeaderConfig convert(Header responseHeader) {
    // TODO: should convert?
    return null;
  }

  public static void addResponse(OpenAPI swagger, Operation operation
      , io.swagger.v3.oas.annotations.Operation apiOperation) {
    ResponseConfig responseConfig = convert(apiOperation);
    generateResponse(swagger, responseConfig);
  }

  public static void addResponse(OpenAPI swagger, ApiResponse apiResponse) {
    ResponseConfig responseConfig = convert(apiResponse);
    generateResponse(swagger, responseConfig);
  }

  public static void addResponse(OpenAPI swagger, Operation operation, ApiResponse apiResponse) {
    ResponseConfig responseConfig = convert(apiResponse);
    generateResponse(swagger, responseConfig);
  }

  private static void generateResponse(OpenAPI swagger, ResponseConfig responseConfig) {
    // TODO: generate response
  }
}
