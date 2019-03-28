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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import io.swagger.models.ModelImpl;
import io.swagger.models.Response;
import io.swagger.models.properties.Property;

public class TestApiResponse {
  static SwaggerOperations swaggerOperations = SwaggerOperations.generate(ApiResponseAnnotation.class);

  @AfterClass
  public static void teardown() {
    swaggerOperations = null;
  }

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
  public void checkResponseHeader() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testResponseHeader");
    Assert.assertEquals("/testResponseHeader", swaggerOperation.getPath());

    Response response = swaggerOperation.getOperation().getResponses().get("200");
    Property property = response.getHeaders().get("k1");
    Assert.assertEquals("integer", property.getType());
    Assert.assertEquals("int32", property.getFormat());
  }

  @Test
  public void checkResponseDesc() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testMulti");
    Assert.assertEquals("/testMulti", swaggerOperation.getPath());

    Response response1 = swaggerOperation.getOperation().getResponses().get("200");
    Response response2 = swaggerOperation.getOperation().getResponses().get("301");
    Assert.assertEquals("msg1", response1.getDescription());
    Assert.assertEquals("msg2", response2.getDescription());
  }

  @Test
  public void checkApiResponseHeader() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testApiResponseHeader");
    Assert.assertEquals("/testApiResponseHeader", swaggerOperation.getPath());

    Response response = swaggerOperation.getOperation().getResponses().get("200");
    Property property = response.getHeaders().get("k1");
    Assert.assertEquals("integer", property.getType());
    Assert.assertEquals("int32", property.getFormat());

    property = response.getHeaders().get("k2");
    Assert.assertEquals("string", property.getType());
    Assert.assertNull(property.getFormat());
  }

  @Test
  public void checkSingle() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testSingle");
    Assert.assertEquals("/testSingle", swaggerOperation.getPath());

    Response response = swaggerOperation.getOperation().getResponses().get("200");
    Assert.assertEquals("integer", ((ModelImpl) response.getResponseSchema()).getType());
    Assert.assertEquals("int32", ((ModelImpl) response.getResponseSchema()).getFormat());
  }

  @Test
  public void checkMulti() {
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testMulti");
    Assert.assertEquals("/testMulti", swaggerOperation.getPath());

    Response response = swaggerOperation.getOperation().getResponses().get("200");
    Assert.assertEquals("integer", ((ModelImpl) response.getResponseSchema()).getType());
    Assert.assertEquals("int32", ((ModelImpl) response.getResponseSchema()).getFormat());

    response = swaggerOperation.getOperation().getResponses().get("301");
    Assert.assertEquals("string", ((ModelImpl) response.getResponseSchema()).getType());
    Assert.assertNull(((ModelImpl) response.getResponseSchema()).getFormat());
  }
}
