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
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.foundation.vertx.stream.PumpFromPart;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.StatusType;

public class StandardHttpServletResponseEx extends HttpServletResponseWrapper implements HttpServletResponseEx {
  private final BodyBufferSupport bodyBuffer = new BodyBufferSupportImpl();

  private final Map<String, Object> attributes = new HashMap<>();

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

  @Override
  public void setStatus(int sc) {
    super.setStatus(sc);
    statusType = Status.fromStatusCode(sc);
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
  public CompletableFuture<Void> sendPart(Part part) {
    if (part == null) {
      return CompletableFuture.completedFuture(null);
    }

    DownloadUtils.prepareDownloadHeader(this, part);

    OutputStream outputStream;
    try {
      outputStream = getOutputStream();
    } catch (IOException e) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      future.completeExceptionally(e);
      return future;
    }

    // if context is null, then will switch to sync logic
    Context context = Vertx.currentContext();
    return new PumpFromPart(context, part).toOutputStream(outputStream, false);
  }
}
