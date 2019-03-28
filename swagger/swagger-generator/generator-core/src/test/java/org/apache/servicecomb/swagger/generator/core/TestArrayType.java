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

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.core.schema.ArrayType;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperations;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.models.ModelImpl;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.properties.ByteArrayProperty;

public class TestArrayType {
  @Test
  public void test() {
    SwaggerOperations swaggerOperations = SwaggerOperations.generate(ArrayType.class);
    SwaggerOperation swaggerOperation = swaggerOperations.findOperation("testBytes");
    BodyParameter bodyParameter = (BodyParameter) swaggerOperation.getOperation().getParameters().get(0);
    ModelImpl model = SwaggerUtils.getModelImpl(swaggerOperations.getSwagger(), bodyParameter);

    Assert.assertEquals(ModelImpl.OBJECT, model.getType());
    Assert.assertEquals(1, model.getProperties().size());

    ByteArrayProperty byteArrayProperty = (ByteArrayProperty) model.getProperties().get("value");
    Assert.assertEquals("string", byteArrayProperty.getType());
    Assert.assertEquals("byte", byteArrayProperty.getFormat());
  }
}
