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
package org.apache.servicecomb.swagger.invocation.springmvc.response;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.engine.SwaggerConsumerOperation;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.invocation.Response;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import io.swagger.models.Swagger;

public class TestSpringmvcConsumerResponseMapper {
  interface ConsumerResponseForTest {
    ResponseEntity<String> responseEntity();

    CompletableFuture<ResponseEntity<String>> asyncResponseEntity();
  }

  SwaggerEnvironment environment = new SwaggerEnvironment();

  SwaggerConsumer swaggerConsumer;

  String result = "abc";

  Response response = Response.ok(result);

  @Before
  public void setup() {
    Swagger swagger = SwaggerGenerator.generate(ConsumerResponseForTest.class);
    swaggerConsumer = environment.createConsumer(ConsumerResponseForTest.class, swagger);
  }

  @Test
  public void responseEntity() {
    SwaggerConsumerOperation operation = swaggerConsumer.findOperation("responseEntity");

    @SuppressWarnings("unchecked")
    ResponseEntity<String> responseEntity = (ResponseEntity<String>) operation.getResponseMapper()
        .mapResponse(response);
    Assert.assertEquals(result, responseEntity.getBody());
    Assert.assertTrue(responseEntity.getHeaders().isEmpty());
  }

  @Test
  public void responseEntityWithHeader() {
    SwaggerConsumerOperation operation = swaggerConsumer.findOperation("responseEntity");
    response.addHeader("h", "v");

    @SuppressWarnings("unchecked")
    ResponseEntity<String> responseEntity = (ResponseEntity<String>) operation.getResponseMapper()
        .mapResponse(response);
    Assert.assertEquals(result, responseEntity.getBody());
    Assert.assertEquals(1, responseEntity.getHeaders().size());
    Assert.assertThat(responseEntity.getHeaders().get("h"), Matchers.contains("v"));
  }

  @Test
  public void asyncResponseEntity() {
    SwaggerConsumerOperation operation = swaggerConsumer.findOperation("asyncResponseEntity");

    @SuppressWarnings("unchecked")
    ResponseEntity<String> responseEntity = (ResponseEntity<String>) operation.getResponseMapper()
        .mapResponse(response);
    Assert.assertEquals(result, responseEntity.getBody());
    Assert.assertTrue(responseEntity.getHeaders().isEmpty());
  }

  @Test
  public void asyncResponseEntityWithHeader() {
    SwaggerConsumerOperation operation = swaggerConsumer.findOperation("asyncResponseEntity");
    response.addHeader("h", "v1").addHeader("h", "v2");
    response.addHeader("h1", null);

    @SuppressWarnings("unchecked")
    ResponseEntity<String> responseEntity = (ResponseEntity<String>) operation.getResponseMapper()
        .mapResponse(response);
    Assert.assertEquals(result, responseEntity.getBody());
    Assert.assertEquals(1, responseEntity.getHeaders().size());
    Assert.assertThat(responseEntity.getHeaders().get("h"), Matchers.contains("v1", "v2"));
  }
}
