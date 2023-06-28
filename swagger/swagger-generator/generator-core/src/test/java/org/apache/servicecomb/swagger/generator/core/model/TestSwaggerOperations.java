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
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.pojo.PojoExample1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.RequestBody;

public class TestSwaggerOperations {

  @Test
  public void emptyOperationId() {
    OpenAPI swagger = SwaggerUtils.parseSwagger(this.getClass().getResource("/schemas/boolean.yaml"));
    swagger.getPaths().values().stream()
        .findFirst().get()
        .getPost().setOperationId("");

    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
        () -> new SwaggerOperations(swagger));
    Assertions.assertEquals("OperationId can not be empty, path=/testboolean, httpMethod=POST.",
        exception.getMessage());
  }

  @Test
  public void testPojoExample1() {
    SwaggerOperations swaggerOperations = SwaggerOperations.generate(PojoExample1.class);
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testMultiParameter");
    Assertions.assertEquals(null, swaggerOperation.getOperation().getParameters());
    RequestBody requestBody = swaggerOperation.getOperation().getRequestBody();
    Assertions.assertEquals(2,
        SwaggerUtils.getSchema(swaggerOperation.getSwagger(),
            requestBody.getContent().get(SwaggerConst.DEFAULT_MEDIA_TYPE).getSchema()).getProperties().size());
  }
}
