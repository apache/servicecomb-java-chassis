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

package org.apache.servicecomb.foundation.vertx.http;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class TestAbstractHttpServletRequest {
  HttpServletRequest request = new AbstractHttpServletRequest() {
  };

  @Test
  public void testAttribute() {
    String key = "a1";
    String value = "abc";
    request.setAttribute(key, value);
    Assertions.assertSame(value, request.getAttribute(key));
    MatcherAssert.assertThat(Collections.list(request.getAttributeNames()), Matchers.contains(key));

    request.setAttribute("a2", "v");
    MatcherAssert.assertThat(Collections.list(request.getAttributeNames()), Matchers.containsInAnyOrder(key, "a2"));

    request.removeAttribute(key);
    Assertions.assertNull(request.getAttribute(key));
  }

  private void checkError(Error error) {
    Assertions.assertEquals("not supported method", error.getMessage());
  }

  @Test
  public void testGetCharacterEncoding() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getCharacterEncoding());
    checkError(error);
  }

  @Test
  public void testSetCharacterEncoding() {
    Error error = Assertions.assertThrows(Error.class, () -> request.setCharacterEncoding(""));
    checkError(error);
  }

  @Test
  public void testGetContentLength() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getContentLength());
    checkError(error);
  }

  @Test
  public void testGetContentLengthLong() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getContentLengthLong());
    checkError(error);
  }

  @Test
  public void testGetContentType() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getContentType());
    checkError(error);
  }

  @Test
  public void testGetInputStream() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getInputStream());
    checkError(error);
  }

  @Test
  public void testGetParameter() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getParameter(""));
    checkError(error);
  }

  @Test
  public void testGetParameterNames() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getParameterNames());
    checkError(error);
  }

  @Test
  public void testGetParameterValues() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getParameterValues(""));
    checkError(error);
  }

  @Test
  public void testGetParameterMap() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getParameterMap());
    checkError(error);
  }

  @Test
  public void testGetProtocol() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getProtocol());
    checkError(error);
  }

  @Test
  public void testGetScheme() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getScheme());
    checkError(error);
  }

  @Test
  public void testGetServerName() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getServerName());
    checkError(error);
  }

  @Test
  public void testGetServerPort() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getServerPort());
    checkError(error);
  }

  @Test
  public void testGetReader() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getReader());
    checkError(error);
  }

  @Test
  public void testGetRemoteAddr() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getRemoteAddr());
    checkError(error);
  }

  @Test
  public void testGetRemoteHost() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getRemoteHost());
    checkError(error);
  }

  @Test
  public void testGetLocale() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getLocale());
    checkError(error);
  }

  @Test
  public void testGetLocales() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getLocales());
    checkError(error);
  }

  @Test
  public void testIsSecure() {
    Error error = Assertions.assertThrows(Error.class, () -> request.isSecure());
    checkError(error);
  }

  @Test
  public void testGetRequestDispatcher() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getRequestDispatcher(""));
    checkError(error);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetRealPath() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getRealPath(""));
    checkError(error);
  }

  @Test
  public void testGetRemotePort() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getRemotePort());
    checkError(error);
  }

  @Test
  public void testGetLocalName() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getLocalName());
    checkError(error);
  }

  @Test
  public void testGetLocalAddr() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getLocalAddr());
    checkError(error);
  }

  @Test
  public void testGetLocalPort() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getLocalPort());
    checkError(error);
  }

  @Test
  public void testGetServletContext() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getServletContext());
    checkError(error);
  }

  @Test
  public void testStartAsync() {
    Error error = Assertions.assertThrows(Error.class, () -> request.startAsync());
    checkError(error);
  }

  @Test
  public void testStartAsyncWithParam() {
    Error error = Assertions.assertThrows(Error.class, () -> request.startAsync(null, null));
    checkError(error);
  }

  @Test
  public void testIsAsyncStarted() {
    Error error = Assertions.assertThrows(Error.class, () -> request.isAsyncStarted());
    checkError(error);
  }

  @Test
  public void testIsAsyncSupported() {
    Error error = Assertions.assertThrows(Error.class, () -> request.isAsyncSupported());
    checkError(error);
  }

  @Test
  public void testGetAsyncContext() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getAsyncContext());
    checkError(error);
  }

  @Test
  public void testGetDispatcherType() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getDispatcherType());
    checkError(error);
  }

  @Test
  public void testGetAuthType() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getAuthType());
    checkError(error);
  }

  @Test
  public void testGetCookies() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getCookies());
    checkError(error);
  }

  @Test
  public void testGetDateHeader() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getDateHeader(""));
    checkError(error);
  }

  @Test
  public void testGetHeader() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getHeader(""));
    checkError(error);
  }

  @Test
  public void testGetHeaders() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getHeaders(""));
    checkError(error);
  }

  @Test
  public void testGetHeaderNames() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getHeaderNames());
    checkError(error);
  }

  @Test
  public void testGetIntHeader() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getIntHeader(""));
    checkError(error);
  }

  @Test
  public void testGetMethod() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getMethod());
    checkError(error);
  }

  @Test
  public void testGetPathInfo() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getPathInfo());
    checkError(error);
  }

  @Test
  public void testGetPathTranslated() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getPathTranslated());
    checkError(error);
  }

  @Test
  public void testGetContextPath() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getContextPath());
    checkError(error);
  }

  @Test
  public void testGetQueryString() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getQueryString());
    checkError(error);
  }

  @Test
  public void testGetRemoteUser() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getRemoteUser());
    checkError(error);
  }

  @Test
  public void testIsUserInRole() {
    Error error = Assertions.assertThrows(Error.class, () -> request.isUserInRole(""));
    checkError(error);
  }

  @Test
  public void testGetUserPrincipal() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getUserPrincipal());
    checkError(error);
  }

  @Test
  public void testGetRequestedSessionId() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getRequestedSessionId());
    checkError(error);
  }

  @Test
  public void testGetRequestURI() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getRequestURI());
    checkError(error);
  }

  @Test
  public void testGetRequestURL() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getRequestURL());
    checkError(error);
  }

  @Test
  public void testGetServletPath() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getServletPath());
    checkError(error);
  }

  @Test
  public void testGetSessionWithParam() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getSession(true));
    checkError(error);
  }

  @Test
  public void testGetSession() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getSession());
    checkError(error);
  }

  @Test
  public void testChangeSessionId() {
    Error error = Assertions.assertThrows(Error.class, () -> request.changeSessionId());
    checkError(error);
  }

  @Test
  public void testIsRequestedSessionIdValid() {
    Error error = Assertions.assertThrows(Error.class, () -> request.isRequestedSessionIdValid());
    checkError(error);
  }

  @Test
  public void testIsRequestedSessionIdFromCookie() {
    Error error = Assertions.assertThrows(Error.class, () -> request.isRequestedSessionIdFromCookie());
    checkError(error);
  }

  @Test
  public void testIsRequestedSessionIdFromURL() {
    Error error = Assertions.assertThrows(Error.class, () -> request.isRequestedSessionIdFromURL());
    checkError(error);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testIsRequestedSessionIdFromUrl() {
    Error error = Assertions.assertThrows(Error.class, () -> request.isRequestedSessionIdFromUrl());
    checkError(error);
  }

  @Test
  public void testAuthenticate() {
    Error error = Assertions.assertThrows(Error.class, () -> request.authenticate(null));
    checkError(error);
  }

  @Test
  public void testLogin() {
    Error error = Assertions.assertThrows(Error.class, () -> request.login(null, null));
    checkError(error);
  }

  @Test
  public void testLogout() {
    Error error = Assertions.assertThrows(Error.class, () -> request.logout());
    checkError(error);
  }

  @Test
  public void testGetParts() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getParts());
    checkError(error);
  }

  @Test
  public void testGetPart() {
    Error error = Assertions.assertThrows(Error.class, () -> request.getPart(""));
    checkError(error);
  }

  @Test
  public void testUpgrade() {
    Error error = Assertions.assertThrows(Error.class, () -> request.upgrade(null));
    checkError(error);
  }
}
