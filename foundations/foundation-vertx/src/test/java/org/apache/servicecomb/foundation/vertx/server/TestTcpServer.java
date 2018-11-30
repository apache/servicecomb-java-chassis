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

import java.net.InetSocketAddress;

import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.vertx.AsyncResultCallback;
import org.apache.servicecomb.foundation.vertx.metrics.DefaultTcpServerMetrics;
import org.apache.servicecomb.foundation.vertx.metrics.metric.DefaultServerEndpointMetric;
import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.impl.NetSocketImpl;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

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

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void testTcpServerNonSSL(@Mocked Vertx vertx, @Mocked AsyncResultCallback<InetSocketAddress> callback,
      @Mocked NetServer netServer) {
    new Expectations() {
      {
        vertx.createNetServer();
        result = netServer;
        netServer.connectHandler((Handler) any);
        netServer.listen(anyInt, anyString, (Handler) any);
      }
    };
    URIEndpointObject endpointObject = new URIEndpointObject("highway://127.0.0.1:6663");
    TcpServer server = new TcpServerForTest(endpointObject);
    // assert done in Expectations
    server.init(vertx, "", callback);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void testTcpServerSSL(@Mocked Vertx vertx, @Mocked AsyncResultCallback<InetSocketAddress> callback,
      @Mocked NetServer netServer) {
    new Expectations() {
      {
        vertx.createNetServer((NetServerOptions) any);
        result = netServer;
        netServer.connectHandler((Handler) any);
        netServer.listen(anyInt, anyString, (Handler) any);
      }
    };
    URIEndpointObject endpointObject = new URIEndpointObject("highway://127.0.0.1:6663?sslEnabled=true");
    TcpServer server = new TcpServerForTest(endpointObject);
    // assert done in Expectations
    server.init(vertx, "", callback);
  }

  Handler<NetSocket> connectHandler;

  boolean netSocketClosed;

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void testConnectionLimit(@Mocked Vertx vertx, @Mocked AsyncResultCallback<InetSocketAddress> callback,
      @Mocked NetServer netServer, @Mocked NetSocketImpl netSocket) {
    DefaultServerEndpointMetric endpointMetric = new DefaultServerEndpointMetric(null);
    DefaultTcpServerMetrics tcpServerMetrics = new DefaultTcpServerMetrics(endpointMetric);

    new MockUp<NetServer>(netServer) {
      @Mock
      NetServer connectHandler(Handler<NetSocket> handler) {
        connectHandler = handler;
        return netServer;
      }
    };
    new MockUp<NetSocketImpl>(netSocket) {
      @Mock
      void close() {
        netSocketClosed = true;
      }
    };
    new Expectations() {
      {
        vertx.createNetServer((NetServerOptions) any);
        result = netServer;
        netServer.listen(anyInt, anyString, (Handler) any);
        netSocket.metrics();
        result = tcpServerMetrics;
      }
    };
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
    Assert.assertTrue(netSocketClosed);
    Assert.assertEquals(1, endpointMetric.getRejectByConnectionLimitCount());
  }
}
