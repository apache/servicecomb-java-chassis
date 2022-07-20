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
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.common.rest.codec.param.CookieProcessorCreator.CookieProcessor;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestCookieProcessor {
  final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

  final Map<String, String> cookies = new HashMap<>();

  final RestClientRequest clientRequest = Mockito.mock(RestClientRequest.class);

  final String cookieName = "v1";

  final String cookieValue = "c1v";

  private CookieProcessor createProcessor(String name, Class<?> type) {
    return new CookieProcessor(name, TypeFactory.defaultInstance().constructType(type), null, true);
  }

  private CookieProcessor createProcessor(String name, Class<?> type, String defaultValue, boolean required) {
    return new CookieProcessor(name, TypeFactory.defaultInstance().constructType(type), defaultValue, required);
  }

  private void createClientRequest() {
    Mockito.doAnswer(invocation -> {
      cookies.put(cookieName, cookieValue);
      return null;
    }).when(clientRequest).addCookie(cookieName, cookieValue);
  }

  @Test
  public void testGetValueNoCookies() throws Exception {
    Mockito.when(request.getCookies()).thenReturn(null);

    CookieProcessor processor = createProcessor(cookieName, String.class, null, false);
    Object value = processor.getValue(request);
    Assertions.assertNull(value);
  }

  @Test
  public void testNoCookieAndRequired() throws Exception {
    Mockito.when(request.getCookies()).thenReturn(null);

    CookieProcessor processor = createProcessor(cookieName, String.class, null, true);
    try {
      processor.getValue(request);
      Assertions.assertEquals("required is true, throw exception", "not throw exception");
    } catch (Exception e) {
      Assertions.assertTrue(e.getMessage().contains("Parameter is required."));
    }
  }

  @Test
  public void testGetValueCookiesNotFound() throws Exception {
    Cookie[] cookies = new Cookie[] {new Cookie(cookieName, cookieValue)};
    Mockito.when(request.getCookies()).thenReturn(cookies);

    CookieProcessor processor = createProcessor("c2", String.class, null, false);
    Object value = processor.getValue(request);
    Assertions.assertNull(value);
  }

  @Test
  public void testGetValueCookiesFound() throws Exception {
    Cookie[] cookies = new Cookie[] {new Cookie(cookieName, cookieValue)};
    Mockito.when(request.getCookies()).thenReturn(cookies);

    CookieProcessor processor = createProcessor(cookieName, String.class);
    Object value = processor.getValue(request);
    Assertions.assertEquals(cookieValue, value);
  }

  @Test
  public void testGetValueRequiredTrue() throws Exception {
    Cookie[] cookies = new Cookie[] {new Cookie(cookieName, null)};
    Mockito.when(request.getCookies()).thenReturn(cookies);

    CookieProcessor processor = createProcessor(cookieName, String.class, null, true);
    try {
      processor.getValue(request);
      Assertions.assertEquals("required is true, throw exception", "not throw exception");
    } catch (Exception e) {
      Assertions.assertTrue(e.getMessage().contains("Parameter is required."));
    }
  }

  @Test
  public void testGetValueRequiredFalse() throws Exception {
    Cookie[] cookies = new Cookie[] {new Cookie(cookieName, null)};
    Mockito.when(request.getCookies()).thenReturn(cookies);

    CookieProcessor processor = createProcessor(cookieName, String.class, "test", false);
    Object result = processor.getValue(request);
    Assertions.assertEquals("test", result);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetValueCookiesDate() throws Exception {
    Date date = new Date();
    String strDate = com.fasterxml.jackson.databind.util.ISO8601Utils.format(date);
    Cookie[] cookies = new Cookie[] {new Cookie(cookieName, strDate)};
    Mockito.when(request.getCookies()).thenReturn(cookies);

    CookieProcessor processor = createProcessor(cookieName, Date.class);
    Object value = processor.getValue(request);
    Assertions.assertEquals(strDate, com.fasterxml.jackson.databind.util.ISO8601Utils.format((Date) value));
  }

  @Test
  public void testSetValue() throws Exception {
    createClientRequest();

    CookieProcessor processor = createProcessor(cookieName, String.class);
    processor.setValue(clientRequest, cookieValue);
    Assertions.assertEquals(cookieValue, cookies.get(cookieName));
  }

  @Test
  public void testSetValueDateFixed() throws Exception {
    Date date = new Date(1586957400199L);
    String strDate =  "2020-04-15T13:30:00.199+00:00";

    String cookieValue = RestObjectMapperFactory.getConsumerWriterMapper().convertToString(date);
    Mockito.doAnswer(invocation -> {
      cookies.put("h1", cookieValue);
      return null;
    }).when(clientRequest).addCookie("h1", cookieValue);
    CookieProcessor processor = createProcessor("h1", Date.class);
    processor.setValue(clientRequest, date);
    Assertions.assertEquals(strDate, cookies.get("h1"));
  }

  @Test
  public void testSetValueDate() throws Exception {
    Date date = new Date();

    String strDate =  new StdDateFormat().format(date);

    String cookieValue = RestObjectMapperFactory.getConsumerWriterMapper().convertToString(date);
    Mockito.doAnswer(invocation -> {
      cookies.put("h1", cookieValue);
      return null;
    }).when(clientRequest).addCookie("h1", cookieValue);
    CookieProcessor processor = createProcessor("h1", Date.class);
    processor.setValue(clientRequest, date);
    Assertions.assertEquals(strDate, cookies.get("h1"));
  }

  @Test
  public void testGetProcessorType() {
    CookieProcessor processor = createProcessor(cookieName, String.class);
    Assertions.assertEquals("cookie", processor.getProcessorType());
  }
}
