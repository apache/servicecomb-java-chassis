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
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Map;

import org.apache.servicecomb.swagger.generator.core.unittest.SwaggerGeneratorForTest;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.apache.servicecomb.swagger.generator.pojo.PojoSwaggerGeneratorContext;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.ResponseHeader;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;

public class TestApiOperation {
  SwaggerGeneratorContext context = new PojoSwaggerGeneratorContext();

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
    SwaggerGenerator swaggerGenerator =
        new SwaggerGeneratorForTest(context, ApiOperationAnnotation.class);
    swaggerGenerator.generate();

    Swagger swagger = swaggerGenerator.getSwagger();
    testBase(swagger.getPath("/test"));
    testPrimitive(swagger.getPath("/testPrimitive"));
    testMap(swagger.getPath("/testMap"));
    testList(swagger.getPath("/testList"));
    testSet(swagger.getPath("/testSet"));
    assertThat(swagger.getPath("/testHidden"), is(nullValue()));
  }

  @Test
  public void testUnknown() {
    UnitTestSwaggerUtils.testException(
        "generate operation swagger failed, org.apache.servicecomb.swagger.generator.core.TestApiOperation$UnknownResponseContainer:testUnknown",
        "not support responseContainer xxx",
        context,
        UnknownResponseContainer.class);
  }

  private void testSet(Path path) {
    Operation operation = path.getPost();
    Property result200 = operation.getResponses().get("200").getSchema();
    Assert.assertEquals(ArrayProperty.class, result200.getClass());
    Assert.assertEquals(true, ((ArrayProperty) result200).getUniqueItems());
  }

  private void testList(Path path) {
    Operation operation = path.getPost();
    Property result200 = operation.getResponses().get("200").getSchema();
    Assert.assertEquals(ArrayProperty.class, result200.getClass());
    Assert.assertEquals(null, ((ArrayProperty) result200).getUniqueItems());
  }

  private void testMap(Path path) {
    Operation operation = path.getPost();
    Property result200 = operation.getResponses().get("200").getSchema();
    Assert.assertEquals(MapProperty.class, result200.getClass());
  }

  private void testPrimitive(Path path) {
    Operation operation = path.getPost();

    Assert.assertEquals(2, operation.getResponses().size());

    Property result200 = operation.getResponses().get("200").getSchema();
    Assert.assertEquals("integer", result200.getType());
    Assert.assertEquals("int32", result200.getFormat());

    Property result202 = operation.getResponses().get("202").getSchema();
    Assert.assertEquals("string", result202.getType());
    Assert.assertEquals(null, result202.getFormat());
  }

  private void testBase(Path path) {
    Assert.assertEquals(1, path.getOperations().size());

    Operation operation = path.getGet();

    Assert.assertEquals("summary", operation.getSummary());
    Assert.assertEquals("notes", operation.getDescription());
    Assert.assertEquals(Arrays.asList("tag1", "tag2"), operation.getTags());
    Assert.assertEquals(Arrays.asList("application/json"), operation.getProduces());
    Assert.assertEquals(Arrays.asList("application/json"), operation.getConsumes());
    Assert.assertEquals(Arrays.asList(Scheme.HTTP, Scheme.HTTPS), operation.getSchemes());

    Map<String, Response> responseMap = operation.getResponses();
    Assert.assertEquals(2, responseMap.size());

    Response response = responseMap.get(SwaggerConst.SUCCESS_KEY);
    Assert.assertNotNull(response);
    Assert.assertEquals(null, response.getSchema());

    response = responseMap.get("202");
    Assert.assertNotNull(response);
    Assert.assertEquals(null, response.getSchema());

    Assert.assertEquals(1, response.getHeaders().size());
    Assert.assertEquals("integer", response.getHeaders().get("h1").getType());
  }
}
