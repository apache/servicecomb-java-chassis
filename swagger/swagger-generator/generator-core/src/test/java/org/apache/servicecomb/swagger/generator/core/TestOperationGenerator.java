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

import static org.hamcrest.Matchers.contains;

import java.util.List;

import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperations;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

public class TestOperationGenerator {
  static SwaggerOperations swaggerOperations = SwaggerOperations.generate(TestClass.class);

  @AfterAll
  public static void teardown() {
    swaggerOperations = null;
  }

  @OpenAPIDefinition(tags = {@Tag(name = "default0"), @Tag(name = "default1")})
  private static class TestClass {
    @ApiResponse(responseCode = "200", description = "200 is ok............",
        content = @Content(mediaType = "application/json", schema = @Schema(name = "String")),
        headers = @Header(name = "x-user-domain", schema = @Schema(name = "String")))
    @Operation(operationId = "value1", tags = {"tag1", "tag2"},
        responses = {
            @ApiResponse(headers = @Header(name = "x-user-name", schema = @Schema(name = "String"))),
            @ApiResponse(headers = @Header(name = "x-user-id", schema = @Schema(name = "String")))},
        extensions = {
            @Extension(name = "x-class-name", properties = @ExtensionProperty(value = "value", name = "key"))})
    public void responseThenApiOperation() {
    }

    @Operation(operationId = "value1", tags = {"tag1", "tag2"},
        responses = {@ApiResponse(headers = {
            @Header(name = "x-user-name", schema = @Schema(name = "String")),
            @Header(name = "x-user-id", schema = @Schema(name = "String"))})},
        extensions = {
            @Extension(name = "x-class-name", properties = {
                @ExtensionProperty(value = "value", name = "key")})})
    @ApiResponse(responseCode = "200", description = "200 is ok............",
        content = @Content(mediaType = "application/json", schema = @Schema(name = "String")),
        headers = @Header(name = "x-user-domain", schema = @Schema(name = "String")))
    public void apiOperationThenResponse() {
    }

    @Operation(operationId = "value2")
    public void apiOperationNoTag() {
    }

    public void noApiOperation() {
    }
  }

  @Test
  public void apiOperationNoTag() {
    SwaggerOperation operation = swaggerOperations.findOperation("apiOperationNoTag");
    List<String> tags = operation.getOperation().getTags();
    MatcherAssert.assertThat(tags, contains("default0", "default1"));
    Assertions.assertEquals("value2", operation.getOperation().getSummary());
  }

  @Test
  public void noApiOperation() {
    SwaggerOperation operation = swaggerOperations.findOperation("noApiOperation");
    List<String> tags = operation.getOperation().getTags();
    MatcherAssert.assertThat(tags, contains("default0", "default1"));
    Assertions.assertNull(operation.getOperation().getSummary());
  }

  @Test
  public void responseThenApiOperation() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("responseThenApiOperation");
    List<String> tags = swaggerOperation.getOperation().getTags();
    MatcherAssert.assertThat(tags, contains("tag1", "tag2"));

    io.swagger.v3.oas.models.responses.ApiResponse response = swaggerOperation.getOperation().getResponses().get("200");
    Assertions.assertEquals("200 is ok............", response.getDescription());
    Assertions.assertNull(response.getHeaders().get("x-user-domain"));
    Assertions.assertNotNull(response.getHeaders().get("x-user-name"));
    Assertions.assertNotNull(swaggerOperation.getOperation().getExtensions().get("x-class-name"));
  }

  @Test
  public void apiOperationThenResponse() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("apiOperationThenResponse");
    List<String> tags = swaggerOperation.getOperation().getTags();
    MatcherAssert.assertThat(tags, contains("tag1", "tag2"));

    io.swagger.v3.oas.models.responses.ApiResponse response = swaggerOperation.getOperation().getResponses().get("200");
    Assertions.assertEquals("200 is ok............", response.getDescription());
    Assertions.assertNull(response.getHeaders().get("x-user-domain"));
    Assertions.assertNotNull(response.getHeaders().get("x-user-name"));
    Assertions.assertNotNull(swaggerOperation.getOperation().getExtensions().get("x-class-name"));
  }
}
