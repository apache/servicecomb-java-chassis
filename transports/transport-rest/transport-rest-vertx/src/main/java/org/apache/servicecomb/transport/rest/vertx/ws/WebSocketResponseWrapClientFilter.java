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

import java.util.concurrent.Executor;

import org.apache.servicecomb.common.rest.filter.HttpClientFilter;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.ExceptionCodes;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.foundation.vertx.ws.VertxClientWebSocketResponseToHttpServletResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.arguments.consumer.ClientWebSocketArgumentMapper;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.apache.servicecomb.swagger.invocation.ws.ClientWebSocket;
import org.apache.servicecomb.swagger.invocation.ws.WebSocketPipe;

/**
 * WebSocketResponseWrapClientFilter
 */
public class WebSocketResponseWrapClientFilter implements HttpClientFilter {
  @Override
  public boolean enabled() {
    return HttpClientFilter.super.enabled();
  }

  @Override
  public boolean enabledForTransport(String transport) {
    return Const.WEBSOCKET.equals(transport);
  }

  @Override
  public int getOrder() {
    return 10010;
  }

  @Override
  public Response afterReceiveResponse(Invocation invocation, HttpServletResponseEx responseEx) {
    if (!(responseEx instanceof VertxClientWebSocketResponseToHttpServletResponse)) {
      throw new InvocationException(ExceptionFactory.CONSUMER_INNER_STATUS, "unexpected responseEx type["
          + responseEx.getClass()
          + "], this should be a websocket scene");
    }
    // only handshake success scenes run into this method. No need to consider handshake failure case.
    final VertxClientWebSocketResponseToHttpServletResponse webSocketResponseEx = (VertxClientWebSocketResponseToHttpServletResponse) responseEx;
    final ClientWebSocket clientWebSocket = invocation.getLocalContext(
        ClientWebSocketArgumentMapper.SCB_CLIENT_WEBSOCKET_LOCAL_CONTEXT_KEY);

    final Executor executor = invocation
        .getOperationMeta()
        .getExecutor();
    final VertxWebSocketAdaptor webSocketAdaptor = new VertxWebSocketAdaptor(
        executor,
        clientWebSocket,
        webSocketResponseEx.getVertxClientWebSocket());
    invocation.addLocalContext("webSocketAdaptor", webSocketAdaptor);

    if (!invocation.isEdge()) {
      return Response.success(null,
          responseEx.getStatusType());
    } else {
      final WebSocketPipe webSocketPipe =
          invocation.getLocalContext(WebSocketHandshakeServerFilter.WEBSOCKET_PIPE_CONTEXT_KEY);
      if (webSocketPipe == null) {
        throw new InvocationException(ExceptionFactory.CONSUMER_INNER_STATUS_CODE,
            ExceptionFactory.CONSUMER_INNER_REASON_PHRASE,
            new CommonExceptionData(ExceptionCodes.GENERIC_CLIENT, "edge websocket lost pipe"));
      }
      return Response.success(webSocketPipe.getServerWebSocket(), responseEx.getStatusType());
    }
  }
}
