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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.Part;
import javax.ws.rs.core.Response.StatusType;

public abstract class AbstractHttpServletResponse extends BodyBufferSupportImpl implements HttpServletResponseEx {
  private Map<String, Object> attributes = new HashMap<>();

  @Override
  public String getCharacterEncoding() {
    throw new Error("not supported method");
  }

  @Override
  public String getContentType() {
    throw new Error("not supported method");
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    throw new Error("not supported method");
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    throw new Error("not supported method");
  }

  @Override
  public void setCharacterEncoding(String charset) {
    throw new Error("not supported method");
  }

  @Override
  public void setContentLength(int len) {
    throw new Error("not supported method");
  }

  @Override
  public void setContentLengthLong(long len) {
    throw new Error("not supported method");
  }

  @Override
  public void setContentType(String type) {
    throw new Error("not supported method");
  }

  @Override
  public void setBufferSize(int size) {
    throw new Error("not supported method");
  }

  @Override
  public int getBufferSize() {
    throw new Error("not supported method");
  }

  @Override
  public void flushBuffer() throws IOException {
    throw new Error("not supported method");
  }

  @Override
  public void resetBuffer() {
    throw new Error("not supported method");
  }

  @Override
  public boolean isCommitted() {
    throw new Error("not supported method");
  }

  @Override
  public void reset() {
    throw new Error("not supported method");
  }

  @Override
  public void setLocale(Locale loc) {
    throw new Error("not supported method");
  }

  @Override
  public Locale getLocale() {
    throw new Error("not supported method");
  }

  @Override
  public void addCookie(Cookie cookie) {
    throw new Error("not supported method");
  }

  @Override
  public boolean containsHeader(String name) {
    throw new Error("not supported method");
  }

  @Override
  public String encodeURL(String url) {
    throw new Error("not supported method");
  }

  @Override
  public String encodeRedirectURL(String url) {
    throw new Error("not supported method");
  }

  @Override
  @Deprecated
  public String encodeUrl(String url) {
    throw new Error("not supported method");
  }

  @Override
  @Deprecated
  public String encodeRedirectUrl(String url) {
    throw new Error("not supported method");
  }

  @Override
  public void sendError(int sc, String msg) throws IOException {
    throw new Error("not supported method");
  }

  @Override
  public void sendError(int sc) throws IOException {
    throw new Error("not supported method");
  }

  @Override
  public void sendRedirect(String location) throws IOException {
    throw new Error("not supported method");
  }

  @Override
  public void setDateHeader(String name, long date) {
    throw new Error("not supported method");
  }

  @Override
  public void addDateHeader(String name, long date) {
    throw new Error("not supported method");
  }

  @Override
  public void setHeader(String name, String value) {
    throw new Error("not supported method");
  }

  @Override
  public void addHeader(String name, String value) {
    throw new Error("not supported method");
  }

  @Override
  public void setIntHeader(String name, int value) {
    throw new Error("not supported method");
  }

  @Override
  public void addIntHeader(String name, int value) {
    throw new Error("not supported method");
  }

  @Override
  public void setStatus(int sc) {
    throw new Error("not supported method");
  }

  @Override
  @Deprecated
  public void setStatus(int sc, String sm) {
    throw new Error("not supported method");
  }

  @Override
  public int getStatus() {
    throw new Error("not supported method");
  }

  @Override
  public String getHeader(String name) {
    throw new Error("not supported method");
  }

  @Override
  public Collection<String> getHeaders(String name) {
    throw new Error("not supported method");
  }

  @Override
  public Collection<String> getHeaderNames() {
    throw new Error("not supported method");
  }

  @Override
  public StatusType getStatusType() {
    throw new Error("not supported method");
  }

  @Override
  public void setAttribute(String key, Object value) {
    this.attributes.put(key, value);
  }

  @Override
  public Object getAttribute(String key) {
    return this.attributes.get(key);
  }

  @Override
  public CompletableFuture<Void> sendPart(Part body) {
    throw new Error("not supported method");
  }
}
