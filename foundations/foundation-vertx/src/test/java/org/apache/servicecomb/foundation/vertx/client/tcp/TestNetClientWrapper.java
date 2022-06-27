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
package org.apache.servicecomb.foundation.vertx.client.tcp;


import java.util.ArrayList;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;


import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;

public class TestNetClientWrapper {

  Vertx vertx;

  TcpClientConfig normalClientConfig;

  NetClient normalNetClient;

  TcpClientConfig sslClientConfig;

  NetClient sslNetClient;

  NetClientWrapper netClientWrapper;

  @Mock
  NetSocket normalSocket;

  @Mock
  NetSocket sslSocket;

  @BeforeEach
  public void setup() {
    vertx = Mockito.mock(Vertx.class);
    normalClientConfig = Mockito.mock(TcpClientConfig.class);
    normalNetClient = Mockito.mock(NetClient.class);
    sslClientConfig =Mockito.mock(TcpClientConfig.class);
    sslNetClient = Mockito.mock(NetClient.class);
    Mockito.when(vertx.createNetClient(normalClientConfig)).thenReturn(normalNetClient);
    Mockito.when(vertx.createNetClient(sslClientConfig)).thenReturn(sslNetClient);
    netClientWrapper = new NetClientWrapper(vertx, normalClientConfig, sslClientConfig);
  }

  @org.junit.jupiter.api.Test
  public void getClientConfig() {
    Assertions.assertSame(normalClientConfig, netClientWrapper.getClientConfig(false));
    Assertions.assertSame(sslClientConfig, netClientWrapper.getClientConfig(true));
  }

  @org.junit.jupiter.api.Test
  public void connect() {
    int port = 8000;
    String host = "localhost";

    List<NetSocket> socks = new ArrayList<>();
    Promise<NetSocket> promiseConnect = Promise.promise();
    Promise<NetSocket> sslPromiseConnect = Promise.promise();
    Handler<AsyncResult<NetSocket>> connectHandler = asyncSocket -> socks.add(asyncSocket.result());
    Mockito.when(normalNetClient.connect(port, host, connectHandler)).thenAnswer(invocation -> {
      promiseConnect.complete(normalSocket);
      connectHandler.handle(promiseConnect.future());
      return null;
    });
    Mockito.when(sslNetClient.connect(port, host, connectHandler)).thenAnswer(invocationOnMock -> {
      sslPromiseConnect.complete(sslSocket);
      connectHandler.handle(sslPromiseConnect.future());
      return null;
    });

    netClientWrapper.connect(false, port, host, connectHandler);
    netClientWrapper.connect(true, port, host, connectHandler);

    MatcherAssert.assertThat(socks, Matchers.contains(normalSocket, sslSocket));
  }
}
