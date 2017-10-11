/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.vertx.http;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.ws.rs.core.HttpHeaders;

import io.servicecomb.foundation.vertx.stream.BufferInputStream;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.HttpServerRequestUtils;

// wrap vertx http request to Servlet http request
public class VertxServerRequestToHttpServletRequest extends AbstractHttpServletRequest {
  private static final EmptyAsyncContext EMPTY_ASYNC_CONTEXT = new EmptyAsyncContext();

  private RoutingContext context;

  private HttpServerRequest vertxRequest;

  private Cookie[] cookies;

  private ServletInputStream inputStream;

  public VertxServerRequestToHttpServletRequest(RoutingContext context, String path) {
    this(context);
    HttpServerRequestUtils.setPath(vertxRequest, path);
  }

  public VertxServerRequestToHttpServletRequest(RoutingContext context) {
    this.context = context;
    this.vertxRequest = context.request();
    super.setBodyBuffer(context.getBody());
  }

  @Override
  public void setBodyBuffer(Buffer bodyBuffer) {
    super.setBodyBuffer(bodyBuffer);
    context.setBody(bodyBuffer);
  }

  @Override
  public String getContentType() {
    return this.vertxRequest.getHeader(HttpHeaders.CONTENT_TYPE);
  }

  @Override
  public Cookie[] getCookies() {
    if (cookies == null) {
      Set<io.vertx.ext.web.Cookie> vertxCookies = context.cookies();
      Cookie tmpCookies[] = new Cookie[vertxCookies.size()];
      int idx = 0;
      for (io.vertx.ext.web.Cookie oneVertxCookie : vertxCookies) {
        Cookie cookie = new Cookie(oneVertxCookie.getName(), oneVertxCookie.getValue());
        tmpCookies[idx] = cookie;
        idx++;
      }
      cookies = tmpCookies;
    }
    return cookies;
  }

  @Override
  public String getParameter(String name) {
    return this.vertxRequest.getParam(name);
  }


  @Override
  public String[] getParameterValues(String name) {
    List<String> paramList = this.vertxRequest.params().getAll(name);
    return (String[]) paramList.toArray(new String[paramList.size()]);
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    Map<String, String[]> paramMap = new HashMap<>();
    MultiMap map = this.vertxRequest.params();
    for (String name : map.names()) {
      List<String> valueList = map.getAll(name);
      paramMap.put(name, (String[]) map.getAll(name).toArray(new String[valueList.size()]));
    }
    return paramMap;
  }

  @Override
  public String getScheme() {
    return this.vertxRequest.scheme();
  }

  @Override
  public String getRemoteAddr() {
    return this.vertxRequest.remoteAddress().host();
  }

  @Override
  public String getRemoteHost() {
    return this.vertxRequest.remoteAddress().host();
  }

  @Override
  public int getRemotePort() {
    return this.vertxRequest.remoteAddress().port();
  }

  @Override
  public String getLocalAddr() {
    return this.vertxRequest.localAddress().host();
  }

  @Override
  public int getLocalPort() {
    return this.vertxRequest.localAddress().port();
  }


  @Override
  public String getHeader(String name) {
    return this.vertxRequest.getHeader(name);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    return Collections.enumeration(this.vertxRequest.headers().getAll(name));
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return Collections.enumeration(vertxRequest.headers().names());
  }

  @Override
  public int getIntHeader(String name) {
    String header = this.vertxRequest.getHeader(name);
    if (header == null) {
      return -1;
    }

    return Integer.parseInt(header);
  }

  @Override
  public String getMethod() {
    return this.vertxRequest.method().name();
  }

  @Override
  public String getPathInfo() {
    return this.vertxRequest.path();
  }

  @Override
  public String getQueryString() {
    return this.vertxRequest.query();
  }

  @Override
  public String getRequestURI() {
    return this.vertxRequest.path();
  }


  @Override
  public String getServletPath() {
    return this.getPathInfo();
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    if (inputStream == null) {
      inputStream = new BufferInputStream(context.getBody().getByteBuf());
    }
    return inputStream;
  }

  @Override
  public AsyncContext getAsyncContext() {
    return EMPTY_ASYNC_CONTEXT;
  }
}
