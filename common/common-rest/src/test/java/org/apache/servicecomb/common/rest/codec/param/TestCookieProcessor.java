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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.param.CookieProcessorCreator.CookieProcessor;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestCookieProcessor {
  @Mocked
  HttpServletRequest request;

  Map<String, String> cookies = new HashMap<>();

  RestClientRequest clientRequest;

  private CookieProcessor createProcessor(String name, Class<?> type) {
    return new CookieProcessor(name, TypeFactory.defaultInstance().constructType(type));
  }

  private void createClientRequest() {
    clientRequest = new MockUp<RestClientRequest>() {
      @Mock
      void addCookie(String name, String value) {
        cookies.put(name, value);
      }
    }.getMockInstance();
  }

  @Test
  public void testGetValueNoCookies() throws Exception {
    new Expectations() {
      {
        request.getCookies();
        result = null;
      }
    };

    CookieProcessor processor = createProcessor("c1", String.class);
    Object value = processor.getValue(request);
    Assert.assertNull(value);
  }

  @Test
  public void testGetValueCookiesNotFound() throws Exception {
    Cookie[] cookies = new Cookie[] {new Cookie("c1", "c1v")};
    new Expectations() {
      {
        request.getCookies();
        result = cookies;
      }
    };

    CookieProcessor processor = createProcessor("c2", String.class);
    Object value = processor.getValue(request);
    Assert.assertNull(value);
  }

  @Test
  public void testGetValueCookiesFound() throws Exception {
    Cookie[] cookies = new Cookie[] {new Cookie("c1", "c1v")};
    new Expectations() {
      {
        request.getCookies();
        result = cookies;
      }
    };

    CookieProcessor processor = createProcessor("c1", String.class);
    Object value = processor.getValue(request);
    Assert.assertEquals("c1v", value);
  }

  @Test
  public void testGetValueCookiesDate() throws Exception {
    Date date = new Date();
    String strDate = ISO8601Utils.format(date);
    Cookie[] cookies = new Cookie[] {new Cookie("c1", strDate)};
    new Expectations() {
      {
        request.getCookies();
        result = cookies;
      }
    };

    CookieProcessor processor = createProcessor("c1", Date.class);
    Object value = processor.getValue(request);
    Assert.assertEquals(strDate, ISO8601Utils.format((Date) value));
  }

  @Test
  public void testSetValue() throws Exception {
    createClientRequest();

    CookieProcessor processor = createProcessor("c1", String.class);
    processor.setValue(clientRequest, "c1v");
    Assert.assertEquals("c1v", cookies.get("c1"));
  }

  @Test
  public void testSetValueDate() throws Exception {
    Date date = new Date();
    String strDate = ISO8601Utils.format(date);

    createClientRequest();

    CookieProcessor processor = createProcessor("h1", Date.class);
    processor.setValue(clientRequest, date);
    Assert.assertEquals(strDate, cookies.get("h1"));
  }

  @Test
  public void testGetProcessorType() {
    CookieProcessor processor = createProcessor("c1", String.class);
    Assert.assertEquals("cookie", processor.getProcessorType());
  }
}
