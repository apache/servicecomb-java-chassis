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
import java.util.Map;

import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperations;
import org.hamcrest.MatcherAssert;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.ResponseHeader;
import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestApiOperation {
  static SwaggerOperations swaggerOperations = SwaggerOperations.generate(ApiOperationAnnotation.class);

  @AfterAll
  public static void teardown() {
    swaggerOperations = null;
  }

  interface ApiOperationAnnotation {
    @ApiOperation(
        value = "summary",
        notes = "notes",
        tags = {"tag1", "tag2"},
        httpMethod = "GET",
        nickname = "test",
        produces = "application/json",
        consumes = "application/json",
        protocols = "http,https",
        code = 202,
        responseHeaders = {@ResponseHeader(name = "h1", response = int.class)},
        extensions = {@Extension(
            name = "x-tagA",
            properties = {@ExtensionProperty(name = "x-tagAExt", value = "value of tagAExt")})})
    void testBase();

    @ApiOperation(value = "aaa", code = 202, response = String.class)
    int testPrimitive();

    @ApiOperation(value = "aaa", response = Integer.class, responseContainer = "Map")
    int testMap();

    @ApiOperation(value = "aaa", response = Integer.class, responseContainer = "List")
    int testList();

    @ApiOperation(value = "aaa", response = Integer.class, responseContainer = "Set")
    int testSet();

    @ApiOperation(value = "aaa", hidden = true)
    int testHidden();
  }

  interface UnknownResponseContainer {
    @ApiOperation(value = "aaa", response = Integer.class, responseContainer = "xxx")
    int testUnknown();
  }

  @Test
  public void testApiOperation() {
    Swagger swagger = swaggerOperations.getSwagger();
    testBase(swagger.getPath("/test"));
    testPrimitive(swagger.getPath("/testPrimitive"));
    testMap(swagger.getPath("/testMap"));
    testList(swagger.getPath("/testList"));
    testSet(swagger.getPath("/testSet"));
    MatcherAssert.assertThat(swagger.getPath("/testHidden"), is(nullValue()));
  }

  @Test
  public void testUnknown() {
    UnitTestSwaggerUtils.testException(
        "generate swagger operation failed, method=org.apache.servicecomb.swagger.generator.core.TestApiOperation$UnknownResponseContainer:testUnknown.",
        "not support responseContainer xxx",
        UnknownResponseContainer.class);
  }

  private void testSet(Path path) {
    Operation operation = path.getPost();
    Model result200 = operation.getResponses().get("200").getResponseSchema();
    Assertions.assertEquals(ArrayModel.class, result200.getClass());
    Assertions.assertEquals(true, ((ArrayModel) result200).getUniqueItems());
  }

  private void testList(Path path) {
    Operation operation = path.getPost();
    Model result200 = operation.getResponses().get("200").getResponseSchema();
    Assertions.assertEquals(ArrayModel.class, result200.getClass());
    Assertions.assertNull(((ArrayModel) result200).getUniqueItems());
  }

  private void testMap(Path path) {
    Operation operation = path.getPost();
    Model result200 = operation.getResponses().get("200").getResponseSchema();
    Assertions.assertEquals(ModelImpl.class, result200.getClass());
    Assertions.assertNotNull(((ModelImpl) result200).getAdditionalProperties());
  }

  private void testPrimitive(Path path) {
    Operation operation = path.getPost();

    Assertions.assertEquals(2, operation.getResponses().size());

    ModelImpl result200 = (ModelImpl) operation.getResponses().get("200").getResponseSchema();
    Assertions.assertEquals("integer", result200.getType());
    Assertions.assertEquals("int32", result200.getFormat());

    ModelImpl result202 = (ModelImpl) operation.getResponses().get("202").getResponseSchema();
    Assertions.assertEquals("string", result202.getType());
    Assertions.assertNull(result202.getFormat());
  }

  private void testBase(Path path) {
    Assertions.assertEquals(1, path.getOperations().size());

    Operation operation = path.getGet();

    Assertions.assertEquals("summary", operation.getSummary());
    Assertions.assertEquals("notes", operation.getDescription());
    Assertions.assertEquals(Arrays.asList("tag1", "tag2"), operation.getTags());
    Assertions.assertEquals(Arrays.asList("application/json"), operation.getProduces());
    Assertions.assertEquals(Arrays.asList("application/json"), operation.getConsumes());
    Assertions.assertEquals(Arrays.asList(Scheme.HTTP, Scheme.HTTPS), operation.getSchemes());

    Map<String, Response> responseMap = operation.getResponses();
    Assertions.assertEquals(2, responseMap.size());

    Response response = responseMap.get(SwaggerConst.SUCCESS_KEY);
    Assertions.assertNotNull(response);
    Assertions.assertNull(response.getResponseSchema());

    response = responseMap.get("202");
    Assertions.assertNotNull(response);
    Assertions.assertNull(response.getResponseSchema());

    Assertions.assertEquals(1, response.getHeaders().size());
    Assertions.assertEquals("integer", response.getHeaders().get("h1").getType());
  }
}
