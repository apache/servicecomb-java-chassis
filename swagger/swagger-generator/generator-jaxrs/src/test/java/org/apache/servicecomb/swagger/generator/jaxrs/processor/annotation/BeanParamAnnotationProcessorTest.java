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

package org.apache.servicecomb.swagger.generator.jaxrs.processor.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.http.Part;
import javax.ws.rs.BeanParam;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.servicecomb.swagger.generator.core.OperationGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.jaxrs.JaxrsSwaggerGeneratorContext;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.Parameter;

public class BeanParamAnnotationProcessorTest {

  @Test
  public void processSuccess() throws NoSuchMethodException {
    BeanParamAnnotationProcessor processor = new BeanParamAnnotationProcessor();
    final OperationGenerator operationGenerator = mockOperationGenerator("testBeanParam", AggregatedParam.class);

    processor.process(null, operationGenerator, 0);

    final List<Parameter> providerParameters = operationGenerator.getProviderParameters();
    assertEquals(5, providerParameters.size());
    AbstractSerializableParameter<?> parameter;
    parameter = (AbstractSerializableParameter<?>) providerParameters.get(0);
    assertEquals("path", parameter.getIn());
    assertEquals("path0", parameter.getName());
    assertEquals("pa", parameter.getDefault());
    assertEquals("string", parameter.getType());
    parameter = (AbstractSerializableParameter<?>) providerParameters.get(1);
    assertEquals("query", parameter.getIn());
    assertEquals("query1", parameter.getName());
    assertEquals("integer", parameter.getType());
    assertEquals("int32", parameter.getFormat());
    parameter = (AbstractSerializableParameter<?>) providerParameters.get(2);
    assertEquals("formData", parameter.getIn());
    assertEquals("form3", parameter.getName());
    assertEquals(12L, parameter.getDefault());
    assertEquals("integer", parameter.getType());
    assertEquals("int64", parameter.getFormat());
    parameter = (AbstractSerializableParameter<?>) providerParameters.get(3);
    assertEquals("cookie", parameter.getIn());
    assertEquals("cookie4", parameter.getName());
    assertEquals("integer", parameter.getType());
    assertEquals("int64", parameter.getFormat());
    parameter = (AbstractSerializableParameter<?>) providerParameters.get(4);
    assertEquals("header", parameter.getIn());
    assertEquals("header2", parameter.getName());
    assertEquals("string", parameter.getType());
  }

  @Test
  public void processOnParamWithPart() throws NoSuchMethodException {
    BeanParamAnnotationProcessor processor = new BeanParamAnnotationProcessor();
    final OperationGenerator operationGenerator = mockOperationGenerator("testBeanParamWithPart",
        BeanParamWithPart.class);

    processor.process(null, operationGenerator, 0);

    final List<Parameter> providerParameters = operationGenerator.getProviderParameters();
    assertEquals(3, providerParameters.size());
    AbstractSerializableParameter<?> parameter;
    parameter = (AbstractSerializableParameter<?>) providerParameters.get(0);
    assertEquals("queryStr", parameter.getName());
    assertEquals("query", parameter.getIn());
    assertEquals("boolean", parameter.getType());
    parameter = (AbstractSerializableParameter<?>) providerParameters.get(1);
    assertEquals("up0", parameter.getName());
    assertEquals("formData", parameter.getIn());
    assertEquals("file", parameter.getType());
    parameter = (AbstractSerializableParameter<?>) providerParameters.get(2);
    assertEquals("up1", parameter.getName());
    assertEquals("formData", parameter.getIn());
    assertEquals("file", parameter.getType());
  }

  @Test
  public void processOnComplexBeanParamField() throws NoSuchMethodException {
    BeanParamAnnotationProcessor processor = new BeanParamAnnotationProcessor();
    final OperationGenerator operationGenerator = mockOperationGenerator("testBeanParamComplexField",
        BeanParamComplexField.class);

    try {
      processor.process(null, operationGenerator, 0);
      fail("A error is expected!");
    } catch (Error e) {
      assertEquals("Processing param failed, method=org.apache.servicecomb.swagger.generator.jaxrs.processor"
              + ".annotation.BeanParamAnnotationProcessorTest$TestProvider:testBeanParamComplexField, beanParamIdx=0",
          e.getMessage());
      assertEquals("not allow such type of param:[class io.swagger.models.properties.RefProperty], "
              + "param name is [q]",
          e.getCause().getMessage());
    }
  }

  @Test
  public void processOnComplexBeanParamSetter() throws NoSuchMethodException {
    BeanParamAnnotationProcessor processor = new BeanParamAnnotationProcessor();
    final OperationGenerator operationGenerator = mockOperationGenerator("testBeanParamComplexSetter",
        BeanParamComplexSetter.class);

    try {
      processor.process(null, operationGenerator, 0);
      fail("A error is expected!");
    } catch (Error e) {
      assertEquals("Processing param failed, method=org.apache.servicecomb.swagger.generator.jaxrs.processor"
              + ".annotation.BeanParamAnnotationProcessorTest$TestProvider:testBeanParamComplexSetter, beanParamIdx=0",
          e.getMessage());
      assertEquals("not allow such type of param:[class io.swagger.models.properties.RefProperty], "
              + "param name is [h]",
          e.getCause().getMessage());
    }
  }

  @Test
  public void processOnParamFieldUntagged() throws NoSuchMethodException {
    BeanParamAnnotationProcessor processor = new BeanParamAnnotationProcessor();
    OperationGenerator operationGenerator = mockOperationGenerator("testBeanParamWithUntaggedField",
        BeanParamWithUntaggedField.class);

    try {
      processor.process(null, operationGenerator, 0);
      fail("A error is expected!");
    } catch (Error e) {
      assertEquals("There is a field[name] cannot be mapped to swagger param. "
              + "Maybe you should tag @JsonIgnore on it.",
          e.getCause().getMessage());
    }
  }

  @Test
  public void processOnParamFieldWithJsonIgnore() throws NoSuchMethodException {
    BeanParamAnnotationProcessor processor = new BeanParamAnnotationProcessor();
    OperationGenerator operationGenerator = mockOperationGenerator("testBeanParamWithJsonIgnore",
        BeanParamWithJsonIgnoredTagged.class);

    processor.process(null, operationGenerator, 0);

    final List<Parameter> providerParameters = operationGenerator.getProviderParameters();
    assertEquals(1, providerParameters.size());
    AbstractSerializableParameter<?> parameter = (AbstractSerializableParameter<?>) providerParameters.get(0);
    assertEquals("name", parameter.getName());
    assertEquals("query", parameter.getIn());
    assertEquals("string", parameter.getType());
  }

  private OperationGenerator mockOperationGenerator(String methodName, Class<?>... paramTypes)
      throws NoSuchMethodException {
    final Method providerMethod = TestProvider.class.getDeclaredMethod(methodName, paramTypes);
    final SwaggerGenerator swaggerGenerator = new SwaggerGenerator(new JaxrsSwaggerGeneratorContext(),
        TestProvider.class);
    return new OperationGenerator(swaggerGenerator, providerMethod);
  }

  static class TestProvider {
    public String testBeanParam(@BeanParam AggregatedParam aggregatedParam) {
      return aggregatedParam.toString();
    }

    public String testBeanParamWithPart(@BeanParam BeanParamWithPart aggregatedParam) {
      return aggregatedParam.toString();
    }

    public String testBeanParamComplexField(@BeanParam BeanParamComplexField param) {
      return param.toString();
    }

    public String testBeanParamComplexSetter(@BeanParam BeanParamComplexSetter param) {
      return param.toString();
    }

    public String testBeanParamWithUntaggedField(@BeanParam BeanParamWithUntaggedField param) {
      return param.toString();
    }

    public String testBeanParamWithJsonIgnore(@BeanParam BeanParamWithJsonIgnoredTagged param) {
      return param.toString();
    }
  }

  static class AggregatedParam {
    @DefaultValue("pa")
    @PathParam("path0")
    private String strVal;

    @QueryParam("query1")
    private int intVal;

    private long longVal;

    private long cookieVal;

    @HeaderParam("header2")
    private String headerVal;

    public String getStrVal() {
      return strVal;
    }

    public void setStrVal(String strVal) {
      this.strVal = strVal;
    }

    public int getIntVal() {
      return intVal;
    }

    public void setIntVal(int intVal) {
      this.intVal = intVal;
    }

    public long getLongVal() {
      return longVal;
    }

    @DefaultValue("12")
    @FormParam("form3")
    public void setLongVal(long longVal) {
      this.longVal = longVal;
    }

    public long getCookieVal() {
      return cookieVal;
    }

    @CookieParam("cookie4")
    public void setCookieVal(long cookieVal) {
      this.cookieVal = cookieVal;
    }

    public String getHeaderVal() {
      return headerVal;
    }

    public void setHeaderVal(String headerVal) {
      this.headerVal = headerVal;
    }
  }

  static class BeanParamWithPart {
    @QueryParam("queryStr")
    private boolean queryStr;

    @FormParam("up0")
    private Part up0;

    private Part up1;

    public boolean isQueryStr() {
      return queryStr;
    }

    public void setQueryStr(boolean queryStr) {
      this.queryStr = queryStr;
    }

    public Part getUp0() {
      return up0;
    }

    public void setUp0(Part up0) {
      this.up0 = up0;
    }

    public Part getUp1() {
      return up1;
    }

    @FormParam("up1")
    public void setUp1(Part up1) {
      this.up1 = up1;
    }
  }

  static class BeanParamComplexField {
    @QueryParam("q")
    private AggregatedParam complex;

    public AggregatedParam getComplex() {
      return complex;
    }

    public void setComplex(AggregatedParam complex) {
      this.complex = complex;
    }
  }

  static class BeanParamComplexSetter {
    private AggregatedParam complex;

    public AggregatedParam getComplex() {
      return complex;
    }

    @HeaderParam("h")
    public void setComplex(AggregatedParam complex) {
      this.complex = complex;
    }
  }

  static class BeanParamWithUntaggedField {
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  static class BeanParamWithJsonIgnoredTagged {
    @QueryParam("name")
    private String name;

    @JsonIgnore
    private AggregatedParam ignored;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public AggregatedParam getIgnored() {
      return ignored;
    }

    public void setIgnored(
        AggregatedParam ignored) {
      this.ignored = ignored;
    }
  }
}
