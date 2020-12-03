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

import java.util.HashMap;

import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClientWithContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.WebSocketConnectOptions;

/**
 * Created by on 2017/4/28.
 */
public final class WebsocketUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketUtils.class);

  private WebsocketUtils() {
  }

  public static void open(IpPort ipPort, String url, Handler<Void> onOpen, Handler<Void> onClose,
      Handler<Buffer> onMessage, Handler<Throwable> onException,
      Handler<Throwable> onConnectFailed) {
    HttpClientWithContext vertxHttpClient = WebsocketClientPool.INSTANCE.getClient();
    vertxHttpClient.runOnContext(client -> {
      WebSocketConnectOptions options = new WebSocketConnectOptions();
      options.setHost(ipPort.getHostOrIp()).setPort(ipPort.getPort()).setURI(url)
          .setHeaders(RestUtils.getDefaultHeaders().addAll(RestUtils.getSignAuthHeaders(
              RestUtils.createSignRequest(HttpMethod.GET.name(), ipPort, new RequestParam(), url, new HashMap<>()))));
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
          asyncResult.result().frameHandler((frame) -> {
            if (frame.isBinary() || frame.isText()) {
              onMessage.handle(frame.binaryData());
            }
          });
        }
      });
    });
  }
}
