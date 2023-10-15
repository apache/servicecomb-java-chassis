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

import org.apache.servicecomb.common.rest.codec.param.QueryProcessorCreator.QueryProcessor;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import jakarta.servlet.http.HttpServletRequest;

public class TestQueryProcessor {
  static Environment environment = Mockito.mock(Environment.class);

  @BeforeAll
  public static void beforeClass() {
    LegacyPropertyFactory.setEnvironment(environment);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.query.emptyAsNull", boolean.class, false))
        .thenReturn(false);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.query.ignoreDefaultValue", boolean.class, false))
        .thenReturn(false);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.query.ignoreRequiredCheck", boolean.class, false))
        .thenReturn(false);
  }

  final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

  private ParamValueProcessor createProcessor(String name, Class<?> type,
      Parameter.StyleEnum style, boolean explode) {
    return createProcessor(name, type, null, true, style, explode);
  }

  private ParamValueProcessor createProcessor(String name, Class<?> type, String defaultValue, boolean required,
      Parameter.StyleEnum style, boolean explode) {
    JavaType javaType = TypeFactory.defaultInstance().constructType(type);

    QueryParameter queryParameter = new QueryParameter();
    queryParameter.name(name)
        .required(required)
        .setSchema(new StringSchema());
    queryParameter.getSchema().setDefault(defaultValue);

    if (javaType.isContainerType()) {
      queryParameter.setSchema(new ArraySchema());
      queryParameter.setExplode(explode);
      queryParameter.setStyle(style);
    }
    return new QueryProcessor(queryParameter, javaType);
  }

  @Test
  public void testGetValueNormal() throws Exception {
    Mockito.when(request.getParameter("name")).thenReturn("value");

    ParamValueProcessor processor = createProcessor("name", String.class, StyleEnum.FORM, true);
    Object value = processor.getValue(request);
    Assertions.assertEquals("value", value);
  }

  @Test
  public void testGetValueContainerType() throws Exception {
    Mockito.when(request.getParameterValues("name")).thenReturn(new String[] {"value", "value2"});

    ParamValueProcessor processor = createProcessor("name", String[].class, StyleEnum.FORM, true);
    String[] value = (String[]) processor.getValue(request);
    MatcherAssert.assertThat(value, Matchers.arrayContaining("value", "value2"));
  }

  @Test
  public void testGetValueOnCollectionFormatIsCsv() throws Exception {
    Mockito.when(request.getParameter("name")).thenReturn("value2,value3");

    ParamValueProcessor processor = createProcessor("name", String[].class, StyleEnum.FORM, false);
    String[] value = (String[]) processor.getValue(request);
    MatcherAssert.assertThat(value, Matchers.arrayContaining("value2", "value3"));
  }

  @Test
  public void testGetProcessorType() {
    ParamValueProcessor processor = createProcessor("name", String.class, StyleEnum.FORM, true);
    Assertions.assertEquals("query", processor.getProcessorType());
  }

  @Test
  public void testGetValueRequiredTrue() throws Exception {
    Mockito.when(request.getParameter("name")).thenReturn(null);

    ParamValueProcessor processor = createProcessor("name", String.class, StyleEnum.FORM, true);
    try {
      processor.getValue(request);
      Assertions.assertEquals("required is true, throw exception", "not throw exception");
    } catch (Exception e) {
      Assertions.assertTrue(e.getMessage().contains("Parameter name is required."));
    }
  }

  @Test
  public void testGetValueRequiredFalse() throws Exception {
    Mockito.when(request.getParameter("name")).thenReturn(null);

    ParamValueProcessor processor = createProcessor("name", String.class, "test", false, StyleEnum.FORM, true);
    Object result = processor.getValue(request);
    Assertions.assertEquals("test", result);
  }
}
