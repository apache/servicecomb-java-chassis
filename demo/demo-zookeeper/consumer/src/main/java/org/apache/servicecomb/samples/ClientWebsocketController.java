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

package org.apache.servicecomb.samples;

import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.annotation.Transport;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocket;

@RestSchema(schemaId = "ClientWebsocketController")
@RequestMapping(path = "/ws")
public class ClientWebsocketController {
  interface ProviderService {
    WebSocket websocket();
  }

  @RpcReference(schemaId = "WebsocketController", microserviceName = "provider")
  private ProviderService providerService;

  @PostMapping("/websocket")
  @Transport(name = CoreConst.WEBSOCKET)
  public void websocket(ServerWebSocket serverWebsocket) {
    WebSocket providerWebSocket = providerService.websocket();
    providerWebSocket.closeHandler(v -> serverWebsocket.close());
    providerWebSocket.textMessageHandler(m -> {
      System.out.println("send message " + m);
      serverWebsocket.writeTextMessage(m);
    });
    serverWebsocket.textMessageHandler(m -> {
      System.out.println("receive message " + m);
      providerWebSocket.writeTextMessage(m);
    });
  }
}
