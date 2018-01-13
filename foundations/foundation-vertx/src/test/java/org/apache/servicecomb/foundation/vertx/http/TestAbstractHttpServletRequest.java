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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestAbstractHttpServletRequest {
  HttpServletRequest request = new AbstractHttpServletRequest() {
  };

  @Test
  public void testAttribute() {
    String key = "a1";
    String value = "abc";
    request.setAttribute(key, value);
    Assert.assertSame(value, request.getAttribute(key));
    Assert.assertThat(Collections.list(request.getAttributeNames()), Matchers.contains(key));

    request.setAttribute("a2", "v");
    Assert.assertThat(Collections.list(request.getAttributeNames()), Matchers.contains(key, "a2"));

    request.removeAttribute(key);
    Assert.assertNull(request.getAttribute(key));
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private void setExceptionExpected() {
    expectedException.expect(Error.class);
    expectedException.expectMessage(Matchers.is("not supported method"));
  }

  @Test
  public void testGetCharacterEncoding() {
    setExceptionExpected();

    request.getCharacterEncoding();
  }

  @Test
  public void testSetCharacterEncoding() throws UnsupportedEncodingException {
    setExceptionExpected();

    request.setCharacterEncoding("");
  }

  @Test
  public void testGetContentLength() {
    setExceptionExpected();

    request.getContentLength();
  }

  @Test
  public void testGetContentLengthLong() {
    setExceptionExpected();

    request.getContentLengthLong();
  }

  @Test
  public void testGetContentType() {
    setExceptionExpected();

    request.getContentType();
  }

  @Test
  public void testGetInputStream() throws IOException {
    setExceptionExpected();

    request.getInputStream();
  }

  @Test
  public void testGetParameter() {
    setExceptionExpected();

    request.getParameter("");
  }

  @Test
  public void testGetParameterNames() {
    setExceptionExpected();

    request.getParameterNames();
  }

  @Test
  public void testGetParameterValues() {
    setExceptionExpected();

    request.getParameterValues("");
  }

  @Test
  public void testGetParameterMap() {
    setExceptionExpected();

    request.getParameterMap();
  }

  @Test
  public void testGetProtocol() {
    setExceptionExpected();

    request.getProtocol();
  }

  @Test
  public void testGetScheme() {
    setExceptionExpected();

    request.getScheme();
  }

  @Test
  public void testGetServerName() {
    setExceptionExpected();

    request.getServerName();
  }

  @Test
  public void testGetServerPort() {
    setExceptionExpected();

    request.getServerPort();
  }

  @Test
  public void testGetReader() throws IOException {
    setExceptionExpected();

    request.getReader();
  }

  @Test
  public void testGetRemoteAddr() {
    setExceptionExpected();

    request.getRemoteAddr();
  }

  @Test
  public void testGetRemoteHost() {
    setExceptionExpected();

    request.getRemoteHost();
  }

  @Test
  public void testGetLocale() {
    setExceptionExpected();

    request.getLocale();
  }

  @Test
  public void testGetLocales() {
    setExceptionExpected();

    request.getLocales();
  }

  @Test
  public void testIsSecure() {
    setExceptionExpected();

    request.isSecure();
  }

  @Test
  public void testGetRequestDispatcher() {
    setExceptionExpected();

    request.getRequestDispatcher("");
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testGetRealPath() {
    setExceptionExpected();

    request.getRealPath("");
  }

  @Test
  public void testGetRemotePort() {
    setExceptionExpected();

    request.getRemotePort();
  }

  @Test
  public void testGetLocalName() {
    setExceptionExpected();

    request.getLocalName();
  }

  @Test
  public void testGetLocalAddr() {
    setExceptionExpected();

    request.getLocalAddr();
  }

  @Test
  public void testGetLocalPort() {
    setExceptionExpected();

    request.getLocalPort();
  }

  @Test
  public void testGetServletContext() {
    setExceptionExpected();

    request.getServletContext();
  }

  @Test
  public void testStartAsync() {
    setExceptionExpected();

    request.startAsync();
  }

  @Test
  public void testStartAsyncWithParam() {
    setExceptionExpected();

    request.startAsync(null, null);
  }

  @Test
  public void testIsAsyncStarted() {
    setExceptionExpected();

    request.isAsyncStarted();
  }

  @Test
  public void testIsAsyncSupported() {
    setExceptionExpected();

    request.isAsyncSupported();
  }

  @Test
  public void testGetAsyncContext() {
    setExceptionExpected();

    request.getAsyncContext();
  }

  @Test
  public void testGetDispatcherType() {
    setExceptionExpected();

    request.getDispatcherType();
  }

  @Test
  public void testGetAuthType() {
    setExceptionExpected();

    request.getAuthType();
  }

  @Test
  public void testGetCookies() {
    setExceptionExpected();

    request.getCookies();
  }

  @Test
  public void testGetDateHeader() {
    setExceptionExpected();

    request.getDateHeader("");
  }

  @Test
  public void testGetHeader() {
    setExceptionExpected();

    request.getHeader("");
  }

  @Test
  public void testGetHeaders() {
    setExceptionExpected();

    request.getHeaders("");
  }

  @Test
  public void testGetHeaderNames() {
    setExceptionExpected();

    request.getHeaderNames();
  }

  @Test
  public void testGetIntHeader() {
    setExceptionExpected();

    request.getIntHeader("");
  }

  @Test
  public void testGetMethod() {
    setExceptionExpected();

    request.getMethod();
  }

  @Test
  public void testGetPathInfo() {
    setExceptionExpected();

    request.getPathInfo();
  }

  @Test
  public void testGetPathTranslated() {
    setExceptionExpected();

    request.getPathTranslated();
  }

  @Test
  public void testGetContextPath() {
    setExceptionExpected();

    request.getContextPath();
  }

  @Test
  public void testGetQueryString() {
    setExceptionExpected();

    request.getQueryString();
  }

  @Test
  public void testGetRemoteUser() {
    setExceptionExpected();

    request.getRemoteUser();
  }

  @Test
  public void testIsUserInRole() {
    setExceptionExpected();

    request.isUserInRole("");
  }

  @Test
  public void testGetUserPrincipal() {
    setExceptionExpected();

    request.getUserPrincipal();
  }

  @Test
  public void testGetRequestedSessionId() {
    setExceptionExpected();

    request.getRequestedSessionId();
  }

  @Test
  public void testGetRequestURI() {
    setExceptionExpected();

    request.getRequestURI();
  }

  @Test
  public void testGetRequestURL() {
    setExceptionExpected();

    request.getRequestURL();
  }

  @Test
  public void testGetServletPath() {
    setExceptionExpected();

    request.getServletPath();
  }

  @Test
  public void testGetSessionWithParam() {
    setExceptionExpected();

    request.getSession(true);
  }

  @Test
  public void testGetSession() {
    setExceptionExpected();

    request.getSession();
  }

  @Test
  public void testChangeSessionId() {
    setExceptionExpected();

    request.changeSessionId();
  }

  @Test
  public void testIsRequestedSessionIdValid() {
    setExceptionExpected();

    request.isRequestedSessionIdValid();
  }

  @Test
  public void testIsRequestedSessionIdFromCookie() {
    setExceptionExpected();

    request.isRequestedSessionIdFromCookie();
  }

  @Test
  public void testIsRequestedSessionIdFromURL() {
    setExceptionExpected();

    request.isRequestedSessionIdFromURL();
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testIsRequestedSessionIdFromUrl() {
    setExceptionExpected();

    request.isRequestedSessionIdFromUrl();
  }

  @Test
  public void testAuthenticate() throws IOException, ServletException {
    setExceptionExpected();

    request.authenticate(null);
  }

  @Test
  public void testLogin() throws ServletException {
    setExceptionExpected();

    request.login(null, null);
  }

  @Test
  public void testLogout() throws ServletException {
    setExceptionExpected();

    request.logout();
  }

  @Test
  public void testGetParts() throws IOException, ServletException {
    setExceptionExpected();

    request.getParts();
  }

  @Test
  public void testGetPart() throws IOException, ServletException {
    setExceptionExpected();

    request.getPart("");
  }

  @Test
  public void testUpgrade() throws IOException, ServletException {
    setExceptionExpected();

    request.upgrade(null);
  }
}
