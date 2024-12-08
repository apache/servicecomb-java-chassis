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

import java.util.List;

import org.apache.servicecomb.common.rest.filter.HttpClientFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClients;
import org.apache.servicecomb.foundation.vertx.client.ws.WebSocketClientWithContext;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketTransportClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketTransportClient.class);

  private final List<HttpClientFilter> httpClientFilters;

  WebSocketTransportClient() {
    httpClientFilters = SPIServiceUtils.getSortedService(HttpClientFilter.class);
  }

  public void send(Invocation invocation, AsyncResponse asyncResp) {
    final WebSocketClientWithContext webSocketClientWithContext = findHttpClientPool(invocation);
    final WebSocketClientInvocation webSocketClientInvocation = new WebSocketClientInvocation(
        webSocketClientWithContext,
        httpClientFilters);
    try {
      webSocketClientInvocation.invoke(invocation, asyncResp);
    } catch (Throwable e) {
      asyncResp.fail(invocation.getInvocationType(), e);
      LOGGER.error("vertx websocket transport send error.", e);
    }
  }

  protected WebSocketClientWithContext findHttpClientPool(Invocation invocation) {
    String clientName = WebSocketTransportClientOptionsSPI.CLIENT_NAME;
    return HttpClients.getWebSocketClient(clientName, invocation.isSync(), null);
  }
}
