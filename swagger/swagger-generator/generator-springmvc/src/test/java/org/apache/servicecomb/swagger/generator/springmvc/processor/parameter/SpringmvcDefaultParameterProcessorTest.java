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

package org.apache.servicecomb.swagger.generator.springmvc.processor.parameter;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import mockit.Deencapsulation;

public class SpringmvcDefaultParameterProcessorTest {
  @Test
  public void processOnSimpleParam() throws NoSuchMethodException {
    final SwaggerGenerator swaggerGenerator = new SwaggerGenerator(Mockito.mock(SwaggerGeneratorContext.class),
        TestProvider.class);
    final Method providerMethod = TestProvider.class.getDeclaredMethod("testSimpleParam", String.class);
    final OperationGenerator operationGenerator = new OperationGenerator(swaggerGenerator, providerMethod);

    new SpringmvcDefaultParameterProcessor().process(operationGenerator, 0);

    final List<Parameter> providerParameters = operationGenerator.getProviderParameters();
    Assert.assertEquals(1, providerParameters.size());
    Parameter parameter = providerParameters.get(0);
    Assert.assertEquals(QueryParameter.class, parameter.getClass());
    Assert.assertEquals("strParam", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    QueryParameter queryParameter = (QueryParameter) parameter;
    Assert.assertEquals("string", queryParameter.getType());
  }

  @Test
  public void processOnObjectParam() throws NoSuchMethodException {
    final OperationGenerator operationGenerator = mockOperationGenerator("testObjectParam", "/test", TestParam.class);

    new SpringmvcDefaultParameterProcessor().process(operationGenerator, 0);

    final List<Parameter> providerParameters = operationGenerator.getProviderParameters();
    Assert.assertEquals(2, providerParameters.size());
    Parameter parameter = providerParameters.get(0);
    Assert.assertEquals(QueryParameter.class, parameter.getClass());
    Assert.assertEquals("name", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    QueryParameter queryParameter = (QueryParameter) parameter;
    Assert.assertEquals("string", queryParameter.getType());
    parameter = providerParameters.get(1);
    Assert.assertEquals(QueryParameter.class, parameter.getClass());
    Assert.assertEquals("age", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    queryParameter = (QueryParameter) parameter;
    Assert.assertEquals("integer", queryParameter.getType());
    Assert.assertEquals("int32", queryParameter.getFormat());
  }

  /**
   * Map and List param is not supported
   */
  @Test
  public void processOnUnsupportedParam() throws NoSuchMethodException {
    final SwaggerGenerator swaggerGenerator = new SwaggerGenerator(Mockito.mock(SwaggerGeneratorContext.class),
        TestProvider.class);
    final Method providerMethod = TestProvider.class.getDeclaredMethod("testUnsupportedParamType",
        int.class, List.class, Map.class);
    final OperationGenerator operationGenerator = new OperationGenerator(swaggerGenerator, providerMethod);

    try {
      new SpringmvcDefaultParameterProcessor().process(operationGenerator, 1);
      fail("an error is expected!");
    } catch (Error e) {
      Assert.assertEquals(
          "cannot process parameter [integerList], method=org.apache.servicecomb.swagger.generator.springmvc"
              + ".processor.parameter.SpringmvcDefaultParameterProcessorTest$TestProvider:testUnsupportedParamType, "
              + "paramIdx=1, type=java.util.List<org.apache.servicecomb.swagger.generator.springmvc.processor.parameter"
              + ".SpringmvcDefaultParameterProcessorTest$TestParam>",
          e.getMessage());
    }
    try {
      new SpringmvcDefaultParameterProcessor().process(operationGenerator, 2);
      fail("an error is expected!");
    } catch (Error e) {
      Assert.assertEquals(
          "cannot process parameter [stringMap], method=org.apache.servicecomb.swagger.generator.springmvc"
              + ".processor.parameter.SpringmvcDefaultParameterProcessorTest$TestProvider:testUnsupportedParamType, "
              + "paramIdx=2, type=java.util.Map<java.lang.String, java.lang.String>",
          e.getMessage());
    }
  }

  @Test
  public void processOnMultiObjectParamsWithSameFieldName() throws NoSuchMethodException {
    final OperationGenerator operationGenerator = mockOperationGenerator("testMultiObjParamsWithSameFiledName", "/test",
        String.class, TestParam.class, TestParam.class, int.class);

    final SpringmvcDefaultParameterProcessor springmvcDefaultParameterProcessor = new SpringmvcDefaultParameterProcessor();
    springmvcDefaultParameterProcessor.process(operationGenerator, 0);
    springmvcDefaultParameterProcessor.process(operationGenerator, 1);
    springmvcDefaultParameterProcessor.process(operationGenerator, 2);
    springmvcDefaultParameterProcessor.process(operationGenerator, 3);

    final List<Parameter> providerParameters = operationGenerator.getProviderParameters();
    Assert.assertEquals(2, providerParameters.size());
    Parameter parameter = providerParameters.get(0);
    Assert.assertEquals(QueryParameter.class, parameter.getClass());
    Assert.assertEquals("name", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    QueryParameter queryParameter = (QueryParameter) parameter;
    Assert.assertEquals("string", queryParameter.getType());
    parameter = providerParameters.get(1);
    Assert.assertEquals(QueryParameter.class, parameter.getClass());
    Assert.assertEquals("age", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    queryParameter = (QueryParameter) parameter;
    Assert.assertEquals("integer", queryParameter.getType());
    Assert.assertEquals("int32", queryParameter.getFormat());
  }

  private OperationGenerator mockOperationGenerator(String providerParamName, String path, Class<?>... classes)
      throws NoSuchMethodException {
    final SwaggerGenerator swaggerGenerator = new SwaggerGenerator(Mockito.mock(SwaggerGeneratorContext.class),
        TestProvider.class);
    final Method providerMethod = TestProvider.class.getDeclaredMethod(providerParamName, classes);
    final OperationGenerator operationGenerator = new OperationGenerator(swaggerGenerator, providerMethod);
    Deencapsulation.setField(operationGenerator, "path", path);
    return operationGenerator;
  }

  static class TestProvider {
    public String testSimpleParam(String strParam) {
      return strParam;
    }

    public String testObjectParam(TestParam objParam) {
      return objParam.toString();
    }

    public String testUnsupportedParamType(int i, List<TestParam> integerList, Map<String, String> stringMap) {
      return null;
    }

    public String testMultiObjParamsWithSameFiledName(String name, TestParam objParam0, TestParam objParam1, int age) {
      return objParam0 + "-" + objParam1;
    }
  }

  static class TestParam {
    private String name;

    private int age;

    public String getName() {
      return name;
    }

    public TestParam setName(String name) {
      this.name = name;
      return this;
    }

    public int getAge() {
      return age;
    }

    public TestParam setAge(int age) {
      this.age = age;
      return this;
    }
  }
}