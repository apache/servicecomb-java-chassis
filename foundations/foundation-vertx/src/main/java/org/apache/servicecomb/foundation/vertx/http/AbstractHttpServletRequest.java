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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

public abstract class AbstractHttpServletRequest extends BodyBufferSupportImpl implements HttpServletRequestEx {
  private Map<String, Object> attributeMap = new HashMap<>();

  @Override
  public Object getAttribute(String name) {
    return attributeMap.get(name);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return Collections.enumeration(attributeMap.keySet());
  }

  @Override
  public String getCharacterEncoding() {
    throw new Error("not supported method");
  }

  @Override
  public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
    throw new Error("not supported method");
  }

  @Override
  public int getContentLength() {
    throw new Error("not supported method");
  }

  @Override
  public long getContentLengthLong() {
    throw new Error("not supported method");
  }

  @Override
  public String getContentType() {
    throw new Error("not supported method");
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    throw new Error("not supported method");
  }

  @Override
  public String getParameter(String name) {
    throw new Error("not supported method");
  }

  @Override
  public Enumeration<String> getParameterNames() {
    throw new Error("not supported method");
  }

  @Override
  public String[] getParameterValues(String name) {
    throw new Error("not supported method");
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    throw new Error("not supported method");
  }

  @Override
  public String getProtocol() {
    throw new Error("not supported method");
  }

  @Override
  public String getScheme() {
    throw new Error("not supported method");
  }

  @Override
  public String getServerName() {
    throw new Error("not supported method");
  }

  @Override
  public int getServerPort() {
    throw new Error("not supported method");
  }

  @Override
  public BufferedReader getReader() throws IOException {
    throw new Error("not supported method");
  }

  @Override
  public String getRemoteAddr() {
    throw new Error("not supported method");
  }

  @Override
  public String getRemoteHost() {
    throw new Error("not supported method");
  }

  @Override
  public void setAttribute(String name, Object o) {
    attributeMap.put(name, o);
  }

  @Override
  public void removeAttribute(String name) {
    attributeMap.remove(name);
  }

  @Override
  public Locale getLocale() {
    throw new Error("not supported method");
  }

  @Override
  public Enumeration<Locale> getLocales() {
    throw new Error("not supported method");
  }

  @Override
  public boolean isSecure() {
    throw new Error("not supported method");
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String path) {
    throw new Error("not supported method");
  }

  @Override
  @Deprecated
  public String getRealPath(String path) {
    throw new Error("not supported method");
  }

  @Override
  public int getRemotePort() {
    throw new Error("not supported method");
  }

  @Override
  public String getLocalName() {
    throw new Error("not supported method");
  }

  @Override
  public String getLocalAddr() {
    throw new Error("not supported method");
  }

  @Override
  public int getLocalPort() {
    throw new Error("not supported method");
  }

  @Override
  public ServletContext getServletContext() {
    throw new Error("not supported method");
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    throw new Error("not supported method");
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
      throws IllegalStateException {
    throw new Error("not supported method");
  }

  @Override
  public boolean isAsyncStarted() {
    throw new Error("not supported method");
  }

  @Override
  public boolean isAsyncSupported() {
    throw new Error("not supported method");
  }

  @Override
  public AsyncContext getAsyncContext() {
    throw new Error("not supported method");
  }

  @Override
  public DispatcherType getDispatcherType() {
    throw new Error("not supported method");
  }

  @Override
  public String getAuthType() {
    throw new Error("not supported method");
  }

  @Override
  public Cookie[] getCookies() {
    throw new Error("not supported method");
  }

  @Override
  public long getDateHeader(String name) {
    throw new Error("not supported method");
  }

  @Override
  public String getHeader(String name) {
    throw new Error("not supported method");
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    throw new Error("not supported method");
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    throw new Error("not supported method");
  }

  @Override
  public int getIntHeader(String name) {
    throw new Error("not supported method");
  }

  @Override
  public String getMethod() {
    throw new Error("not supported method");
  }

  @Override
  public String getPathInfo() {
    throw new Error("not supported method");
  }

  @Override
  public String getPathTranslated() {
    throw new Error("not supported method");
  }

  @Override
  public String getContextPath() {
    throw new Error("not supported method");
  }

  @Override
  public String getQueryString() {
    throw new Error("not supported method");
  }

  @Override
  public String getRemoteUser() {
    throw new Error("not supported method");
  }

  @Override
  public boolean isUserInRole(String role) {
    throw new Error("not supported method");
  }

  @Override
  public Principal getUserPrincipal() {
    throw new Error("not supported method");
  }

  @Override
  public String getRequestedSessionId() {
    throw new Error("not supported method");
  }

  @Override
  public String getRequestURI() {
    throw new Error("not supported method");
  }

  @Override
  public StringBuffer getRequestURL() {
    throw new Error("not supported method");
  }

  @Override
  public String getServletPath() {
    throw new Error("not supported method");
  }

  @Override
  public HttpSession getSession(boolean create) {
    throw new Error("not supported method");
  }

  @Override
  public HttpSession getSession() {
    throw new Error("not supported method");
  }

  @Override
  public String changeSessionId() {
    throw new Error("not supported method");
  }

  @Override
  public boolean isRequestedSessionIdValid() {
    throw new Error("not supported method");
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {
    throw new Error("not supported method");
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {
    throw new Error("not supported method");
  }

  @Override
  @Deprecated
  public boolean isRequestedSessionIdFromUrl() {
    throw new Error("not supported method");
  }

  @Override
  public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
    throw new Error("not supported method");
  }

  @Override
  public void login(String username, String password) throws ServletException {
    throw new Error("not supported method");
  }

  @Override
  public void logout() throws ServletException {
    throw new Error("not supported method");
  }

  @Override
  public Collection<Part> getParts() throws IOException, ServletException {
    throw new Error("not supported method");
  }

  @Override
  public Part getPart(String name) throws IOException, ServletException {
    throw new Error("not supported method");
  }

  @Override
  public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
    throw new Error("not supported method");
  }
}
