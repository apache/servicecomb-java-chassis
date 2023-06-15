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

import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Map;

import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperations;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MediaType;


public class OperationMethodAnnotationProcessorTest {
  static SwaggerOperations swaggerOperations = SwaggerOperations.generate(TestClass.class);

  @AfterAll
  public static void teardown() {
    swaggerOperations = null;
  }

  private static class TestClass {
    @Operation(summary = "value1", tags = {"tag1", "tag2"})
    public void function() {
    }

    @Operation(summary = "value2")
    public void functionWithNoTag() {
    }

    @Operation(summary = "testSingleMediaType",
        responses = {@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_XML))},
        requestBody = @RequestBody(content = @Content(mediaType = MediaType.TEXT_PLAIN)))
    public String testSingleMediaType(String input) {
      return input;
    }

    @Operation(summary = "testMultiMediaType",
        responses = {
            @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON + "," + MediaType.TEXT_PLAIN))},
        requestBody = @RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON + ","
            + MediaType.APPLICATION_XML)))
    public String testMultiMediaType(String input) {
      return input;
    }

    @Operation(summary = "testBlankMediaType",
        responses = {@ApiResponse(content = @Content(mediaType = ""))},
        requestBody = @RequestBody(content = @Content(mediaType = "")))
    public String testBlankMediaType(String input) {
      return input;
    }

    @Operation(summary = "testBodyParam")
    public String testBodyParam(@RequestBody TestBodyBean user) {
      return user.toString();
    }
  }


  private static class TestBodyBean {

    @NotBlank
    private String age;

    @NotNull
    private String name;

    @NotEmpty
    private String sexes;

    public String getAge() {
      return age;
    }

    public void setAge(String age) {
      this.age = age;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getSexes() {
      return sexes;
    }

    public void setSexes(String sexes) {
      this.sexes = sexes;
    }
  }

  @Test
  public void testConvertTags() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("function");
    MatcherAssert.assertThat(swaggerOperation.getOperation().getTags(), containsInAnyOrder("tag1", "tag2"));
  }

  @Test
  public void testConvertTagsOnMethodWithNoTag() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("functionWithNoTag");
    Assertions.assertNull(swaggerOperation.getOperation().getTags());
  }

  @Test
  public void testMediaType() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testSingleMediaType");
    MatcherAssert.assertThat(swaggerOperation.getOperation().getRequestBody().getContent().get("input"),
        Matchers.equalTo(MediaType.TEXT_PLAIN));
    MatcherAssert.assertThat(swaggerOperation.getOperation().getResponses().getDefault().getContent().get(""),
        Matchers.equalTo(MediaType.APPLICATION_XML));

    swaggerOperation = swaggerOperations.findOperation("testMultiMediaType");
    MatcherAssert.assertThat(swaggerOperation.getOperation().getRequestBody().getContent().keySet(),
        Matchers.contains(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
    MatcherAssert.assertThat(swaggerOperation.getOperation().getResponses().getDefault().getContent().keySet(),
        Matchers.contains(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML));

    swaggerOperation = swaggerOperations.findOperation("testBlankMediaType");
    Assertions.assertNull(swaggerOperation.getOperation().getRequestBody().getContent().keySet());
    Assertions.assertNull(swaggerOperation.getOperation().getResponses().getDefault().getContent().keySet());
  }


  @Test
  public void testBodyParam() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testBodyParam");
    Schema schema = swaggerOperation.getSwagger()
        .getPaths().get("/testBodyParam").getPost().getRequestBody().getContent()
        .get(MediaType.APPLICATION_JSON).getSchema();
    Assertions.assertEquals(Components.COMPONENTS_SCHEMAS_REF + "TestBodyBean", schema.get$ref());
    schema = swaggerOperation.getSwagger().getComponents().getSchemas().get("TestBodyBean");
    Map<String, Schema> properties = schema.getProperties();

    // swagger new version do not support primitive types(stinrg, integer, etc...) to using NotBlank, NotEmpty, ...)
//    Assertions.assertTrue(properties.get("age").getNullable(), "Support NotBlank annotation");
//    Assertions.assertTrue(properties.get("sexes").getNullable(), "Support NotEmpty annotation");
//    Assertions.assertTrue(properties.get("name").getNullable(), "Original support NotNull annotation");
    Assertions.assertEquals(properties.get("age").getType(), "string");
    Assertions.assertEquals(properties.get("sexes").getType(), "string");
    Assertions.assertEquals(properties.get("name").getType(), "string");
  }
}
