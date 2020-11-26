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

package org.apache.servicecomb.serviceregistry.client.http;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.auth.SignRequest;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.WebSocketConnectOptions;

public final class WebsocketClientUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketClientUtil.class);

  private WebsocketClientPool websocketClientPool;

  private List<AuthHeaderProvider> authHeaderProviders;

  WebsocketClientUtil(ServiceRegistryConfig serviceRegistryConfig) {
    websocketClientPool = new WebsocketClientPool(serviceRegistryConfig.getWatchClientName());
    authHeaderProviders = serviceRegistryConfig.getAuthHeaderProviders();
  }

  public void open(IpPort ipPort, String url, Handler<Void> onOpen, Handler<Void> onClose,
      Handler<Buffer> onMessage, Handler<Throwable> onException,
      Handler<Throwable> onConnectFailed) {
    HttpClientWithContext vertxHttpClient = websocketClientPool.getClient();
    vertxHttpClient.runOnContext(client -> {
      WebSocketConnectOptions options = new WebSocketConnectOptions();
      options.setHost(ipPort.getHostOrIp()).setPort(ipPort.getPort()).setURI(url)
          .setHeaders(getDefaultHeaders().addAll(getSignAuthHeaders(
              createSignRequest(HttpMethod.GET.name(), ipPort, new RequestParam(), url, new HashMap<>()))));
      client.webSocket(options, asyncResult -> {
        if (asyncResult.failed()) {
          onConnectFailed.handle(asyncResult.cause());
        } else {
          onOpen.handle(null);
          asyncResult.result().exceptionHandler(v -> {
            onException.handle(v);
            try {
              asyncResult.result().close();
            } catch (Exception err) {
              LOGGER.error("ws close error.", err);
            }
          });
          asyncResult.result().closeHandler(v -> {
            onClose.handle(v);
          });
          asyncResult.result().pongHandler(pong -> {
            // ignore, just prevent NPE.
          });
          asyncResult.result().frameHandler((frame) ->
              {
                if (frame.isBinary() || frame.isText()) {
                  onMessage.handle(frame.binaryData());
                }
              }
          );
        }
      });
    });
  }

  public MultiMap getDefaultHeaders() {
    return MultiMap.caseInsensitiveMultiMap().addAll(defaultHeaders());
  }

  private Map<String, String> defaultHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put(RestClientUtil.HEADER_CONTENT_TYPE, "application/json");
    headers.put(RestClientUtil.HEADER_USER_AGENT, "cse-serviceregistry-client/1.0.0");
    headers.put(RestClientUtil.HEADER_TENANT_NAME, ServiceRegistryConfig.INSTANCE.getTenantName());

    return headers;
  }

  public Map<String, String> getSignAuthHeaders(SignRequest signReq) {
    Map<String, String> headers = new HashMap<>();
    authHeaderProviders.forEach(provider -> headers.putAll(provider.getSignAuthHeaders(signReq)));
    return headers;
  }

  public SignRequest createSignRequest(String method, IpPort ipPort, RequestParam requestParam, String url,
      Map<String, String> headers) {
    SignRequest signReq = new SignRequest();
    StringBuilder endpoint = new StringBuilder("https://" + ipPort.getHostOrIp());
    endpoint.append(":" + ipPort.getPort());
    endpoint.append(url);
    try {
      signReq.setEndpoint(new URI(endpoint.toString()));
    } catch (URISyntaxException e) {
      LOGGER.error("set uri failed, uri is {}, message: {}", endpoint.toString(), e.getMessage());
    }
    signReq.setContent((requestParam.getBody() != null && requestParam.getBody().length > 0)
        ? new ByteArrayInputStream(requestParam.getBody())
        : null);
    signReq.setHeaders(headers);
    signReq.setHttpMethod(method);
    signReq.setQueryParams(requestParam.getQueryParamsMap());
    return signReq;
  }
}
