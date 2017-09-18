/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.provider.springmvc.reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.common.rest.RestConst;

public class TestClientToHttpServletRequest {
  @Test
  public void testConstructFormTrue() {
    Map<String, Object> forms = new HashMap<>();
    HttpServletRequest request = new ClientToHttpServletRequest(null, null, null, forms, true);

    Assert.assertEquals(null, request.getAttribute(RestConst.BODY_PARAMETER));
    Assert.assertEquals(forms, request.getAttribute(RestConst.FORM_PARAMETERS));
  }

  @Test
  public void testConstructFormFalse() {
    Object body = new Object();
    HttpServletRequest request = new ClientToHttpServletRequest(null, null, null, body, false);

    Assert.assertEquals(null, request.getAttribute(RestConst.FORM_PARAMETERS));
    Assert.assertEquals(body, request.getAttribute(RestConst.BODY_PARAMETER));
  }

  @Test
  public void testConstructPath() {
    Map<String, String> pathParams = new HashMap<>();
    HttpServletRequest request = new ClientToHttpServletRequest(pathParams, null, null, null, false);

    Assert.assertEquals(pathParams, request.getAttribute(RestConst.PATH_PARAMETERS));
  }

  @Test
  public void testGetContentType() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    httpHeaders.put(HttpHeaders.CONTENT_TYPE, Arrays.asList("json"));

    HttpServletRequest request = new ClientToHttpServletRequest(null, null, httpHeaders, null, false);
    Assert.assertEquals("json", request.getContentType());
  }

  @Test
  public void testGetParameterNormal() {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("name", Arrays.asList("value"));

    HttpServletRequest request = new ClientToHttpServletRequest(null, queryParams, null, null, false);
    Assert.assertEquals("value", request.getParameter("name"));
  }

  @Test
  public void testGetParameterEmpty() {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("name", Arrays.asList());

    HttpServletRequest request = new ClientToHttpServletRequest(null, queryParams, null, null, false);
    Assert.assertEquals(null, request.getParameter("name"));
  }

  @Test
  public void testGetParameterNull() {
    Map<String, List<String>> queryParams = new HashMap<>();

    HttpServletRequest request = new ClientToHttpServletRequest(null, queryParams, null, null, false);
    Assert.assertEquals(null, request.getParameter("name"));
  }

  @Test
  public void testGetParameterValuesNormal() {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("name", Arrays.asList("value"));

    HttpServletRequest request = new ClientToHttpServletRequest(null, queryParams, null, null, false);
    Assert.assertThat(request.getParameterValues("name"), Matchers.arrayContaining("value"));
  }

  @Test
  public void testGetParameterValuesEmpty() {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("name", Arrays.asList());

    HttpServletRequest request = new ClientToHttpServletRequest(null, queryParams, null, null, false);
    Assert.assertArrayEquals(null, request.getParameterValues("name"));
  }

  @Test
  public void testGetParameterValuesNull() {
    Map<String, List<String>> queryParams = new HashMap<>();

    HttpServletRequest request = new ClientToHttpServletRequest(null, queryParams, null, null, false);
    Assert.assertArrayEquals(null, request.getParameterValues("name"));
  }

  @Test
  public void testGetHeaderNormal() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    httpHeaders.put("name", Arrays.asList("value"));

    HttpServletRequest request = new ClientToHttpServletRequest(null, null, httpHeaders, null, false);
    Assert.assertEquals("value", request.getHeader("name"));
  }

  @Test
  public void testGetHeaderEmpty() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    httpHeaders.put("name", Arrays.asList());

    HttpServletRequest request = new ClientToHttpServletRequest(null, null, httpHeaders, null, false);
    Assert.assertEquals(null, request.getHeader("name"));
  }

  @Test
  public void testGetHeaderNull() {
    Map<String, List<String>> httpHeaders = new HashMap<>();

    HttpServletRequest request = new ClientToHttpServletRequest(null, null, httpHeaders, null, false);
    Assert.assertEquals(null, request.getHeader("name"));
  }

  @Test
  public void testGetHeadersNormal() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    httpHeaders.put("name", Arrays.asList("value"));

    HttpServletRequest request = new ClientToHttpServletRequest(null, null, httpHeaders, null, false);
    Assert.assertThat(Collections.list(request.getHeaders("name")), Matchers.contains("value"));
  }

  @Test
  public void testGetHeadersEmpty() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    httpHeaders.put("name", Arrays.asList());

    HttpServletRequest request = new ClientToHttpServletRequest(null, null, httpHeaders, null, false);
    Assert.assertEquals(null, request.getHeaders("name"));
  }

  @Test
  public void testGetHeadersNull() {
    Map<String, List<String>> httpHeaders = new HashMap<>();

    HttpServletRequest request = new ClientToHttpServletRequest(null, null, httpHeaders, null, false);
    Assert.assertEquals(null, request.getHeaders("name"));
  }

  @Test
  public void testGetCookiesNull() {
    Map<String, List<String>> httpHeaders = new HashMap<>();

    HttpServletRequest request = new ClientToHttpServletRequest(null, null, httpHeaders, null, false);
    Assert.assertEquals(0, request.getCookies().length);
  }

  @Test
  public void testGetCookiesNormal() {
    Map<String, List<String>> httpHeaders = new HashMap<>();
    httpHeaders.put(HttpHeaders.COOKIE, Arrays.asList("k1=v1;k2=v2;"));

    HttpServletRequest request = new ClientToHttpServletRequest(null, null, httpHeaders, null, false);
    Cookie[] cookies = request.getCookies();
    Assert.assertSame(cookies, request.getCookies());
    Assert.assertEquals(1, cookies.length);
    Assert.assertEquals("k1", cookies[0].getName());
    Assert.assertEquals("v1", cookies[0].getValue());
  }
}
