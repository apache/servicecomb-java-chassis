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

package org.apache.servicecomb.transport.rest.vertx.ws;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.filter.inner.ServerRestArgsFilter;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.ws.ServerWebSocket;

import com.netflix.config.DynamicPropertyFactory;

public class ServerWebSocketArgsFilter extends ServerRestArgsFilter {
  private static final boolean enabled = DynamicPropertyFactory.getInstance().getBooleanProperty
      ("servicecomb.http.filter.server.serverWebSocketArgs.enabled", true).get();

  @Override
  public boolean enabled() {
    return enabled;
  }

  @Override
  public boolean enabledForTransport(String transport) {
    return Const.WEBSOCKET.equals(transport);
  }

  @Override
  public CompletableFuture<Void> beforeSendResponseAsync(Invocation invocation, HttpServletResponseEx responseEx) {
    Response response = (Response) responseEx.getAttribute(RestConst.INVOCATION_HANDLER_RESPONSE);
    if (response.getResult() instanceof ServerWebSocket) {
      // don't do anything on WebSocket response. Let the WebSocketHandshakeServerFilter to handle WS handshaking.
      return CompletableFuture.completedFuture(null);
    }
    // WS handshaking may fail and return an HTTP response, like 401
    return super.beforeSendResponseAsync(invocation, responseEx);
  }
}
