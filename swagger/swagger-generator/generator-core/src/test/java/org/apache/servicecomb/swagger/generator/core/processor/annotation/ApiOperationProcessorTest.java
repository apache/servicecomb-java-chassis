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

package org.apache.servicecomb.swagger.generator.core.processor.annotation;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.pojo.PojoSwaggerGeneratorContext;
import org.junit.Test;

import io.swagger.annotations.ApiOperation;

public class ApiOperationProcessorTest {

  @Test
  public void testConvertTags() throws NoSuchMethodException {
    ApiOperationProcessor apiOperationProcessor = new ApiOperationProcessor();

    Method function = TestClass.class.getMethod("function");
    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(new PojoSwaggerGeneratorContext(), TestClass.class);
    OperationGenerator operationGenerator = new OperationGenerator(swaggerGenerator, function);

    apiOperationProcessor.process(function.getAnnotation(ApiOperation.class),
        operationGenerator);

    assertThat(operationGenerator.getOperation().getTags(), containsInAnyOrder("tag1", "tag2"));
  }

  @Test
  public void testConvertTagsOnMethodWithNoTag() throws NoSuchMethodException {
    ApiOperationProcessor apiOperationProcessor = new ApiOperationProcessor();

    Method function = TestClass.class.getMethod("functionWithNoTag");
    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(new PojoSwaggerGeneratorContext(), TestClass.class);
    OperationGenerator operationGenerator = new OperationGenerator(swaggerGenerator, function);

    apiOperationProcessor.process(function.getAnnotation(ApiOperation.class),
        operationGenerator);

    List<String> tagList = operationGenerator.getOperation().getTags();
    assertNull(tagList);
  }

  private static class TestClass {
    @ApiOperation(value = "value1", tags = {"tag1", "tag2"})
    public void function() {
    }

    @ApiOperation(value = "value2")
    public void functionWithNoTag() {
    }
  }
}
