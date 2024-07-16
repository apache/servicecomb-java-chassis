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

package org.apache.servicecomb.transport.rest.client;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.WebSocketTransportContext;
import org.apache.servicecomb.common.rest.definition.RestMetaUtils;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.AbstractFilter;
import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.core.filter.EdgeFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientOptionsSPI;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClients;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketClient;

public class WebSocketClientCodecFilter extends AbstractFilter implements ConsumerFilter, EdgeFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClientCodecFilter.class);

  public static final String NAME = "websocket-client";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean enabledForTransport(String transport) {
    return CoreConst.WEBSOCKET.equals(transport);
  }

  @Override
  public int getOrder() {
    return Filter.CONSUMER_LOAD_BALANCE_ORDER + 2000;
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    invocation.getInvocationStageTrace().startConsumerConnection();

    CompletableFuture<Response> createWebSocket = new CompletableFuture<>();
    URIEndpointObject endpoint = (URIEndpointObject) invocation.getEndpoint().getAddress();
    HttpClientOptionsSPI optionsSPI;
    if (endpoint.isHttp2Enabled()) {
      optionsSPI = SPIServiceUtils.getTargetService(HttpClientOptionsSPI.class,
          HttpTransportHttpClientOptionsSPI.class);
    } else {
      optionsSPI = SPIServiceUtils.getTargetService(HttpClientOptionsSPI.class,
          Http2TransportHttpClientOptionsSPI.class);
    }
    WebSocketClient webSocketClient = HttpClients.createWebSocketClient(optionsSPI, endpoint.isSslEnabled());

    try {
      webSocketClient.connect(endpoint.getPort(), endpoint.getHostOrIp(), createRequestPath(invocation,
              RestMetaUtils.getRestOperationMeta(invocation.getOperationMeta())))
          .onComplete(asyncResult -> {
            invocation.getInvocationStageTrace().finishConsumerConnection();
            if (asyncResult.failed()) {
              createWebSocket.completeExceptionally(asyncResult.cause());
              return;
            }
            if (invocation.isEdge()) {
              WebSocketTransportContext parentContext = invocation.getTransportContext();
              ServerWebSocket serverWebSocket = parentContext.getServerWebSocket();
              WebSocket clientWebSocket = asyncResult.result();
              serverWebSocket.closeHandler(v -> {
                if (!clientWebSocket.isClosed()) {
                  clientWebSocket.close();
                }
              });
              serverWebSocket.textMessageHandler(clientWebSocket::writeTextMessage);
              serverWebSocket.binaryMessageHandler(clientWebSocket::writeBinaryMessage);
              serverWebSocket.exceptionHandler(e -> {
                LOGGER.warn("consumer exception.", e);
                if (!serverWebSocket.isClosed()) {
                  serverWebSocket.close();
                }
              });
              clientWebSocket.closeHandler(v -> {
                if (!serverWebSocket.isClosed()) {
                  serverWebSocket.close();
                }
              });
              clientWebSocket.textMessageHandler(serverWebSocket::writeTextMessage);
              clientWebSocket.binaryMessageHandler(serverWebSocket::writeBinaryMessage);
              clientWebSocket.exceptionHandler(e -> {
                LOGGER.warn("producer exception.", e);
                if (!clientWebSocket.isClosed()) {
                  clientWebSocket.close();
                }
              });
            }
            invocation.setTransportContext(new WebSocketClientTransportContext(
                asyncResult.result()));
            createWebSocket.complete(Response.createSuccess(asyncResult.result()));
          });
    } catch (Exception e) {
      createWebSocket.completeExceptionally(e);
    }

    return createWebSocket;
  }

  protected String createRequestPath(Invocation invocation, RestOperationMeta restOperationMeta) throws Exception {
    String path = invocation.getLocalContext(RestConst.REST_CLIENT_REQUEST_PATH);
    if (path == null) {
      path = restOperationMeta.getPathBuilder().createRequestPath(invocation.getSwaggerArguments());
    }

    URIEndpointObject endpoint = (URIEndpointObject) invocation.getEndpoint().getAddress();
    String urlPrefix = endpoint.getFirst(DefinitionConst.URL_PREFIX);
    if (StringUtils.isEmpty(urlPrefix) || path.startsWith(urlPrefix)) {
      return path;
    }

    return urlPrefix + path;
  }
}
