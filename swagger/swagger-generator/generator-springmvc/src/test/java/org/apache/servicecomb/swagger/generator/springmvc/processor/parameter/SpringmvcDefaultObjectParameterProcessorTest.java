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

import java.lang.reflect.Method;
import java.util.List;

import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import mockit.Deencapsulation;

public class SpringmvcDefaultObjectParameterProcessorTest {

  @Test
  public void processOnObjectParam() throws NoSuchMethodException {
    final OperationGenerator operationGenerator = mockOperationGenerator("testObjectParam", "/test", TestParam.class);

    new SpringmvcDefaultObjectParameterProcessor().process(operationGenerator, 0);

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

  @Test
  public void processOnMultiObjectParamsWithSameFieldName() throws NoSuchMethodException {
    final OperationGenerator operationGenerator = mockOperationGenerator("testMultiObjParamsWithSameFiledName", "/test",
        String.class, TestParam.class, TestParam.class, int.class);

    final SpringmvcDefaultObjectParameterProcessor springmvcDefaultParameterProcessor = new SpringmvcDefaultObjectParameterProcessor();
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

  @Test
  public void processOnRecursiveObjectParam() throws NoSuchMethodException {
    final OperationGenerator operationGenerator = mockOperationGenerator("testRecursiveParam", "/test",
        RecursiveParamA.class);

    new SpringmvcDefaultObjectParameterProcessor().process(operationGenerator, 0);

    final List<Parameter> providerParameters = operationGenerator.getProviderParameters();
    Assert.assertEquals(1, providerParameters.size());
    Parameter parameter = providerParameters.get(0);
    Assert.assertEquals(QueryParameter.class, parameter.getClass());
    Assert.assertEquals("name", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    QueryParameter queryParameter = (QueryParameter) parameter;
    Assert.assertEquals("string", queryParameter.getType());
  }

  @Test
  public void processOnGenericObjectParam() throws NoSuchMethodException {
    final OperationGenerator operationGenerator = mockOperationGenerator("testGenericObjectParam", "/test",
        GenericParam.class);

    new SpringmvcDefaultObjectParameterProcessor().process(operationGenerator, 0);

    final List<Parameter> providerParameters = operationGenerator.getProviderParameters();
    Assert.assertEquals(2, providerParameters.size());
    Parameter parameter = providerParameters.get(0);
    Assert.assertEquals(QueryParameter.class, parameter.getClass());
    Assert.assertEquals("num", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    QueryParameter queryParameter = (QueryParameter) parameter;
    Assert.assertEquals("integer", queryParameter.getType());
    Assert.assertEquals("int32", queryParameter.getFormat());
    parameter = providerParameters.get(1);
    Assert.assertEquals(QueryParameter.class, parameter.getClass());
    Assert.assertEquals("str", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    queryParameter = (QueryParameter) parameter;
    Assert.assertEquals("string", queryParameter.getType());
  }

  @Test
  public void processOnGenericSimpleParam() throws NoSuchMethodException {
    final OperationGenerator operationGenerator = mockOperationGenerator("testGenericSimpleParam", "/test",
        GenericParam.class);

    new SpringmvcDefaultObjectParameterProcessor().process(operationGenerator, 0);

    final List<Parameter> providerParameters = operationGenerator.getProviderParameters();
    Assert.assertEquals(3, providerParameters.size());
    Parameter parameter = providerParameters.get(0);
    Assert.assertEquals(QueryParameter.class, parameter.getClass());
    Assert.assertEquals("num", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    QueryParameter queryParameter = (QueryParameter) parameter;
    Assert.assertEquals("integer", queryParameter.getType());
    Assert.assertEquals("int32", queryParameter.getFormat());
    parameter = providerParameters.get(1);
    Assert.assertEquals(QueryParameter.class, parameter.getClass());
    Assert.assertEquals("str", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    queryParameter = (QueryParameter) parameter;
    Assert.assertEquals("string", queryParameter.getType());
    parameter = providerParameters.get(2);
    Assert.assertEquals(QueryParameter.class, parameter.getClass());
    Assert.assertEquals("data", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    queryParameter = (QueryParameter) parameter;
    Assert.assertEquals("string", queryParameter.getType());
  }

  private OperationGenerator mockOperationGenerator(String providerParamName, String path, Class<?>... classes)
      throws NoSuchMethodException {
    final SwaggerGenerator swaggerGenerator = new SwaggerGenerator(Mockito.mock(SwaggerGeneratorContext.class),
        TestProvider.class);
    final Method providerMethod = TestProvider.class
        .getDeclaredMethod(providerParamName, classes);
    final OperationGenerator operationGenerator = new OperationGenerator(swaggerGenerator, providerMethod);
    Deencapsulation.setField(operationGenerator, "path", path);
    return operationGenerator;
  }

  static class TestProvider {
    public String testObjectParam(TestParam objParam) {
      return objParam.toString();
    }

    public String testMultiObjParamsWithSameFiledName(String name, TestParam objParam0, TestParam objParam1, int age) {
      return objParam0 + "-" + objParam1;
    }

    public String testRecursiveParam(RecursiveParamA recursiveParamA) {
      return null;
    }

    public String testGenericObjectParam(GenericParam<TestParam> genericParam) {
      return genericParam.toString();
    }

    public String testGenericSimpleParam(GenericParam<String> genericParam) {
      return genericParam.toString();
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

  static class GenericParam<T> {
    private int num;

    private String str;

    T data;

    public int getNum() {
      return num;
    }

    public void setNum(int num) {
      this.num = num;
    }

    public String getStr() {
      return str;
    }

    public void setStr(String str) {
      this.str = str;
    }

    public T getData() {
      return data;
    }

    public void setData(T data) {
      this.data = data;
    }
  }

  static class RecursiveParamA {
    private String name;

    private RecursiveParamB recursiveParamB;

    private RecursiveParamC recursiveParamC;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public RecursiveParamB getRecursiveParamB() {
      return recursiveParamB;
    }

    public void setRecursiveParamB(
        RecursiveParamB recursiveParamB) {
      this.recursiveParamB = recursiveParamB;
    }

    public RecursiveParamC getRecursiveParamC() {
      return recursiveParamC;
    }

    public void setRecursiveParamC(
        RecursiveParamC recursiveParamC) {
      this.recursiveParamC = recursiveParamC;
    }
  }

  static class RecursiveParamB {
    private int age;

    private RecursiveParamA recursiveParamA;

    private RecursiveParamC recursiveParamC;

    public int getAge() {
      return age;
    }

    public void setAge(int age) {
      this.age = age;
    }

    public RecursiveParamA getRecursiveParamA() {
      return recursiveParamA;
    }

    public void setRecursiveParamA(
        RecursiveParamA recursiveParamA) {
      this.recursiveParamA = recursiveParamA;
    }

    public RecursiveParamC getRecursiveParamC() {
      return recursiveParamC;
    }

    public void setRecursiveParamC(
        RecursiveParamC recursiveParamC) {
      this.recursiveParamC = recursiveParamC;
    }
  }

  static class RecursiveParamC {
    private String address;

    private RecursiveParamB recursiveParamB;

    public String getAddress() {
      return address;
    }

    public void setAddress(String address) {
      this.address = address;
    }

    public RecursiveParamB getRecursiveParamB() {
      return recursiveParamB;
    }

    public void setRecursiveParamB(
        RecursiveParamB recursiveParamB) {
      this.recursiveParamB = recursiveParamB;
    }
  }
}