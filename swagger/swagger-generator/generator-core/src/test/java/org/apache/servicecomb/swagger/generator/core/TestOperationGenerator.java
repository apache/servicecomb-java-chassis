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

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.servicecomb.foundation.test.scaffolding.spring.SpringUtils;
import org.apache.servicecomb.swagger.extend.parameter.HttpRequestParameter;
import org.apache.servicecomb.swagger.generator.pojo.PojoSwaggerGeneratorContext;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StringValueResolver;

import io.swagger.annotations.ApiOperation;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;

public class TestOperationGenerator {
  @Test
  public void testPathPlaceHolder() {
    StringValueResolver stringValueResolver =
        SpringUtils.createStringValueResolver(Collections.singletonMap("var", "varValue"));

    PojoSwaggerGeneratorContext context = new PojoSwaggerGeneratorContext();
    context.setEmbeddedValueResolver(stringValueResolver);

    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(context, null);
    OperationGenerator operationGenerator = new OperationGenerator(swaggerGenerator, null);
    operationGenerator.setPath("/a/${var}/b");

    assertEquals("/a/varValue/b", operationGenerator.getPath());
  }

  @Test
  public void testConvertTags() throws NoSuchMethodException {
    Method function = TestClass.class.getMethod("function");
    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(new PojoSwaggerGeneratorContext(), TestClass.class);
    OperationGenerator operationGenerator = new OperationGenerator(swaggerGenerator, function);

    operationGenerator.generate();

    List<String> tagList = operationGenerator.getOperation().getTags();
    assertThat(tagList, contains("tag1", "tag2"));
  }

  @Test
  public void testConvertTagsOnMethodWithNoTag() throws NoSuchMethodException {
    Method function = TestClass.class.getMethod("functionWithNoTag");
    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(new PojoSwaggerGeneratorContext(), TestClass.class);
    OperationGenerator operationGenerator = new OperationGenerator(swaggerGenerator, function);
    swaggerGenerator.addDefaultTag("default0");
    swaggerGenerator.addDefaultTag("default1");

    operationGenerator.generate();

    List<String> tagList = operationGenerator.getOperation().getTags();
    assertThat(tagList, contains("default0", "default1"));
  }

  @Test
  public void testConvertTagsOnMethodWithNoAnnotation() throws NoSuchMethodException {
    Method function = TestClass.class.getMethod("functionWithNoAnnotation");
    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(new PojoSwaggerGeneratorContext(), TestClass.class);
    OperationGenerator operationGenerator = new OperationGenerator(swaggerGenerator, function);
    swaggerGenerator.addDefaultTag("default0");
    swaggerGenerator.addDefaultTag("default1");

    operationGenerator.generate();

    List<String> tagList = operationGenerator.getOperation().getTags();
    assertThat(tagList, contains("default0", "default1"));
  }

  @Test
  public void addProviderParameter() throws NoSuchMethodException {
    Method function = TestClass.class.getMethod("functionWithNoAnnotation");
    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(new PojoSwaggerGeneratorContext(), TestClass.class);
    OperationGenerator operationGenerator = new OperationGenerator(swaggerGenerator, function);

    Parameter parameter = new BodyParameter();
    parameter.setName("param0");
    operationGenerator.addProviderParameter(parameter);
    Assert.assertEquals(1, operationGenerator.getProviderParameters().size());
    Assert.assertSame(parameter, operationGenerator.getProviderParameters().get(0));

    parameter = new HttpRequestParameter();
    operationGenerator.addProviderParameter(parameter);
    Assert.assertSame(parameter, operationGenerator.getProviderParameters().get(1));

    parameter = new QueryParameter();
    parameter.setName("param1");
    operationGenerator.addProviderParameter(parameter);
    Assert.assertSame(parameter, operationGenerator.getProviderParameters().get(2));

    parameter = new QueryParameter();
    parameter.setName("param0");
    operationGenerator.addProviderParameter(parameter);
    Assert.assertEquals(3, operationGenerator.getProviderParameters().size());
    Assert.assertNotSame(parameter, operationGenerator.getProviderParameters().get(2));
  }

  private static class TestClass {
    @ApiOperation(value = "value1", tags = {"tag1", "tag2"})
    public void function() {
    }

    @ApiOperation(value = "value2")
    public void functionWithNoTag() {
    }

    @SuppressWarnings("unused")
    public void functionWithNoAnnotation() {
    }
  }
}
