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

package org.apache.servicecomb.foundation.vertx.server;

import static org.mockito.ArgumentMatchers.any;

import java.net.InetSocketAddress;

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.vertx.AsyncResultCallback;
import org.apache.servicecomb.foundation.vertx.metrics.DefaultTcpServerMetrics;
import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultServerEndpointMetric;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.core.env.Environment;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.NetSocketImpl;

public class TestTcpServer {
  static class TcpServerForTest extends TcpServer {
    public TcpServerForTest(URIEndpointObject endpointObject) {
      super(endpointObject);
    }

    @Override
    protected TcpServerConnection createTcpServerConnection() {
      return new TcpServerConnection() {
        @Override
        public void init(NetSocket netSocket) {
          super.init(netSocket);
        }
      };
    }
  }

  protected Environment environment;

  @BeforeEach
  public void setup() {
    environment = Mockito.mock(Environment.class);
    LegacyPropertyFactory.setEnvironment(environment);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void testTcpServerNonSSL() {
    Vertx vertx = Mockito.mock(Vertx.class);
    AsyncResultCallback<InetSocketAddress> callback = Mockito.mock(AsyncResultCallback.class);
    NetServer netServer = Mockito.mock(NetServer.class);
    Mockito.when(vertx.createNetServer()).thenReturn(netServer);

    URIEndpointObject endpointObject = new URIEndpointObject("highway://127.0.0.1:6663");
    TcpServer server = new TcpServerForTest(endpointObject);
    // assert done in Expectations
    server.init(vertx, "", callback);
  }

  Handler<NetSocket> connectHandler;

  boolean netSocketClosed;

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void testConnectionLimit() {
    Vertx vertx = Mockito.mock(Vertx.class);
    AsyncResultCallback<InetSocketAddress> callback = Mockito.mock(AsyncResultCallback.class);
    NetServer netServer = Mockito.mock(NetServer.class);
    NetSocketImpl netSocket = Mockito.mock(NetSocketImpl.class);
    Mockito.when(vertx.createNetServer(any())).thenReturn(netServer);

    DefaultServerEndpointMetric endpointMetric = new DefaultServerEndpointMetric(null);
    DefaultTcpServerMetrics tcpServerMetrics = new DefaultTcpServerMetrics(endpointMetric);

    Mockito.doAnswer((Answer<NetServer>) invocationOnMock -> {
      connectHandler = invocationOnMock.getArgument(0);
      return netServer;
    }).when(netServer).connectHandler(any());

    Mockito.doAnswer((Answer<Void>) invocationOnMock -> {
      netSocketClosed = true;
      return null;
    }).when(netSocket).close();
    Mockito.when(netSocket.metrics()).thenReturn(tcpServerMetrics);
    SocketAddress socketAddress = Mockito.mock(SocketAddress.class);
    Mockito.when(netSocket.remoteAddress()).thenReturn(socketAddress);
    Mockito.when(socketAddress.toString()).thenReturn("127.0.0.1:6663");

    URIEndpointObject endpointObject = new URIEndpointObject("highway://127.0.0.1:6663?sslEnabled=true");
    TcpServer server = new TcpServerForTest(endpointObject) {
      @Override
      protected int getConnectionLimit() {
        return 2;
      }
    };
    // assert done in Expectations
    server.init(vertx, "", callback);

    // no problem
    endpointMetric.onConnect();
    endpointMetric.onConnect();
    connectHandler.handle(netSocket);

    // reject
    endpointMetric.onConnect();
    connectHandler.handle(netSocket);
    Assertions.assertTrue(netSocketClosed);
    Assertions.assertEquals(1, endpointMetric.getRejectByConnectionLimitCount());
  }
}
