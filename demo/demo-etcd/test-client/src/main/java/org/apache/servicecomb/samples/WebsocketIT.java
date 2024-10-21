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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.samples.ThirdSvcConfiguration.WebsocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.vertx.core.http.WebSocket;

@Component
public class WebsocketIT implements CategorizedTestCase {
  @Autowired
  private WebsocketClient websocketClient;

  @Override
  public void testRestTransport() throws Exception {
    StringBuffer sb = new StringBuffer();
    AtomicBoolean closed = new AtomicBoolean(false);
    CountDownLatch latch = new CountDownLatch(1);

    WebSocket webSocket = websocketClient.websocket();
    webSocket.textMessageHandler(s -> {
      sb.append(s);
      sb.append(" ");
      webSocket.writeTextMessage(s);
    });
    webSocket.closeHandler(v -> {
      closed.set(true);
      latch.countDown();
    });
    latch.await(30, TimeUnit.SECONDS);
    TestMgr.check(sb.toString(), "hello hello 0 hello 1 hello 2 hello 3 hello 4 total 6 ");
    TestMgr.check(closed.get(), true);
  }
}
