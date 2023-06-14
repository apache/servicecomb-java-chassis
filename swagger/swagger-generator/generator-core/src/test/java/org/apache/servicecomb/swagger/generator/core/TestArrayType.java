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
import org.apache.servicecomb.swagger.generator.core.schema.ArrayType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import jakarta.ws.rs.core.MediaType;

public class TestArrayType {
  @Test
  public void test() {
    SwaggerOperations swaggerOperations = SwaggerOperations.generate(ArrayType.class);
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testBytes");
    RequestBody bodyParameter = swaggerOperation.getOperation().getRequestBody();
    Schema model = bodyParameter.getContent().get(MediaType.APPLICATION_JSON).getSchema();

    Assertions.assertEquals(Components.COMPONENTS_SCHEMAS_REF + "testBytesBody", model.get$ref());
    OpenAPI openAPI = swaggerOperation.getSwagger();
    Schema schema = openAPI.getComponents().getSchemas().get("testBytesBody");
    Assertions.assertEquals(1, schema.getProperties().size());

    ArraySchema arrayProperty = (ArraySchema) schema.getProperties().get("value");
    ByteArraySchema byteArrayProperty = (ByteArraySchema) arrayProperty.getItems();
    Assertions.assertEquals("string", byteArrayProperty.getType());
    Assertions.assertEquals("byte", byteArrayProperty.getFormat());
  }
}
