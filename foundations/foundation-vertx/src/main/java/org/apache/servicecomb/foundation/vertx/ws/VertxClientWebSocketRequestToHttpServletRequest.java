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

package org.apache.servicecomb.foundation.vertx.ws;

import java.util.Collections;
import java.util.Enumeration;

import org.apache.http.HttpHeaders;
import org.apache.servicecomb.foundation.common.http.HttpUtils;
import org.apache.servicecomb.foundation.vertx.http.AbstractHttpServletRequest;

import io.vertx.core.http.WebSocketConnectOptions;

public class VertxClientWebSocketRequestToHttpServletRequest extends AbstractHttpServletRequest {
  private final WebSocketConnectOptions clientRequest;

  private String characterEncoding;

  public VertxClientWebSocketRequestToHttpServletRequest(WebSocketConnectOptions clientRequest) {
    this.clientRequest = clientRequest;
  }

  @Override
  public String getRequestURI() {
    return HttpUtils.splitPathFromUri(clientRequest.getURI());
  }

  @Override
  public String getQueryString() {
    return HttpUtils.splitQueryFromUri(clientRequest.getURI());
  }

  @Override
  public String getHeader(String name) {
    return clientRequest.getHeaders().get(name);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    return Collections.enumeration(clientRequest.getHeaders().getAll(name));
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return Collections.enumeration(clientRequest.getHeaders().names());
  }

  @Override
  public void setHeader(String name, String value) {
    clientRequest.getHeaders().set(name, value);
  }

  @Override
  public void addHeader(String name, String value) {
    clientRequest.getHeaders().add(name, value);
  }

  @Override
  public String getContextPath() {
    return "";
  }

  @Override
  public String getMethod() {
    return clientRequest.getMethod().name();
  }

  @Override
  public String getContentType() {
    return clientRequest.getHeaders().get(HttpHeaders.CONTENT_TYPE);
  }

  @Override
  public String getCharacterEncoding() {
    if (characterEncoding == null) {
      characterEncoding = HttpUtils.getCharsetFromContentType(getContentType());
    }

    return characterEncoding;
  }
}
