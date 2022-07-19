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

package org.apache.servicecomb.common.rest.codec.param;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.param.FormProcessorCreator.FormProcessor;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.parameters.FormParameter;
import io.swagger.models.properties.ArrayProperty;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
public class TestFormProcessor {
  final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

  final Map<String, Object> forms = new HashMap<>();

  final RestClientRequest clientRequest = Mockito.mock(RestClientRequest.class);

  private FormProcessor createProcessor(String name, Type type) {
    return createProcessor(name, type, null, true);
  }

  private FormProcessor createProcessor(String name, Type type, String defaultValue, boolean required) {
    JavaType javaType = TypeFactory.defaultInstance().constructType(type);

    FormParameter formParameter = new FormParameter();
    formParameter.name(name)
        .required(required)
        .setDefaultValue(defaultValue);

    if (javaType.isContainerType()) {
      formParameter.type(ArrayProperty.TYPE);
    }
    return new FormProcessor(formParameter, javaType);
  }

  @Test
  public void testGetValueWithAttr() throws Exception {
    Map<String, Object> forms = new HashMap<>();
    forms.put("name", "value");
    Mockito.when(request.getAttribute(RestConst.FORM_PARAMETERS)).thenReturn(forms);

    ParamValueProcessor processor = createProcessor("name", String.class);
    Object value = processor.getValue(request);
    Assertions.assertEquals("value", value);
  }

  @Test
  public void testGetValueNormal() throws Exception {
    Mockito.when(request.getParameter("name")).thenReturn("value");

    ParamValueProcessor processor = createProcessor("name", String.class);
    Object value = processor.getValue(request);
    Assertions.assertEquals("value", value);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetValueNormalDate() throws Exception {
    Date date = new Date();
    String strDate = com.fasterxml.jackson.databind.util.ISO8601Utils.format(date);
    Mockito.when(request.getParameter("name")).thenReturn(strDate);

    ParamValueProcessor processor = createProcessor("name", Date.class);
    Object value = processor.getValue(request);
    Assertions.assertEquals(strDate, com.fasterxml.jackson.databind.util.ISO8601Utils.format((Date) value));
  }

  @Test
  public void testGetValueContainerTypeNull() throws Exception {
    Mockito.when(request.getParameterValues("name")).thenReturn(null);

    ParamValueProcessor processor = createProcessor("name", String[].class, null, false);
    String[] value = (String[]) processor.getValue(request);
    Assertions.assertNull(value);
  }

  @Test
  public void testGetValueNull() throws Exception {
    Mockito.when(request.getParameter("name")).thenReturn(null);

    ParamValueProcessor processor = createProcessor("name", String.class, null, true);
    try {
      processor.getValue(request);
      Assertions.assertEquals("required is true, throw exception", "not throw exception");
    } catch (Exception e) {
      Assertions.assertTrue(e.getMessage().contains("Parameter is required."));
    }
  }

  @Test
  public void testGetValueArray() throws Exception {
    Mockito.when(request.getParameterValues("name")).thenReturn(new String[] {"value"});

    ParamValueProcessor processor = createProcessor("name", String[].class);
    String[] value = (String[]) processor.getValue(request);
    MatcherAssert.assertThat(value, Matchers.arrayContaining("value"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetValueList() throws Exception {
    Mockito.when(request.getParameterValues("name")).thenReturn(new String[] {"value"});

    ParamValueProcessor processor = createProcessor("name",
        TypeFactory.defaultInstance().constructCollectionType(List.class, String.class),
        null, true);
    Object value = processor.getValue(request);
    MatcherAssert.assertThat((List<String>) value, Matchers.contains("value"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetValueSet() throws Exception {
    Mockito.when(request.getParameterValues("name")).thenReturn(new String[] {"value"});

    ParamValueProcessor processor = createProcessor("name",
        TypeFactory.defaultInstance().constructCollectionType(Set.class, String.class), null,
        true);
    Object value = processor.getValue(request);
    MatcherAssert.assertThat((Set<String>) value, Matchers.contains("value"));
  }

  @Test
  public void testSetValue() throws Exception {
    Mockito.doAnswer(invocation -> {
      forms.put("name", "value");
      return null;
    }).when(clientRequest).addForm("name", "value");

    ParamValueProcessor processor = createProcessor("name", String.class);
    processor.setValue(clientRequest, "value");
    Assertions.assertEquals("value", forms.get("name"));
  }

  @Test
  public void testSetValueDate() throws Exception {
    Date date = new Date();

    Mockito.doAnswer(invocation -> {
      forms.put("name", date);
      return null;
    }).when(clientRequest).addForm("name", date);
    ParamValueProcessor processor = createProcessor("name", Date.class);
    processor.setValue(clientRequest, date);
    Assertions.assertSame(date, forms.get("name"));
  }

  @Test
  public void testGetProcessorType() {
    ParamValueProcessor processor = createProcessor("name", String.class);
    Assertions.assertEquals("formData", processor.getProcessorType());
  }
}
