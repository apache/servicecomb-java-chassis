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
package org.apache.servicecomb.swagger.invocation.response.consumer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.engine.SwaggerConsumerOperation;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.models.OpenAPI;

public class TestConsumerResponseMapperFactorys {
  interface ConsumerResponseForTest {
    String normal();

    CompletableFuture<String> async();

    @ApiResponse(responseCode = "200", description = "", content =
        {@Content(schema = @Schema(implementation = String.class))})
    Response scbResponse();

    @ApiResponse(responseCode = "200", description = "", content =
        {@Content(schema = @Schema(implementation = String.class))})
    jakarta.ws.rs.core.Response jaxrsResponse();

    Optional<String> optional();
  }

  SwaggerEnvironment environment = new SwaggerEnvironment();

  SwaggerConsumer swaggerConsumer;

  String result = "abc";

  Response response = Response.ok(result);

  @Before
  public void setup() {
    OpenAPI swagger = SwaggerGenerator.generate(ConsumerResponseForTest.class);
    swaggerConsumer = environment.createConsumer(ConsumerResponseForTest.class, swagger);
  }

  @Test
  public void should_mapper_to_normal_string() {
    SwaggerConsumerOperation operation = swaggerConsumer.findOperation("normal");
    assertThat(operation.getResponseMapper()).isInstanceOf(DefaultConsumerResponseMapper.class);
    Assertions.assertEquals(result, operation.getResponseMapper().mapResponse(response));
  }

  @Test
  public void should_mapper_to_completableFuture_element_string() {
    SwaggerConsumerOperation operation = swaggerConsumer.findOperation("async");
    assertThat(operation.getResponseMapper()).isInstanceOf(DefaultConsumerResponseMapper.class);
    Assertions.assertEquals(result, operation.getResponseMapper().mapResponse(response));
  }

  @Test
  public void should_mapper_to_scbResponse_string() {
    SwaggerConsumerOperation operation = swaggerConsumer.findOperation("scbResponse");
    assertThat(operation.getResponseMapper().getClass().getName())
        .startsWith(CseResponseConsumerResponseMapperFactory.class.getName());
    Response scbResponse = (Response) operation.getResponseMapper().mapResponse(response);
    Assertions.assertEquals(result, scbResponse.getResult());
  }

  @Test
  public void should_mapper_to_optional_string() {
    SwaggerConsumerOperation operation = swaggerConsumer.findOperation("optional");
    assertThat(operation.getResponseMapper()).isInstanceOf(OptionalConsumerResponseMapper.class);
    @SuppressWarnings("unchecked")
    Optional<String> optional = (Optional<String>) operation.getResponseMapper().mapResponse(response);
    Assertions.assertEquals(result, optional.get());
  }
}
