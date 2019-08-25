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

import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.servicecomb.swagger.generator.core.pojo.TestType1;
import org.apache.servicecomb.swagger.generator.core.pojo.TestType2;
import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;
import org.apache.servicecomb.swagger.generator.core.utils.ParamUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

public class TestParamUtils {
  @Test
  public void testGetRawJsonType() {
    Parameter param = Mockito.mock(Parameter.class);
    Map<String, Object> extensions = new HashMap<>();
    when(param.getVendorExtensions()).thenReturn(extensions);

    extensions.put(SwaggerConst.EXT_RAW_JSON_TYPE, true);
    Assert.assertTrue(ClassUtils.isRawJsonType(param));

    extensions.put(SwaggerConst.EXT_RAW_JSON_TYPE, "test");
    Assert.assertFalse(ClassUtils.isRawJsonType(param));
  }

  @Test
  public void isComplexProperty() {
    Property property = new RefProperty("ref");
    Assert.assertTrue(ParamUtils.isComplexProperty(property));
    property = new ObjectProperty();
    Assert.assertTrue(ParamUtils.isComplexProperty(property));
    property = new MapProperty();
    Assert.assertTrue(ParamUtils.isComplexProperty(property));
    property = new ArrayProperty(new ObjectProperty());
    Assert.assertTrue(ParamUtils.isComplexProperty(property));

    property = new ArrayProperty(new StringProperty());
    Assert.assertFalse(ParamUtils.isComplexProperty(property));
    property = new StringProperty();
    Assert.assertFalse(ParamUtils.isComplexProperty(property));
  }

  @Test
  public void setParameterTypeByTypeNormal() {
    AbstractSerializableParameter<?> parameter = new QueryParameter();
    ParamUtils.setParameterType(String.class, parameter);
    Assert.assertEquals("string", parameter.getType());

    parameter = new HeaderParameter();
    ParamUtils.setParameterType(long.class, parameter);
    Assert.assertEquals("integer", parameter.getType());
    Assert.assertEquals("int64", parameter.getFormat());
  }

  @Test
  public void setParameterTypeByTypeOnComplexType() {
    AbstractSerializableParameter<?> parameter = new QueryParameter();
    parameter.setName("testName");
    try {
      ParamUtils.setParameterType(ArrayList.class, parameter);
      Assert.fail("an exception is expected!");
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("not allow such type of param:[class io.swagger.models.properties.ArrayProperty], "
          + "param name is [testName]", e.getMessage());
    }
  }

  private static class AllTypeTest1 {
    TestType1 t1;

    List<TestType1> t2;

    Map<String, TestType1> t3;

    TestType1[] t4;
  }

  private static class AllTypeTest2 {
    TestType2 t1;

    List<TestType2> t2;

    Map<String, TestType2> t3;

    TestType2[] t4;
  }

  @Test
  public void testAddDefinitions() {
    Field[] fields1 = AllTypeTest1.class.getDeclaredFields();
    Field[] fields2 = AllTypeTest2.class.getDeclaredFields();
    for (int i = 0; i < fields1.length; i++) {
      for (int j = 0; j < fields2.length; j++) {
        if (fields1[i].isSynthetic() || fields2[j].isSynthetic()) {
          continue;
        }
        try {
          testExcep(fields1[i].getGenericType(), fields2[j].getGenericType());
          fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
          assertThat(e.getMessage(), containsString("duplicate param model:"));
        }
      }
    }
  }

  private void testExcep(Type f1, Type f2) {
    Swagger swagger = new Swagger();
    ParamUtils.addDefinitions(swagger, f1);
    ParamUtils.addDefinitions(swagger, f2);
  }

  @Test
  public void testGenericTypeInheritance() throws Exception {
    Method hello = IMyService.class.getMethod("hello", AbstractBean.class);
    assertEquals(PersonBean.class,
        ParamUtils.getGenericParameterType(IMyService.class, IBaseService.class, hello.getGenericReturnType()));
    hello = IMyServiceChild.class.getMethod("hello", AbstractBean.class);
    assertEquals(PersonBean.class,
        ParamUtils.getGenericParameterType(IMyServiceChild.class, IBaseService.class, hello.getGenericReturnType()));

    try {
      hello = IMyServiceChild.class.getMethod("hello", AbstractBean.class);
      assertEquals(PersonBean.class,
          ParamUtils.getGenericParameterType(IMyServiceChild2.class, IBaseService.class, hello.getGenericReturnType()));
      Assert.fail("");
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("not implement (org.apache.servicecomb.swagger.generator.core.IMyServiceChild2) "
          + "(org.apache.servicecomb.swagger.generator.core.IBaseService) (T), "
          + "e.g. extends multiple typed interface or too deep inheritance.", e.getMessage());
    }

    hello = MyEndpoint.class.getMethod("hello", AbstractBean.class);
    assertEquals(PersonBean.class,
        ParamUtils.getGenericParameterType(MyEndpoint.class, AbstractBaseService.class, hello.getGenericReturnType()));

    Method helloBody = IMyService.class.getMethod("helloBody", AbstractBean[].class);
    assertEquals(PersonBean[].class,
        ParamUtils.getGenericParameterType(IMyService.class, IBaseService.class, helloBody.getGenericReturnType()));

    helloBody = MyEndpoint.class.getMethod("helloBody", AbstractBean[].class);
    assertEquals(PersonBean[].class, ParamUtils
        .getGenericParameterType(MyEndpoint.class, AbstractBaseService.class, helloBody.getGenericReturnType()));

    helloBody = MyEndpoint2.class.getMethod("helloBody", PersonBean[].class);
    assertEquals(PersonBean[].class, ParamUtils
        .getGenericParameterType(MyEndpoint2.class, AbstractBaseService.class, helloBody.getGenericReturnType()));
    assertEquals(PersonBean[].class, ParamUtils
        .getGenericParameterType(MyEndpoint2.class, AbstractBaseService.class, helloBody.getGenericParameterTypes()[0]));

    Method helloList = IMyService.class.getMethod("helloList", List.class);
    assertEquals(TypeUtils.parameterize(List.class, PersonBean.class),
        ParamUtils.getGenericParameterType(IMyService.class, IBaseService.class, helloList.getGenericReturnType()));

    helloList = MyEndpoint.class.getMethod("helloList", List.class);
    assertEquals(TypeUtils.parameterize(List.class, PersonBean.class), ParamUtils
        .getGenericParameterType(MyEndpoint.class, AbstractBaseService.class, helloList.getGenericReturnType()));

    Method actual = IMyService.class.getMethod("actual");
    assertEquals(PersonBean.class,
        ParamUtils.getGenericParameterType(IMyService.class, IBaseService.class, actual.getGenericReturnType()));

    helloList = MyEndpoint.class.getMethod("actual");
    assertEquals(PersonBean.class,
        ParamUtils.getGenericParameterType(MyEndpoint.class, AbstractBaseService.class, actual.getGenericReturnType()));
  }
}
