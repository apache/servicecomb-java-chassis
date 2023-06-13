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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponses;

@SuppressWarnings("rawtypes")
public final class AnnotationUtils {
  private AnnotationUtils() {

  }

  public static void appendDefinition(OpenAPI swagger,
      Map<String, io.swagger.v3.oas.models.media.Schema> newDefinitions) {
    if (newDefinitions.isEmpty()) {
      return;
    }

    Map<String, io.swagger.v3.oas.models.media.Schema> definitions = swagger.getComponents().getSchemas();
    if (definitions == null) {
      definitions = new LinkedHashMap<>();
      swagger.getComponents().schemas(definitions);
    }

    definitions.putAll(newDefinitions);
  }

  public static void addResponse(OpenAPI swagger, io.swagger.v3.oas.models.Operation operation
      , Operation apiOperation) {
    // TODO: should convert?
    return;
  }

  public static void addResponse(OpenAPI swagger,
      io.swagger.v3.oas.models.Operation operation, ApiResponse apiResponse) {
    if (operation.getResponses() == null) {
      operation.setResponses(new ApiResponses());
    }
    operation.getResponses().addApiResponse(responseCodeModel(apiResponse), apiResponseModel(apiResponse));
  }

  private static String responseCodeModel(ApiResponse apiResponse) {
    if (StringUtils.isEmpty(apiResponse.responseCode()) || "default".equals(apiResponse.responseCode())) {
      return "200";
    }
    return apiResponse.responseCode();
  }

  public static io.swagger.v3.oas.models.responses.ApiResponse apiResponseModel(ApiResponse apiResponse) {
    io.swagger.v3.oas.models.responses.ApiResponse result =
        new io.swagger.v3.oas.models.responses.ApiResponse();
    result.setDescription(apiResponse.description());
    result.setContent(contentModel(apiResponse.content()));
    result.setHeaders(headersModel(apiResponse.headers()));
    return result;
  }

  public static Map<String, io.swagger.v3.oas.models.headers.Header> headersModel(Header[] headers) {
    Map<String, io.swagger.v3.oas.models.headers.Header> result = new HashMap<>();
    for (Header header : headers) {
      io.swagger.v3.oas.models.headers.Header model =
          new io.swagger.v3.oas.models.headers.Header();
      model.setDescription(header.description());
      model.setSchema(schemaModel(header.schema()));
      result.put(header.name(), model);
    }
    return result;
  }

  public static io.swagger.v3.oas.models.media.Content contentModel(Content[] contents) {
    io.swagger.v3.oas.models.media.Content result = new io.swagger.v3.oas.models.media.Content();
    for (io.swagger.v3.oas.annotations.media.Content content : contents) {
      MediaType mediaType = new MediaType();
      mediaType.setExample(content.examples());
      mediaType.setSchema(schemaModel(content.schema()));
      result.addMediaType(mediaTypeModel(content), mediaType);
    }
    return result;
  }

  public static io.swagger.v3.oas.models.parameters.RequestBody requestBodyModel(RequestBody requestBody) {
    if (requestBody == null) {
      return null;
    }
    io.swagger.v3.oas.models.parameters.RequestBody result = new io.swagger.v3.oas.models.parameters.RequestBody();
    result.setContent(AnnotationUtils.contentModel(requestBody.content()));
    return result;
  }

  private static String mediaTypeModel(io.swagger.v3.oas.annotations.media.Content content) {
    if (StringUtils.isEmpty(content.mediaType())) {
      return jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
    }
    return content.mediaType();
  }

  public static io.swagger.v3.oas.models.media.Schema schemaModel(Schema schema) {
    io.swagger.v3.oas.models.media.Schema result =
        new io.swagger.v3.oas.models.media.Schema();
    result.setDescription(schema.description());
    result.setType(schema.type());
    result.setFormat(schema.format());
    return result;
  }
}
