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

import java.util.Collection;
import java.util.Collections;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.foundation.common.http.HttpStatus;
import org.apache.servicecomb.foundation.vertx.http.AbstractHttpServletResponse;

import io.vertx.core.MultiMap;
import io.vertx.core.http.WebSocket;

public class VertxClientWebSocketResponseToHttpServletResponse extends AbstractHttpServletResponse {
  private final WebSocket vertxClientWebSocket;

  private final HttpStatus httpStatus;

  public VertxClientWebSocketResponseToHttpServletResponse(WebSocket vertxClientWebSocket) {
    this.vertxClientWebSocket = vertxClientWebSocket;
    // if run this function, the underlying WebSocket handshake must be completed.
    httpStatus = new HttpStatus(101, "Switching Protocols");
  }

  @Override
  public int getStatus() {
    return httpStatus.getStatusCode();
  }

  @Override
  public StatusType getStatusType() {
    return httpStatus;
  }

  @Override
  public String getContentType() {
    return getHeader(HttpHeaders.CONTENT_TYPE);
  }

  @Override
  public String getHeader(String name) {
    final MultiMap headers = vertxClientWebSocket.headers();
    if (headers == null) {
      return null;
    }
    return headers.get(name);
  }

  @Override
  public Collection<String> getHeaders(String name) {
    final MultiMap headers = vertxClientWebSocket.headers();
    if (headers == null) {
      return Collections.emptyList();
    }
    return headers.getAll(name);
  }

  @Override
  public Collection<String> getHeaderNames() {
    final MultiMap headers = vertxClientWebSocket.headers();
    if (headers == null) {
      return Collections.emptyList();
    }
    return headers.names();
  }

  public WebSocket getVertxClientWebSocket() {
    return vertxClientWebSocket;
  }
}
