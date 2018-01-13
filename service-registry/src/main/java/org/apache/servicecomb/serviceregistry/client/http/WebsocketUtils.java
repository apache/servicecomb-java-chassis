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
      client.websocket(ipPort.getPort(),
          ipPort.getHostOrIp(),
          url,
          RestUtils.getDefaultHeaders().addAll(RestUtils
              .getSignAuthHeaders(RestUtils.createSignRequest(null, ipPort, new RequestParam(), url, new HashMap<>()))),
          ws -> {
            onOpen.handle(null);

            ws.exceptionHandler(v -> {
              onException.handle(v);
              try {
                ws.close();
              } catch (Exception err) {
                LOGGER.error("ws close error.", err);
              }
            });
            ws.closeHandler(v -> {
              onClose.handle(v);
              try {
                ws.close();
              } catch (Exception err) {
                LOGGER.error("ws close error.", err);
              }
            });
            ws.handler(onMessage);
          },
          onConnectFailed);
    });
  }
}
