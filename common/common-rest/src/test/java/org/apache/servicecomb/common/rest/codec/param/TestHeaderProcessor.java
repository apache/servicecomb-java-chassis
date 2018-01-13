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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.param.HeaderProcessorCreator.HeaderProcessor;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestHeaderProcessor {
  @Mocked
  HttpServletRequest request;

  Map<String, String> headers = new HashMap<>();

  RestClientRequest clientRequest;

  private HeaderProcessor createProcessor(String name, Class<?> type) {
    return new HeaderProcessor(name, TypeFactory.defaultInstance().constructType(type));
  }

  private void createClientRequest() {
    clientRequest = new MockUp<RestClientRequest>() {
      @Mock
      void putHeader(String name, String value) {
        headers.put(name, value);
      }
    }.getMockInstance();
  }

  @Test
  public void testGetValueNormal() throws Exception {
    new Expectations() {
      {
        request.getHeader("h1");
        result = "h1v";
      }
    };

    HeaderProcessor processor = createProcessor("h1", String.class);
    Object value = processor.getValue(request);
    Assert.assertEquals("h1v", value);
  }


  @Test
  public void testGetValueNormalDate() throws Exception {
    Date date = new Date();
    String strDate = ISO8601Utils.format(date);
    new Expectations() {
      {
        request.getHeader("h1");
        result = strDate;
      }
    };

    HeaderProcessor processor = createProcessor("h1", Date.class);
    Object value = processor.getValue(request);
    Assert.assertEquals(strDate, ISO8601Utils.format((Date) value));
  }

  @Test
  public void testGetValueContainerTypeNull() throws Exception {
    new Expectations() {
      {
        request.getHeaders("h1");
        result = null;
      }
    };

    HeaderProcessor processor = createProcessor("h1", String[].class);
    String[] value = (String[]) processor.getValue(request);
    Assert.assertNull(value);
  }

  @Test
  public void testGetValueArray() throws Exception {
    new Expectations() {
      {
        request.getHeaders("h1");
        result = Collections.enumeration(Arrays.asList("h1v"));
      }
    };

    HeaderProcessor processor = createProcessor("h1", String[].class);
    String[] value = (String[]) processor.getValue(request);
    Assert.assertThat(value, Matchers.arrayContaining("h1v"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetValueList() throws Exception {
    new Expectations() {
      {
        request.getHeaders("h1");
        result = Collections.enumeration(Arrays.asList("h1v"));
      }
    };

    HeaderProcessor processor =
        new HeaderProcessor("h1", TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
    Object value = processor.getValue(request);
    Assert.assertThat((List<String>) value, Matchers.contains("h1v"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetValueSet() throws Exception {
    new Expectations() {
      {
        request.getHeaders("h1");
        result = Collections.enumeration(Arrays.asList("h1v"));
      }
    };

    HeaderProcessor processor =
        new HeaderProcessor("h1", TypeFactory.defaultInstance().constructCollectionType(Set.class, String.class));
    Object value = processor.getValue(request);
    Assert.assertThat((Set<String>) value, Matchers.contains("h1v"));
  }

  @Test
  public void testSetValue() throws Exception {
    createClientRequest();

    HeaderProcessor processor = createProcessor("h1", String.class);
    processor.setValue(clientRequest, "h1v");
    Assert.assertEquals("h1v", headers.get("h1"));
  }

  @Test
  public void testSetValueNull() throws Exception {
    createClientRequest();
    HeaderProcessor processor = createProcessor("h1", String.class);
    processor.setValue(clientRequest, null);
    Assert.assertEquals(0, headers.size());
  }

  @Test
  public void testSetValueDate() throws Exception {
    Date date = new Date();
    String strDate = ISO8601Utils.format(date);

    createClientRequest();

    HeaderProcessor processor = createProcessor("h1", Date.class);
    processor.setValue(clientRequest, date);
    Assert.assertEquals(strDate, headers.get("h1"));
  }

  @Test
  public void testGetProcessorType() {
    HeaderProcessor processor = createProcessor("h1", String.class);
    Assert.assertEquals("header", processor.getProcessorType());
  }
}
