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

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.swagger.generator.core.utils.ClassUtils;
import org.apache.servicecomb.swagger.generator.core.utils.ParamUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

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
}
