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

import org.apache.servicecomb.foundation.test.scaffolding.model.User;
import org.apache.servicecomb.swagger.generator.core.unittest.SwaggerGeneratorForTest;
import org.apache.servicecomb.swagger.generator.pojo.PojoSwaggerGeneratorContext;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;

public class TestApiImplicitParams {
  SwaggerGeneratorContext context = new PojoSwaggerGeneratorContext();

  interface ApiImplicitParamsAnnotation {
    @ApiImplicitParams(
        value = {@ApiImplicitParam(
            paramType = "body",
            name = "body",
            dataType = "org.apache.servicecomb.foundation.test.scaffolding.model.User")})
    void testBody();
  }

  @Test
  public void testBody() {
    SwaggerGenerator swaggerGenerator =
        new SwaggerGeneratorForTest(context, ApiImplicitParamsAnnotation.class);
    swaggerGenerator.generate();

    Swagger swagger = swaggerGenerator.getSwagger();
    Path path = swagger.getPaths().get("/testBody");
    Operation operation = path.getOperations().get(0);
    Parameter parameter = operation.getParameters().get(0);

    JavaType javaType = ClassUtilsForTest.findJavaType(swaggerGenerator, parameter);
    Assert.assertEquals(User.class, javaType.getRawClass());
  }
}
