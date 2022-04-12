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

import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.common.rest.codec.param.QueryProcessorCreator.QueryProcessor;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import mockit.Expectations;
import mockit.Mocked;

public class TestQueryProcessor {
  @Mocked
  HttpServletRequest request;

  private ParamValueProcessor createProcessor(String name, Class<?> type, String collectionFormat) {
    return createProcessor(name, type, null, true, collectionFormat);
  }

  private ParamValueProcessor createProcessor(String name, Class<?> type, String defaultValue, boolean required,
      String collectionFormat) {
    JavaType javaType = TypeFactory.defaultInstance().constructType(type);

    QueryParameter queryParameter = new QueryParameter();
    queryParameter.name(name)
        .required(required)
        .collectionFormat(collectionFormat)
        .setDefaultValue(defaultValue);

    if (javaType.isContainerType()) {
      queryParameter.type(ArrayProperty.TYPE);
    }
    return new QueryProcessor(queryParameter, javaType);
  }

  @Test
  public void testGetValueNormal() throws Exception {
    new Expectations() {
      {
        request.getParameter("name");
        result = "value";
      }
    };

    ParamValueProcessor processor = createProcessor("name", String.class, "multi");
    Object value = processor.getValue(request);
    Assert.assertEquals("value", value);
  }

  @Test
  public void testGetValueContainerType() throws Exception {
    new Expectations() {
      {
        request.getParameterValues("name");
        result = new String[] {"value", "value2"};
      }
    };

    ParamValueProcessor processor = createProcessor("name", String[].class, "multi");
    String[] value = (String[]) processor.getValue(request);
    MatcherAssert.assertThat(value, Matchers.arrayContaining("value", "value2"));
  }

  @Test
  public void testGetValueOnCollectionFormatIsCsv() throws Exception {
    new Expectations() {
      {
        request.getParameter("name");
        result = "value2,value3";
      }
    };

    ParamValueProcessor processor = createProcessor("name", String[].class, "csv");
    String[] value = (String[]) processor.getValue(request);
    MatcherAssert.assertThat(value, Matchers.arrayContaining("value2", "value3"));
  }

  @Test
  public void testGetProcessorType() {
    ParamValueProcessor processor = createProcessor("name", String.class, "multi");
    Assert.assertEquals("query", processor.getProcessorType());
  }

  @Test
  public void testGetValueRequiredTrue() throws Exception {
    new Expectations() {
      {
        request.getParameter("name");
        result = null;
      }
    };

    ParamValueProcessor processor = createProcessor("name", String.class, "multi");
    try {
      processor.getValue(request);
      Assert.assertEquals("required is true, throw exception", "not throw exception");
    } catch (Exception e) {
      Assert.assertTrue(e.getMessage().contains("Parameter is required."));
    }
  }

  @Test
  public void testGetValueRequiredFalse() throws Exception {
    new Expectations() {
      {
        request.getParameter("name");
        result = null;
      }
    };

    ParamValueProcessor processor = createProcessor("name", String.class, "test", false, "multi");
    Object result = processor.getValue(request);
    Assert.assertEquals("test", result);
  }
}
