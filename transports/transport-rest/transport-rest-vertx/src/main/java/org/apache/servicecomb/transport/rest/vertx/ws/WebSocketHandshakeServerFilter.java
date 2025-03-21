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
import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.foundation.vertx.http.VertxServerRequestToHttpServletRequest;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.arguments.consumer.ClientWebSocketArgumentMapper;
import org.apache.servicecomb.swagger.invocation.ws.ServerWebSocket;
import org.apache.servicecomb.swagger.invocation.ws.WebSocketPipe;

/**
 * Switch HTTP to WebSocket protocol and complete WebSocket handshake.
 */
public class WebSocketHandshakeServerFilter implements HttpServerFilter {
  public static final String WEBSOCKET_PIPE_CONTEXT_KEY = "x-scb-websocket-pipe";

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public boolean enabledForTransport(String transport) {
    return Const.WEBSOCKET.equals(transport);
  }

  @Override
  public Response afterReceiveRequest(Invocation invocation, HttpServletRequestEx requestEx) {
    if (invocation.isEdge()
        && (invocation.getLocalContext(WEBSOCKET_PIPE_CONTEXT_KEY) == null)) {
      // prepare pipe for edge forward websocket scene
      final WebSocketPipe webSocketPipe = new WebSocketPipe(invocation.getTraceId());
      invocation.addLocalContext(WEBSOCKET_PIPE_CONTEXT_KEY, webSocketPipe);
      invocation.addLocalContext(ClientWebSocketArgumentMapper.SCB_CLIENT_WEBSOCKET_LOCAL_CONTEXT_KEY,
          webSocketPipe.getClientWebSocket());
    }
    return null;
  }

  @Override
  public CompletableFuture<Void> beforeSendResponseAsync(Invocation invocation, HttpServletResponseEx responseEx) {
    if (invocation == null) { // error case
      return CompletableFuture.completedFuture(null);
    }
    if (Const.WEBSOCKET.equals(invocation.getProviderTransportName())
        && invocation.getRequestEx() instanceof VertxServerRequestToHttpServletRequest) {
      Response response = (Response) responseEx.getAttribute(RestConst.INVOCATION_HANDLER_RESPONSE);
      final Object result = response.getResult();
      if (result instanceof ServerWebSocket) {
        ((VertxServerRequestToHttpServletRequest) invocation.getRequestEx())
            .toWebSocket()
            .whenComplete((ws, t) ->
                new VertxWebSocketAdaptor(
                    invocation,
                    InvocationType.PRODUCER, // must set it manually, or it's not correct in edge situation
                    invocation.getOperationMeta().getExecutor(),
                    (ServerWebSocket) result,
                    ws));
      }
      // WebSocket operation may also return an HTTP response, for example, rejecting WebSocket handshake.
      // Therefore, we don't throw Exception here, just let it pass and act like REST transport mode.
    }

    return HttpServerFilter.super.beforeSendResponseAsync(invocation, responseEx);
  }
}
