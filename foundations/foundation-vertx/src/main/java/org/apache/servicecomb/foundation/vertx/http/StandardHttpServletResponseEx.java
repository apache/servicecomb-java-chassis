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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.Part;
import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.foundation.common.http.HttpStatus;

import io.vertx.core.buffer.Buffer;

public class StandardHttpServletResponseEx extends HttpServletResponseWrapper implements HttpServletResponseEx {
  private BodyBufferSupport bodyBuffer = new BodyBufferSupportImpl();

  private Map<String, Object> attributes = new HashMap<>();

  private StatusType statusType;

  public StandardHttpServletResponseEx(HttpServletResponse response) {
    super(response);
  }

  @Override
  public void setBodyBuffer(Buffer bodyBuffer) {
    this.bodyBuffer.setBodyBuffer(bodyBuffer);
  }

  @Override
  public Buffer getBodyBuffer() {
    return bodyBuffer.getBodyBuffer();
  }

  @Override
  public byte[] getBodyBytes() {
    return bodyBuffer.getBodyBytes();
  }

  @Override
  public int getBodyBytesLength() {
    return bodyBuffer.getBodyBytesLength();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void setStatus(int sc, String sm) {
    super.setStatus(sc, sm);
    statusType = new HttpStatus(sc, sm);
  }

  @Override
  public int getStatus() {
    return statusType.getStatusCode();
  }

  @Override
  public StatusType getStatusType() {
    return statusType;
  }

  @Override
  public void flushBuffer() throws IOException {
    byte[] bytes = getBodyBytes();
    if (bytes != null) {
      getOutputStream().write(bytes, 0, getBodyBytesLength());
    }
    super.flushBuffer();
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
