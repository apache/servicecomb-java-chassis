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
package org.apache.servicecomb.swagger.generator.core.model;

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.swagger.models.Swagger;

public class TestSwaggerOperations {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void emptyOperationId() {
    Swagger swagger = SwaggerUtils.parseSwagger(this.getClass().getResource("/schemas/boolean.yaml"));
    swagger.getPaths().values().stream()
        .findFirst().get()
        .getPost().setOperationId("");

    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("OperationId can not be empty, path=/testboolean, httpMethod=POST.");

    new SwaggerOperations(swagger);
  }
}
