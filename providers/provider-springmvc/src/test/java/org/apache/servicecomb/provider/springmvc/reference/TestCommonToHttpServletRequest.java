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

package org.apache.servicecomb.provider.springmvc.reference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.core.HttpHeaders;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestCommonToHttpServletRequest {
  @Test
  public void testConstructFormTrue() {
    Map<String, Object> forms = new HashMap<>();
    HttpServletRequest request = new CommonToHttpServletRequest(null, null, null, forms, true);

    Assertions.assertNull(request.getAttribute(RestConst.BODY_PARAMETER));
    Assertions.assertEquals(forms, request.getAttribute(RestConst.FORM_PARAMETERS));
  }

  @Test
  public void testConstructFormFalse() {
    Object body = new Object();
    HttpServletRequest request = new CommonToHttpServletRequest(null, null, null, body, false);

    Assertions.assertNull(request.getAttribute(RestConst.FORM_PARAMETERS));
    Assertions.assertEquals(body, request.getAttribute(RestConst.BODY_PARAMETER));
  }

  @Test
  public void testConstructNormal() {
    List<String> fileKeys = new ArrayList<>();
    fileKeys.add("test1");
    fileKeys.add("test2");
    HttpServletRequest request = new CommonToHttpServletRequest(null, null, null, null, false, fileKeys);
    Assertions.assertEquals(2, ((CommonToHttpServletRequest) request).getFileKeys().size());
    Assertions.assertEquals("test1", ((CommonToHttpServletRequest) request).getFileKeys().get(0));
    Assertions.assertEquals("test2", ((CommonToHttpServletRequest) request).getFileKeys().get(1));
  }

  @Test
  public void testConstructPath() {
    Map<String, String> pathParams = new HashMap<>();
    HttpServletRequest request = new CommonToHttpServletRequest(pathParams, null, null, null, false);

    Assertions.assertEquals(pathParams, request.getAttribute(RestConst.PATH_PARAMETERS));
  }

  @Test
  public void testGetContentType() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    httpHeaders.put(HttpHeaders.CONTENT_TYPE, Arrays.asList("json"));

    HttpServletRequest request = new CommonToHttpServletRequest(null, null, httpHeaders, null, false);
    Assertions.assertEquals("json", request.getContentType());
  }

  @Test
  public void testGetParameterNormal() {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("name", Arrays.asList("value"));

    HttpServletRequest request = new CommonToHttpServletRequest(null, queryParams, null, null, false);
    Assertions.assertEquals("value", request.getParameter("name"));
  }

  @Test
  public void testGetParameterEmpty() {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("name", Arrays.asList());

    HttpServletRequest request = new CommonToHttpServletRequest(null, queryParams, null, null, false);
    Assertions.assertNull(request.getParameter("name"));
  }

  @Test
  public void testGetParameterNull() {
    Map<String, List<String>> queryParams = new HashMap<>();

    HttpServletRequest request = new CommonToHttpServletRequest(null, queryParams, null, null, false);
    Assertions.assertNull(request.getParameter("name"));
  }

  @Test
  public void testGetParameterValuesNormal() {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("name", Arrays.asList("value"));

    HttpServletRequest request = new CommonToHttpServletRequest(null, queryParams, null, null, false);
    MatcherAssert.assertThat(request.getParameterValues("name"), Matchers.arrayContaining("value"));
  }

  @Test
  public void testGetParameterValuesEmpty() {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("name", Arrays.asList());

    HttpServletRequest request = new CommonToHttpServletRequest(null, queryParams, null, null, false);
    Assertions.assertArrayEquals(new String[0], request.getParameterValues("name"));
  }

  @Test
  public void testGetParameterValuesNull() {
    Map<String, List<String>> queryParams = new HashMap<>();

    HttpServletRequest request = new CommonToHttpServletRequest(null, queryParams, null, null, false);
    Assertions.assertArrayEquals(new String[0], request.getParameterValues("name"));
  }

  @Test
  public void testGetHeaderNormal() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    httpHeaders.put("name", Arrays.asList("value"));

    HttpServletRequest request = new CommonToHttpServletRequest(null, null, httpHeaders, null, false);
    Assertions.assertEquals("value", request.getHeader("name"));
  }

  @Test
  public void testGetHeaderEmpty() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    httpHeaders.put("name", Arrays.asList());

    HttpServletRequest request = new CommonToHttpServletRequest(null, null, httpHeaders, null, false);
    Assertions.assertNull(request.getHeader("name"));
  }

  @Test
  public void testGetHeaderNamesNormal() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    httpHeaders.put("name", Arrays.asList("value"));

    HttpServletRequest request = new CommonToHttpServletRequest(null, null, httpHeaders, null, false);
    MatcherAssert.assertThat(Collections.list(request.getHeaderNames()), Matchers.contains("name"));
  }

  @Test
  public void testGetHeaderNamesEmpty() {
    Map<String, List<String>> httpHeaders = new HashMap<>();

    HttpServletRequest request = new CommonToHttpServletRequest(null, null, httpHeaders, null, false);
    Assertions.assertFalse(request.getHeaderNames().hasMoreElements());
  }

  @Test
  public void testGetHeaderNull() {
    Map<String, List<String>> httpHeaders = new HashMap<>();

    HttpServletRequest request = new CommonToHttpServletRequest(null, null, httpHeaders, null, false);
    Assertions.assertNull(request.getHeader("name"));
  }

  @Test
  public void testGetHeadersNormal() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    httpHeaders.put("name", Arrays.asList("value"));

    HttpServletRequest request = new CommonToHttpServletRequest(null, null, httpHeaders, null, false);
    MatcherAssert.assertThat(Collections.list(request.getHeaders("name")), Matchers.contains("value"));
  }

  @Test
  public void testGetHeadersEmpty() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    httpHeaders.put("name", Arrays.asList());

    HttpServletRequest request = new CommonToHttpServletRequest(null, null, httpHeaders, null, false);
    Assertions.assertFalse(request.getHeaders("name").hasMoreElements());
  }

  @Test
  public void testGetHeadersNull() {
    Map<String, List<String>> httpHeaders = new HashMap<>();

    HttpServletRequest request = new CommonToHttpServletRequest(null, null, httpHeaders, null, false);
    Assertions.assertFalse(request.getHeaders("name").hasMoreElements());
  }

  @Test
  public void testGetCookiesNull() {
    Map<String, List<String>> httpHeaders = new HashMap<>();

    HttpServletRequest request = new CommonToHttpServletRequest(null, null, httpHeaders, null, false);
    Assertions.assertEquals(0, request.getCookies().length);
  }

  @Test
  public void testGetCookiesNormal() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    httpHeaders.put(HttpHeaders.COOKIE, Arrays.asList("k1=v1;k2=v2;"));

    HttpServletRequest request = new CommonToHttpServletRequest(null, null, httpHeaders, null, false);
    Cookie[] cookies = request.getCookies();
    Assertions.assertSame(cookies, request.getCookies());
    Assertions.assertEquals(1, cookies.length);
    Assertions.assertEquals("k1", cookies[0].getName());
    Assertions.assertEquals("v1", cookies[0].getValue());
  }

  @Test
  public void testGetInputStream() throws IOException {
    HttpServletRequest request = new CommonToHttpServletRequest(null, null, null, null, false);
    Assertions.assertNull(request.getInputStream());
  }

  @Test
  public void testSetHeader() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    HttpServletRequestEx request = new CommonToHttpServletRequest(null, null, httpHeaders, null, false);
    request.setHeader("name", "v1");
    request.setHeader("name", "v2");
    Assertions.assertEquals("v2", request.getHeader("name"));
  }

  @Test
  public void testAddHeader() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    HttpServletRequestEx request = new CommonToHttpServletRequest(null, null, httpHeaders, null, false);
    request.addHeader("name", "v1");
    request.addHeader("name", "v2");
    MatcherAssert.assertThat(Collections.list(request.getHeaders("name")), Matchers.contains("v1", "v2"));
  }

  @Test
  public void testGetParts() {
    List<String> restParams = new ArrayList<>();
    restParams.add("test1");
    restParams.add("test2");
    File file1 = new File("file1.txt");
    File file2 = new File("file2.txt");
    File[] files = {file1, file2};
    List<File> list = Arrays.asList(files);
    Map<String, Object> objectMap = new HashMap<>();
    objectMap.put("test1", list);
    objectMap.put("test2", files);
    objectMap.put("test3", list);
    objectMap.put("test4", "haha");

    Map<String, String> pathParams = new HashMap<>();
    HttpServletRequest request = new CommonToHttpServletRequest(pathParams, null, null, objectMap, true, restParams);
    try {
      Collection<Part> tmpParts = request.getParts();
      ArrayList<Part> parts = new ArrayList<>(tmpParts);
      Assertions.assertEquals(4, parts.size());
      Assertions.assertEquals("test1", parts.get(0).getName());
      Assertions.assertEquals("test1", parts.get(1).getName());
      Assertions.assertEquals("test2", parts.get(2).getName());
      Assertions.assertEquals("test2", parts.get(3).getName());
    } catch (Throwable e) {
      Assertions.fail("should not throw exception");
    }
  }
}
