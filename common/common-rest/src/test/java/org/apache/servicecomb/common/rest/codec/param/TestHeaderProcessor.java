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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.common.rest.codec.param.HeaderProcessorCreator.HeaderProcessor;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import jakarta.servlet.http.HttpServletRequest;

public class TestHeaderProcessor {
  final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

  final Map<String, String> headers = new HashMap<>();

  final RestClientRequest clientRequest = Mockito.mock(RestClientRequest.class);

  private HeaderProcessor createProcessor(String name, Type type) {
    return createProcessor(name, type, null, true);
  }

  private HeaderProcessor createProcessor(String name, Type type, String defaultValue, boolean required) {
    JavaType javaType = TypeFactory.defaultInstance().constructType(type);

    HeaderParameter headerParameter = new HeaderParameter();
    headerParameter.setSchema(new Schema());
    headerParameter.name(name)
        .required(required);
    headerParameter.getSchema().setDefault(defaultValue);

    if (javaType.isContainerType()) {
      headerParameter.schema(new ArraySchema());
    }
    return new HeaderProcessor(headerParameter, javaType);
  }

  Environment environment = Mockito.mock(Environment.class);

  @BeforeEach
  void setUp() {
    LegacyPropertyFactory.setEnvironment(environment);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.header.ignoreRequiredCheck"
        , boolean.class, false)).thenReturn(false);
  }

  @Test
  public void testGetValueNormal() throws Exception {
    Mockito.when(request.getHeader("h1")).thenReturn("h1v");

    HeaderProcessor processor = createProcessor("h1", String.class);
    Object value = processor.getValue(request);
    Assertions.assertEquals("h1v", value);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetValueNormalDate() throws Exception {
    Date date = new Date();
    String strDate = com.fasterxml.jackson.databind.util.ISO8601Utils.format(date);
    Mockito.when(request.getHeader("h1")).thenReturn(strDate);

    HeaderProcessor processor = createProcessor("h1", Date.class);
    Object value = processor.getValue(request);
    Assertions.assertEquals(strDate, com.fasterxml.jackson.databind.util.ISO8601Utils.format((Date) value));
  }

  @Test
  public void testGetValueContainerTypeNull() throws Exception {
    Mockito.when(request.getHeader("h1")).thenReturn(null);

    HeaderProcessor processor = createProcessor("h1", String[].class, null, false);
    String[] value = (String[]) processor.getValue(request);
    Assertions.assertNull(value);
  }

  @Test
  public void testGetValueRequiredTrue() throws Exception {
    Mockito.when(request.getHeader("h1")).thenReturn(null);

    HeaderProcessor processor = createProcessor("h1", String.class);
    try {
      processor.getValue(request);
      Assertions.assertEquals("required is true, throw exception", "not throw exception");
    } catch (Exception e) {
      Assertions.assertTrue(e.getMessage().contains("Parameter is required."));
    }
  }

  @Test
  public void testGetValueRequiredFalse() throws Exception {
    Mockito.when(request.getHeader("h1")).thenReturn(null);

    HeaderProcessor processor = createProcessor("h1", String.class, "test", false);
    Object value = processor.getValue(request);
    Assertions.assertEquals("test", value);
  }

  @Test
  public void testGetValueArray() throws Exception {
    Mockito.when(request.getHeaders("h1")).thenReturn(Collections.enumeration(Arrays.asList("h1v")));

    HeaderProcessor processor = createProcessor("h1", String[].class);
    String[] value = (String[]) processor.getValue(request);
    MatcherAssert.assertThat(value, Matchers.arrayContaining("h1v"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetValueList() throws Exception {
    Mockito.when(request.getHeaders("h1")).thenReturn(Collections.enumeration(Arrays.asList("h1v")));

    HeaderProcessor processor = createProcessor("h1",
        TypeFactory.defaultInstance().constructCollectionType(List.class, String.class),
        null, true);
    Object value = processor.getValue(request);
    MatcherAssert.assertThat((List<String>) value, Matchers.contains("h1v"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetValueSet() throws Exception {
    Mockito.when(request.getHeaders("h1")).thenReturn(Collections.enumeration(Arrays.asList("h1v")));

    HeaderProcessor processor = createProcessor("h1",
        TypeFactory.defaultInstance().constructCollectionType(Set.class, String.class),
        null, true);
    Object value = processor.getValue(request);
    MatcherAssert.assertThat((Set<String>) value, Matchers.contains("h1v"));
  }

  @Test
  public void testSetValue() throws Exception {
    Mockito.doAnswer(invocation -> {
      headers.put("h1", RestObjectMapperFactory.getConsumerWriterMapper().convertToString("h1v"));
      return null;
    }).when(clientRequest).putHeader("h1", RestObjectMapperFactory.getConsumerWriterMapper().convertToString("h1v"));

    HeaderProcessor processor = createProcessor("h1", String.class);
    processor.setValue(clientRequest, "h1v");
    Assertions.assertEquals("h1v", headers.get("h1"));
  }

  @Test
  public void testSetValueNull() throws Exception {
    Mockito.doAnswer(invocation -> {
      headers.put("h1", RestObjectMapperFactory.getConsumerWriterMapper().convertToString(null));
      return null;
    }).when(clientRequest).putHeader("h1", RestObjectMapperFactory.getConsumerWriterMapper().convertToString(null));
    HeaderProcessor processor = createProcessor("h1", String.class);
    processor.setValue(clientRequest, null);
    Assertions.assertEquals(0, headers.size());
  }

  @Test
  public void testSetValueDateFixed() throws Exception {
    Date date = new Date(1586957400199L);
    String strDate = "2020-04-15T13:30:00.199+00:00";

    Mockito.doAnswer(invocation -> {
      headers.put("h1", RestObjectMapperFactory.getConsumerWriterMapper().convertToString(date));
      return null;
    }).when(clientRequest).putHeader("h1", RestObjectMapperFactory.getConsumerWriterMapper().convertToString(date));

    HeaderProcessor processor = createProcessor("h1", Date.class);
    processor.setValue(clientRequest, date);
    Assertions.assertEquals(strDate, headers.get("h1"));
  }

  @Test
  public void testSetValueDate() throws Exception {
    Date date = new Date();
    String strDate = new StdDateFormat().format(date);
    Mockito.doAnswer(invocation -> {
      headers.put("h1", RestObjectMapperFactory.getConsumerWriterMapper().convertToString(date));
      return null;
    }).when(clientRequest).putHeader("h1", RestObjectMapperFactory.getConsumerWriterMapper().convertToString(date));

    HeaderProcessor processor = createProcessor("h1", Date.class);
    processor.setValue(clientRequest, date);
    Assertions.assertEquals(strDate, headers.get("h1"));
  }

  @Test
  public void testGetProcessorType() {
    HeaderProcessor processor = createProcessor("h1", String.class);
    Assertions.assertEquals("header", processor.getProcessorType());
  }
}
