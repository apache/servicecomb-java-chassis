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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import java.util.Arrays;

import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperations;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponses;
import jakarta.ws.rs.core.MediaType;

public class TestApiOperation {
  static SwaggerOperations swaggerOperations = SwaggerOperations.generate(ApiOperationAnnotation.class);

  @AfterAll
  public static void teardown() {
    swaggerOperations = null;
  }

  interface ApiOperationAnnotation {
    @Operation(
        summary = "summary",
        description = "notes",
        tags = {"tag1", "tag2"},
        method = "GET",
        operationId = "test",
        requestBody = @RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON)),
        responses = @ApiResponse(responseCode = "202",
            content = @Content(mediaType = MediaType.APPLICATION_JSON),
            headers = @Header(name = "h1", schema = @Schema(name = "integer"))),
        extensions = {@Extension(
            name = "x-tagA",
            properties = {@ExtensionProperty(name = "x-tagAExt", value = "value of tagAExt")})})
    void testBase();

    @Operation(summary = "aaa")
    @ApiResponse(responseCode = "202", content = @Content(schema = @Schema(name = "integer")))
    int testPrimitive();

    @Operation(summary = "aaa", hidden = true)
    int testHidden();
  }


  @Test
  public void testApiOperation() {
    OpenAPI swagger = swaggerOperations.getSwagger();
    testBase(swagger.getPaths().get("/test"));
    testPrimitive(swagger.getPaths().get("/testPrimitive"));
    MatcherAssert.assertThat(swagger.getPaths().get("/testHidden"), is(nullValue()));
  }

  private void testPrimitive(PathItem path) {
    io.swagger.v3.oas.models.Operation operation = path.getPost();

    Assertions.assertEquals(2, operation.getResponses().size());

    io.swagger.v3.oas.models.media.Schema result200 =
        operation.getResponses().get("200").getContent().get(MediaType.APPLICATION_JSON).getSchema();
    Assertions.assertEquals("integer", result200.getType());
    Assertions.assertEquals("int32", result200.getFormat());

    io.swagger.v3.oas.models.media.Schema result202 =
        operation.getResponses().get("202").getContent().get(MediaType.APPLICATION_JSON).getSchema();
    Assertions.assertEquals("string", result202.getType());
    Assertions.assertNull(result202.getFormat());
  }

  private void testBase(PathItem path) {
    Assertions.assertEquals(1, path.readOperations().size());

    io.swagger.v3.oas.models.Operation operation = path.getGet();

    Assertions.assertEquals("summary", operation.getSummary());
    Assertions.assertEquals("notes", operation.getDescription());
    Assertions.assertEquals(Arrays.asList("tag1", "tag2"), operation.getTags());
    Assertions.assertEquals(Arrays.asList("application/json"),
        operation.getResponses().getDefault().getContent().keySet().iterator().next());
    Assertions.assertEquals(Arrays.asList("application/json"),
        operation.getRequestBody().getContent().keySet().iterator().next());

    ApiResponses responseMap = operation.getResponses();
    Assertions.assertEquals(2, responseMap.size());

    io.swagger.v3.oas.models.responses.ApiResponse response = responseMap.get(SwaggerConst.SUCCESS_KEY);
    Assertions.assertNotNull(response);
    Assertions.assertNull(response.getContent().get(MediaType.APPLICATION_JSON).getSchema().getAdditionalProperties());

    response = responseMap.get("202");
    Assertions.assertNotNull(response);
    Assertions.assertNull(response.getContent().get(MediaType.APPLICATION_JSON).getSchema().getAdditionalProperties());

    Assertions.assertEquals(1, response.getHeaders().size());
    Assertions.assertEquals("integer", response.getHeaders().get("h1").getSchema().getType());
  }
}
