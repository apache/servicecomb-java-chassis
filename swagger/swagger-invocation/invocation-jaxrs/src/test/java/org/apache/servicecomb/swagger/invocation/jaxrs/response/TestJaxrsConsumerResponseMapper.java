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
package org.apache.servicecomb.swagger.invocation.jaxrs.response;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.engine.SwaggerConsumerOperation;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.swagger.annotations.ApiResponse;
import io.swagger.models.Swagger;

public class TestJaxrsConsumerResponseMapper {
  @Path("/")
  interface ConsumerResponseForTest {
    @ApiResponse(code = 200, message = "", response = String.class)
    @Path("/jaxrsResponse")
    @GET
    javax.ws.rs.core.Response jaxrsResponse();
  }

  SwaggerEnvironment environment = new SwaggerEnvironment();

  SwaggerConsumer swaggerConsumer;

  String result = "abc";

  org.apache.servicecomb.swagger.invocation.Response response = org.apache.servicecomb.swagger.invocation.Response
      .ok(result);

  @Before
  public void setup() {
    Swagger swagger = SwaggerGenerator.generate(ConsumerResponseForTest.class);
    swaggerConsumer = environment.createConsumer(ConsumerResponseForTest.class, swagger);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void jaxrsResponse() {
    SwaggerConsumerOperation operation = swaggerConsumer.findOperation("jaxrsResponse");

    Response jaxrsResponse = (Response) operation.getResponseMapper().mapResponse(response);
    Assert.assertEquals(result, jaxrsResponse.getEntity());
    Assert.assertTrue(jaxrsResponse.getHeaders().isEmpty());
  }

  @Test
  public void jaxrsResponseWithHeaders() {
    SwaggerConsumerOperation operation = swaggerConsumer.findOperation("jaxrsResponse");
    response.addHeader("h", "v1").addHeader("h", "v2").addHeader("h", null);
    response.addHeader("h1", null);

    Response jaxrsResponse = (Response) operation.getResponseMapper().mapResponse(response);
    Assert.assertEquals(result, jaxrsResponse.getEntity());
    Assert.assertEquals(1, jaxrsResponse.getHeaders().size());
    Assert.assertThat(jaxrsResponse.getHeaders().get("h"), Matchers.contains("v1", "v2"));
  }
}
