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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.ResponseHeader;
import io.swagger.models.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestOperationGenerator {
  static SwaggerOperations swaggerOperations = SwaggerOperations.generate(TestClass.class);

  @AfterAll
  public static void teardown() {
    swaggerOperations = null;
  }

  @Api(tags = {"default0", "default1"})
  private static class TestClass {
    @ApiResponse(code = 200, message = "200 is ok............", response = String.class,
        responseHeaders = @ResponseHeader(name = "x-user-domain", response = String.class))
    @ApiOperation(value = "value1", tags = {"tag1", "tag2"},
        responseHeaders = {@ResponseHeader(name = "x-user-name", response = String.class),
            @ResponseHeader(name = "x-user-id", response = String.class)},
        extensions = {
            @Extension(name = "x-class-name", properties = {@ExtensionProperty(value = "value", name = "key")})})
    public void responseThenApiOperation() {
    }

    @ApiOperation(value = "value1", tags = {"tag1", "tag2"},
        responseHeaders = {@ResponseHeader(name = "x-user-name", response = String.class),
            @ResponseHeader(name = "x-user-id", response = String.class)},
        extensions = {
            @Extension(name = "x-class-name", properties = {@ExtensionProperty(value = "value", name = "key")})})
    @ApiResponse(code = 200, message = "200 is ok............", response = String.class,
        responseHeaders = @ResponseHeader(name = "x-user-domain", response = String.class))
    public void apiOperationThenResponse() {
    }

    @ApiOperation(value = "value2")
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

    Response response = swaggerOperation.getOperation().getResponses().get("200");
    Assertions.assertEquals("200 is ok............", response.getDescription());
    Assertions.assertNull(response.getHeaders().get("x-user-domain"));
    Assertions.assertNotNull(response.getHeaders().get("x-user-name"));
    Assertions.assertNotNull(swaggerOperation.getOperation().getVendorExtensions().get("x-class-name"));
  }

  @Test
  public void apiOperationThenResponse() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("apiOperationThenResponse");
    List<String> tags = swaggerOperation.getOperation().getTags();
    MatcherAssert.assertThat(tags, contains("tag1", "tag2"));

    Response response = swaggerOperation.getOperation().getResponses().get("200");
    Assertions.assertEquals("200 is ok............", response.getDescription());
    Assertions.assertNull(response.getHeaders().get("x-user-domain"));
    Assertions.assertNotNull(response.getHeaders().get("x-user-name"));
    Assertions.assertNotNull(swaggerOperation.getOperation().getVendorExtensions().get("x-class-name"));
  }
}
