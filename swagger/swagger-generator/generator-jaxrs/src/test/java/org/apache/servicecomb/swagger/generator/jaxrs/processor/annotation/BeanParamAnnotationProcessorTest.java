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

import java.lang.reflect.Method;
import java.util.List;

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
    AbstractSerializableParameter<?> parameter = (AbstractSerializableParameter<?>) providerParameters.get(0);
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
    assertEquals("header", parameter.getIn());
    assertEquals("header2", parameter.getName());
    assertEquals("string", parameter.getType());
    parameter = (AbstractSerializableParameter<?>) providerParameters.get(3);
    assertEquals("formData", parameter.getIn());
    assertEquals("form3", parameter.getName());
    assertEquals(12L, parameter.getDefault());
    assertEquals("integer", parameter.getType());
    assertEquals("int64", parameter.getFormat());
    parameter = (AbstractSerializableParameter<?>) providerParameters.get(4);
    assertEquals("cookie", parameter.getIn());
    assertEquals("cookie4", parameter.getName());
    assertEquals("integer", parameter.getType());
    assertEquals("int64", parameter.getFormat());
  }

  @Test
  public void processOnComplexBeanParamField() throws NoSuchMethodException {
    BeanParamAnnotationProcessor processor = new BeanParamAnnotationProcessor();
    final OperationGenerator operationGenerator = mockOperationGenerator("testBeanParamComplexField",
        BeanParamComplexField.class);

    try {
      processor.process(null, operationGenerator, 0);
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
    } catch (Error e) {
      assertEquals("Processing param failed, method=org.apache.servicecomb.swagger.generator.jaxrs.processor"
              + ".annotation.BeanParamAnnotationProcessorTest$TestProvider:testBeanParamComplexSetter, beanParamIdx=0",
          e.getMessage());
      assertEquals("not allow such type of param:[class io.swagger.models.properties.RefProperty], "
              + "param name is [h]",
          e.getCause().getMessage());
    }
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

    public String testBeanParamComplexField(@BeanParam BeanParamComplexField param) {
      return param.toString();
    }

    public String testBeanParamComplexSetter(@BeanParam BeanParamComplexSetter param) {
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

    public AggregatedParam setStrVal(String strVal) {
      this.strVal = strVal;
      return this;
    }

    public int getIntVal() {
      return intVal;
    }

    public AggregatedParam setIntVal(int intVal) {
      this.intVal = intVal;
      return this;
    }

    public long getLongVal() {
      return longVal;
    }

    @DefaultValue("12")
    @FormParam("form3")
    public AggregatedParam setLongVal(long longVal) {
      this.longVal = longVal;
      return this;
    }

    public long getCookieVal() {
      return cookieVal;
    }

    @CookieParam("cookie4")
    public AggregatedParam setCookieVal(long cookieVal) {
      this.cookieVal = cookieVal;
      return this;
    }

    public String getHeaderVal() {
      return headerVal;
    }

    public AggregatedParam setHeaderVal(String headerVal) {
      this.headerVal = headerVal;
      return this;
    }
  }

  static class BeanParamComplexField {
    @QueryParam("q")
    private AggregatedParam complex;

    public AggregatedParam getComplex() {
      return complex;
    }

    public BeanParamComplexField setComplex(
        AggregatedParam complex) {
      this.complex = complex;
      return this;
    }
  }

  static class BeanParamComplexSetter {
    private AggregatedParam complex;

    public AggregatedParam getComplex() {
      return complex;
    }

    @HeaderParam("h")
    public BeanParamComplexSetter setComplex(
        AggregatedParam complex) {
      this.complex = complex;
      return this;
    }
  }
}