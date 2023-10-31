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
package org.apache.servicecomb.swagger.generator.springmvc;

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.springmvc.model.Generic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.models.OpenAPI;
import jakarta.ws.rs.core.MediaType;

public class TestTwoSameNameModels {
  private static final String SCHEMA_CONTENT = """
      openapi: 3.0.1
      info:
        title: swagger definition for org.apache.servicecomb.swagger.generator.springmvc.TestTwoSameNameModels$TwoSameModelService
        version: 1.0.0
      servers:
      - url: /TwoSameModelService
      paths:
        /sameGeneric:
          post:
            operationId: genericService
            requestBody:
              content:
                application/json:
                  schema:
                    $ref: '#/components/schemas/GenericSameModel'
              required: true
              x-name: param
            responses:
              "200":
                description: response of 200
                content:
                  application/json:
                    schema:
                      $ref: '#/components/schemas/GenericSameModel'
        /same:
          post:
            operationId: service
            requestBody:
              content:
                application/json:
                  schema:
                    $ref: '#/components/schemas/SameModel'
              required: true
              x-name: param
            responses:
              "200":
                description: response of 200
                content:
                  application/json:
                    schema:
                      $ref: '#/components/schemas/SameModel'
      components:
        schemas:
          GenericSameModel:
            type: object
            properties:
              data:
                $ref: '#/components/schemas/SameModel'
            x-java-class: org.apache.servicecomb.swagger.generator.springmvc.model.Generic<org.apache.servicecomb.swagger.generator.springmvc.model.same1.SameModel>
          SameModel:
            type: object
            properties:
              name:
                type: string
            x-java-class: org.apache.servicecomb.swagger.generator.springmvc.model.same1.SameModel
      """;

  interface TwoSameModelService {
    @RequestMapping(
        path = "/same",
        method = {RequestMethod.POST},
        consumes = {MediaType.APPLICATION_JSON},
        produces = {MediaType.APPLICATION_JSON})
    org.apache.servicecomb.swagger.generator.springmvc.model.same1.SameModel service
        (@RequestBody org.apache.servicecomb.swagger.generator.springmvc.model.same1.SameModel param);

    @RequestMapping(
        path = "/sameGeneric",
        method = {RequestMethod.POST},
        consumes = {MediaType.APPLICATION_JSON},
        produces = {MediaType.APPLICATION_JSON})
    Generic<org.apache.servicecomb.swagger.generator.springmvc.model.same1.SameModel> genericService
        (@RequestBody Generic<org.apache.servicecomb.swagger.generator.springmvc.model.same1.SameModel> param);
  }

  interface SameModelService {
    @RequestMapping(
        path = "/same",
        method = {RequestMethod.POST},
        consumes = {MediaType.APPLICATION_JSON},
        produces = {MediaType.APPLICATION_JSON})
    org.apache.servicecomb.swagger.generator.springmvc.model.same1.SameModel service
        (@RequestBody org.apache.servicecomb.swagger.generator.springmvc.model.same2.SameModel param);
  }

  interface SameModelThrowService {
    @RequestMapping(
        path = "/same",
        method = {RequestMethod.POST},
        consumes = {MediaType.APPLICATION_JSON},
        produces = {MediaType.APPLICATION_JSON})
    org.apache.servicecomb.swagger.generator.springmvc.model.same1.SameModelThrow service
        (@RequestBody org.apache.servicecomb.swagger.generator.springmvc.model.same2.SameModelThrow param);
  }

  @Test
  public void testTwoSameModelWork() {
    SwaggerGenerator generator = SwaggerGenerator.create(TwoSameModelService.class);
    OpenAPI openAPI = generator.generate();
    Assertions.assertEquals(SCHEMA_CONTENT, SwaggerUtils.swaggerToString(openAPI));
  }

  @Test
  public void testSameModelThrowThrowException() {
    SwaggerGenerator generator = SwaggerGenerator.create(SameModelThrowService.class);
    Assertions.assertThrows(IllegalStateException.class, () -> generator.generate());
  }

  @Test
  public void testSameModelThrowException() {
    SwaggerGenerator generator = SwaggerGenerator.create(SameModelService.class);
    Assertions.assertThrows(IllegalStateException.class, () -> generator.generate());
  }
}
