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

import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperations;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.core.MediaType;

public class TestApiResponse {
  static SwaggerOperations swaggerOperations = SwaggerOperations.generate(ApiResponseAnnotation.class);

  @AfterAll
  public static void teardown() {
    swaggerOperations = null;
  }

  interface ApiResponseAnnotation {
    @ApiResponse(
        headers = {@Header(name = "k1", schema = @Schema(type = "integer", format = "int32")),
            @Header(name = "k2", schema = @Schema(type = "string"))},
        responseCode = "200",
        description = "")
    void testApiResponseHeader();

    @ApiResponse(
        headers = {@Header(name = "k1", schema = @Schema(type = "integer", format = "int32"))})
    void testResponseHeader();

    @ApiResponse(
        content = @Content(schema = @Schema(type = "integer", format = "int32")),
        responseCode = "200",
        description = "msg")
    void testSingle();

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(type = "integer", format = "int32")), description = "msg1"),
        @ApiResponse(responseCode = "301", content = @Content(schema = @Schema(type = "string")), description = "msg2")})
    void testMulti();
  }

  @Test
  public void checkResponseHeader() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testResponseHeader");
    Assertions.assertEquals("/testResponseHeader", swaggerOperation.getPath());

    io.swagger.v3.oas.models.responses.ApiResponse response = swaggerOperation.getOperation().getResponses().get("200");
    io.swagger.v3.oas.models.headers.Header property = response.getHeaders().get("k1");
    Assertions.assertEquals("integer", property.getSchema().getType());
    Assertions.assertEquals("int32", property.getSchema().getFormat());
  }

  @Test
  public void checkResponseDesc() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testMulti");
    Assertions.assertEquals("/testMulti", swaggerOperation.getPath());

    io.swagger.v3.oas.models.responses.ApiResponse response1 = swaggerOperation.getOperation().getResponses()
        .get("200");
    io.swagger.v3.oas.models.responses.ApiResponse response2 = swaggerOperation.getOperation().getResponses()
        .get("301");
    Assertions.assertEquals("msg1", response1.getDescription());
    Assertions.assertEquals("msg2", response2.getDescription());
  }

  @Test
  public void checkApiResponseHeader() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testApiResponseHeader");
    Assertions.assertEquals("/testApiResponseHeader", swaggerOperation.getPath());

    io.swagger.v3.oas.models.responses.ApiResponse response = swaggerOperation.getOperation().getResponses().get("200");
    io.swagger.v3.oas.models.headers.Header property = response.getHeaders().get("k1");
    Assertions.assertEquals("integer", property.getSchema().getType());
    Assertions.assertEquals("int32", property.getSchema().getFormat());

    property = response.getHeaders().get("k2");
    Assertions.assertEquals("string", property.getSchema().getType());
    Assertions.assertEquals("", property.getSchema().getFormat());
  }

  @Test
  public void checkSingle() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testSingle");
    Assertions.assertEquals("/testSingle", swaggerOperation.getPath());

    io.swagger.v3.oas.models.responses.ApiResponse response = swaggerOperation.getOperation().getResponses().get("200");
    Assertions.assertEquals("integer", response.getContent().get(MediaType.APPLICATION_JSON).getSchema().getType());
    Assertions.assertEquals("int32", response.getContent().get(MediaType.APPLICATION_JSON).getSchema().getFormat());
  }

  @Test
  public void checkMulti() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testMulti");
    Assertions.assertEquals("/testMulti", swaggerOperation.getPath());

    io.swagger.v3.oas.models.responses.ApiResponse response = swaggerOperation.getOperation().getResponses().get("200");
    Assertions.assertEquals("integer", response.getContent().get(MediaType.APPLICATION_JSON).getSchema().getType());
    Assertions.assertEquals("int32", response.getContent().get(MediaType.APPLICATION_JSON).getSchema().getFormat());

    response = swaggerOperation.getOperation().getResponses().get("301");
    Assertions.assertEquals("string", response.getContent().get(MediaType.APPLICATION_JSON).getSchema().getType());
    Assertions.assertEquals("", response.getContent().get(MediaType.APPLICATION_JSON).getSchema().getFormat());
  }
}
