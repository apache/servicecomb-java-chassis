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

package org.apache.servicecomb.transport.rest.client.ws;

import org.apache.servicecomb.common.rest.codec.RestClientRequest;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocketConnectOptions;

public class RestClientRequestWebSocketWrapper implements RestClientRequest {
  private final WebSocketConnectOptions webSocketClientRequest;

  public RestClientRequestWebSocketWrapper(WebSocketConnectOptions webSocketClientRequest) {
    this.webSocketClientRequest = webSocketClientRequest;
  }

  @Override
  public void write(Buffer bodyBuffer) {
    throw new UnsupportedOperationException("websocket not support http body");
  }

  @Override
  public Future<Void> end() {
    return Future.succeededFuture();
  }

  @Override
  public void addCookie(String name, String value) {
    throw new UnsupportedOperationException("websocket not support cookie");
  }

  @Override
  public void putHeader(String name, String value) {
    webSocketClientRequest.putHeader(name, value);
  }

  @Override
  public MultiMap getHeaders() {
    return webSocketClientRequest.getHeaders();
  }

  @Override
  public void addForm(String name, Object value) {
    throw new UnsupportedOperationException("websocket not support http form");
  }

  @Override
  public Buffer getBodyBuffer() {
    return Buffer.buffer();
  }

  @Override
  public void attach(String name, Object partOrList) {
    throw new UnsupportedOperationException("websocket not support http file upload");
  }
}
