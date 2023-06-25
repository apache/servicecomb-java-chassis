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

import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.engine.SwaggerConsumerOperation;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

public class TestJaxrsConsumerResponseMapper {
  @Path("/")
  interface ConsumerResponseForTest {
    @ApiResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(type = "string")))
    @Path("/jaxrsResponse")
    @GET
    jakarta.ws.rs.core.Response jaxrsResponse();
  }

  SwaggerEnvironment environment = new SwaggerEnvironment();

  SwaggerConsumer swaggerConsumer;

  String result = "abc";

  org.apache.servicecomb.swagger.invocation.Response response = org.apache.servicecomb.swagger.invocation.Response
      .ok(result);

  @BeforeEach
  public void setup() {
    OpenAPI swagger = SwaggerGenerator.generate(ConsumerResponseForTest.class);
    swaggerConsumer = environment.createConsumer(ConsumerResponseForTest.class, swagger);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void jaxrsResponse() {
    SwaggerConsumerOperation operation = swaggerConsumer.findOperation("jaxrsResponse");

    Response jaxrsResponse = (Response) operation.getResponseMapper().mapResponse(response);
    Assertions.assertEquals(result, jaxrsResponse.getEntity());
    Assertions.assertTrue(jaxrsResponse.getHeaders().isEmpty());
  }

  @Test
  public void jaxrsResponseWithHeaders() {
    SwaggerConsumerOperation operation = swaggerConsumer.findOperation("jaxrsResponse");
    response.addHeader("h", "v1").addHeader("h", "v2").addHeader("h", null);
    response.addHeader("h1", null);

    Response jaxrsResponse = (Response) operation.getResponseMapper().mapResponse(response);
    Assertions.assertEquals(result, jaxrsResponse.getEntity());
    Assertions.assertEquals(1, jaxrsResponse.getHeaders().size());
    MatcherAssert.assertThat(jaxrsResponse.getHeaders().get("h"), Matchers.contains("v1", "v2"));
  }
}
