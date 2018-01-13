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

import org.apache.servicecomb.swagger.converter.ConverterMgr;
import org.apache.servicecomb.swagger.generator.core.unittest.SwaggerGeneratorForTest;
import org.apache.servicecomb.swagger.generator.pojo.PojoSwaggerGeneratorContext;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;

public class TestApiResponse {
  SwaggerGeneratorContext context = new PojoSwaggerGeneratorContext();

  interface ApiResponseAnnotation {
    @ApiResponse(
        responseHeaders = {@ResponseHeader(name = "k1", response = int.class),
            @ResponseHeader(name = "k2", response = String.class)},
        code = 200,
        message = "")
    void testApiResponseHeader();

    @ResponseHeader(name = "k1", response = int.class)
    void testResponseHeader();

    @ApiResponse(
        code = 200,
        response = int.class,
        message = "msg")
    void testSingle();

    @ApiResponses({@ApiResponse(code = 200, response = int.class, message = "msg1"),
        @ApiResponse(code = 301, response = String.class, message = "msg2")})
    void testMulti();
  }

  @Test
  public void testBody() {
    SwaggerGenerator swaggerGenerator =
        new SwaggerGeneratorForTest(context, ApiResponseAnnotation.class);
    swaggerGenerator.generate();

    checkResponseDesc(swaggerGenerator);
    checkApiResponseHeader(swaggerGenerator);
    checkResponseHeader(swaggerGenerator);
    checkSingle(swaggerGenerator);
    checkMulti(swaggerGenerator);
  }

  private void checkResponseHeader(SwaggerGenerator generator) {
    Swagger swagger = generator.getSwagger();

    Path path = swagger.getPaths().get("/testResponseHeader");
    Operation operation = path.getOperations().get(0);
    Assert.assertEquals("testResponseHeader", operation.getOperationId());

    Response response = operation.getResponses().get("200");
    Property property = response.getHeaders().get("k1");
    Assert.assertEquals(Integer.class, ConverterMgr.findJavaType(generator, property).getRawClass());
  }

  private void checkResponseDesc(SwaggerGenerator generator) {
    Swagger swagger = generator.getSwagger();

    Path path = swagger.getPaths().get("/testMulti");
    Operation operation = path.getOperations().get(0);

    Response response1 = operation.getResponses().get("200");
    Response response2 = operation.getResponses().get("301");
    Assert.assertEquals("msg1", response1.getDescription());
    Assert.assertEquals("msg2", response2.getDescription());
  }

  private void checkApiResponseHeader(SwaggerGenerator generator) {
    Swagger swagger = generator.getSwagger();

    Path path = swagger.getPaths().get("/testApiResponseHeader");
    Operation operation = path.getOperations().get(0);
    Assert.assertEquals("testApiResponseHeader", operation.getOperationId());

    Response response = operation.getResponses().get("200");
    Property property = response.getHeaders().get("k1");
    Assert.assertEquals(Integer.class, ConverterMgr.findJavaType(generator, property).getRawClass());

    property = response.getHeaders().get("k2");
    Assert.assertEquals(String.class, ConverterMgr.findJavaType(generator, property).getRawClass());
  }

  public void checkSingle(SwaggerGenerator generator) {
    Swagger swagger = generator.getSwagger();

    Path path = swagger.getPaths().get("/testSingle");
    Operation operation = path.getOperations().get(0);
    Assert.assertEquals("testSingle", operation.getOperationId());

    Response response = operation.getResponses().get("200");
    Assert.assertEquals(Integer.class, ConverterMgr.findJavaType(generator, response.getSchema()).getRawClass());
  }

  public void checkMulti(SwaggerGenerator generator) {
    Swagger swagger = generator.getSwagger();

    Path path = swagger.getPaths().get("/testMulti");

    Operation operation = path.getOperations().get(0);
    Assert.assertEquals("testMulti", operation.getOperationId());

    Response response = operation.getResponses().get("200");
    Assert.assertEquals(Integer.class, ConverterMgr.findJavaType(generator, response.getSchema()).getRawClass());

    response = operation.getResponses().get("301");
    Assert.assertEquals(String.class, ConverterMgr.findJavaType(generator, response.getSchema()).getRawClass());
  }
}
