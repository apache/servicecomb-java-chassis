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
package org.apache.servicecomb.swagger.invocation.response.producer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.engine.SwaggerProducer;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.BeforeClass;
import org.junit.Test;

import io.swagger.annotations.ApiResponse;
import io.swagger.models.Swagger;

public class TestProducerResponseMapperFactorys {
  static class ResponseForTest {
    public String normal() {
      return "normal";
    }

    public CompletableFuture<String> async() {
      return CompletableFuture.completedFuture("async");
    }

    @ApiResponse(code = 200, message = "", response = String.class)
    public Response scbResponse() {
      return Response.ok("scb");
    }

    @ApiResponse(code = 200, message = "", response = String.class)
    public javax.ws.rs.core.Response jaxrsResponse() {
      return javax.ws.rs.core.Response.ok("jaxrs").build();
    }

    public Optional<String> optional() {
      return Optional.of("optional");
    }
  }

  static SwaggerEnvironment environment = new SwaggerEnvironment();

  static SwaggerProducer swaggerProducer;

  static ResponseForTest instance = new ResponseForTest();

  static String result = "abc";

  static Response response = Response.ok(result);

  @BeforeClass
  public static void setup() {
    Swagger swagger = SwaggerGenerator.generate(ResponseForTest.class);
    swaggerProducer = environment.createProducer(instance, swagger);
  }

  @Test
  public void should_mapper_to_normal_string() {
    SwaggerProducerOperation operation = swaggerProducer.findOperation("normal");
    assertThat(operation.getResponseMapper()).isInstanceOf(DefaultProducerResponseMapper.class);
    assertThat((String) operation.getResponseMapper().mapResponse(Status.OK, instance.normal()).getResult())
        .isEqualTo("normal");
  }

  @Test
  public void should_mapper_to_completableFuture_element_string() throws ExecutionException, InterruptedException {
    SwaggerProducerOperation operation = swaggerProducer.findOperation("async");
    assertThat(operation.getResponseMapper()).isInstanceOf(DefaultProducerResponseMapper.class);
    assertThat((String) operation.getResponseMapper().mapResponse(Status.OK, instance.async().get()).getResult())
        .isEqualTo("async");
  }

  @Test
  public void should_mapper_to_scbResponse_string() {
    SwaggerProducerOperation operation = swaggerProducer.findOperation("scbResponse");
    assertThat(operation.getResponseMapper().getClass().getName())
        .startsWith(CseResponseProducerResponseMapperFactory.class.getName());
    assertThat((String) operation.getResponseMapper().mapResponse(Status.OK, instance.scbResponse()).getResult())
        .isEqualTo("scb");
  }

  @Test
  public void should_mapper_to_optional_string() {
    SwaggerProducerOperation operation = swaggerProducer.findOperation("optional");
    assertThat(operation.getResponseMapper()).isInstanceOf(OptionalProducerResponseMapper.class);
    assertThat((String) operation.getResponseMapper().mapResponse(Status.OK, instance.optional()).getResult())
        .isEqualTo("optional");
  }
}
