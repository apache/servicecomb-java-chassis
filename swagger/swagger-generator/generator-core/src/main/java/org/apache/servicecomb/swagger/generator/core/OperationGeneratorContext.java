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
package org.apache.servicecomb.swagger.generator.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.swagger.generator.SwaggerConst;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.v3.oas.models.media.Schema;

public class OperationGeneratorContext extends SwaggerGeneratorContext {
  private final Map<String, Schema<?>> responses = new HashMap<>();

  private JavaType responseType;

  private final Map<String, String> responseDescriptions = new HashMap<>();

  // statusCode:headerName:headerSchema
  private final Map<String, Map<String, Schema<?>>> responseHeaders = new HashMap<>();

  public OperationGeneratorContext(SwaggerGeneratorContext parent) {
    super(parent);
    updateResponse(SwaggerConst.SUCCESS_KEY, null);
  }

  public Map<String, Schema<?>> getResponses() {
    return responses;
  }

  public void updateResponse(String code, Schema<?> schema) {
    this.responses.put(code, schema);
  }

  public JavaType getResponseType() {
    return responseType;
  }

  public void setResponseType(JavaType responseType) {
    this.responseType = responseType;
  }

  public Map<String, String> getResponseDescriptions() {
    return responseDescriptions;
  }

  public void updateResponseDescription(String code, String description) {
    this.responseDescriptions.put(code, description);
  }

  public Map<String, Map<String, Schema<?>>> getResponseHeaders() {
    return responseHeaders;
  }

  public void updateResponseHeader(String code, String header, Schema<?> schema) {
    this.responseHeaders.computeIfAbsent(code, key -> new HashMap<>());
    this.responseHeaders.get(code).put(header, schema);
  }

  public void updateProduces() {
    List<String> removed = new ArrayList<>();
    for (String media : supportedProduces) {
      if (SUPPORTED_BODY_CONTENT_TYPE.contains(media)) {
        continue;
      }
      removed.add(media);
    }
    supportedProduces.removeAll(removed);
  }
}
