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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.annotation.Transport;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.vertx.core.http.ServerWebSocket;

@RestSchema(schemaId = "WebsocketController")
@RequestMapping(path = "/ws")
public class WebsocketController {
  @PostMapping("/websocket")
  @Transport(name = CoreConst.WEBSOCKET)
  public void websocket(ServerWebSocket serverWebsocket) {
    AtomicInteger receiveCount = new AtomicInteger(0);
    serverWebsocket.writeTextMessage("hello", r -> {
    });
    serverWebsocket.textMessageHandler(s -> {
      receiveCount.getAndIncrement();
    });
    serverWebsocket.closeHandler((v) -> System.out.println("closed"));
    new Thread(() -> {
      for (int i = 0; i < 5; i++) {
        serverWebsocket.writeTextMessage("hello " + i, r -> {
        });
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      serverWebsocket.writeTextMessage("total " + receiveCount.get());
      serverWebsocket.close();
    }).start();
  }
}
