/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.vertx;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.foundation.common.net.URIEndpointObject;
import io.servicecomb.foundation.vertx.client.tcp.TcpClientConfig;
import io.servicecomb.foundation.vertx.client.tcp.TcpClientConnection;
import io.servicecomb.foundation.vertx.client.tcp.TcpClientConnectionPool;
import io.servicecomb.foundation.vertx.client.tcp.TcpClientPackage;
import io.servicecomb.foundation.vertx.client.tcp.TcpClientVerticle;
import io.servicecomb.foundation.vertx.client.tcp.TcpData;
import io.servicecomb.foundation.vertx.client.tcp.TcpRequest;
import io.servicecomb.foundation.vertx.client.tcp.TcpResonseCallback;
import io.servicecomb.foundation.vertx.server.TcpParser;
import io.servicecomb.foundation.vertx.server.TcpServer;
import io.servicecomb.foundation.vertx.tcp.TcpOutputStream;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.impl.NetSocketImpl;
import io.vertx.core.net.impl.SocketAddressImpl;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestTcp {

  enum ParseStatus {
    MSG_ID_AND_LEN,
    // len: total len/header len
    HEADER,
    BODY
  }

  @Test
  public void testTcpClient() throws Exception {
    NetClient oNetClient = new NetClient() {

      @Override
      public boolean isMetricsEnabled() {
        return true;
      }

      @Override
      public NetClient connect(int port, String host, Handler<AsyncResult<NetSocket>> connectHandler) {
        return Mockito.mock(NetClient.class);
      }

      @Override
      public void close() {
      }
    };
    TcpClientConnection oTcpClient =
        new TcpClientConnection(Mockito.mock(Context.class), oNetClient, "highway://127.2.0.1:8080",
            new TcpClientConfig());
    oTcpClient.checkTimeout();
    oTcpClient.send(new TcpClientPackage(null), 123, Mockito.mock(TcpResonseCallback.class));
    oTcpClient.send(new TcpClientPackage(null), 123, Mockito.mock(TcpResonseCallback.class));

    new MockUp<TcpClientConnectionPool>() {
      @Mock
      protected void startCheckTimeout(TcpClientConfig clientConfig, Context context) {
      }
    };
    Vertx vertx = VertxUtils.init(null);
    TcpClientConfig config = new TcpClientConfig();
    TcpClientConnectionPool oClientPool =
        new TcpClientConnectionPool(config, vertx.getOrCreateContext(), oNetClient);
    oClientPool.send(oTcpClient, new TcpClientPackage(null), Mockito.mock(TcpResonseCallback.class));
    oClientPool.send(oTcpClient, new TcpClientPackage(null), Mockito.mock(TcpResonseCallback.class));
    Assert.assertNotNull(oClientPool);

    TcpRequest oTcpRequest = new TcpRequest(1234, Mockito.mock(TcpResonseCallback.class));
    oTcpRequest.isTimeout();
    oTcpRequest.onReply(Buffer.buffer(), Buffer.buffer(("test").getBytes()));
    oTcpRequest.onSendError(new Throwable("test Errorsss"));
    Assert.assertNotNull(oTcpRequest);

    TcpClientVerticle oTcpClientVerticle = new TcpClientVerticle();
    oTcpClientVerticle.init(vertx, vertx.getOrCreateContext());
    oTcpClientVerticle.createClientPool();
    oTcpClientVerticle.createClientPool();
    Assert.assertNotNull(oTcpClientVerticle.getVertx());

    NetSocket socket = Mockito.mock(NetSocketImpl.class);
    Throwable e = Mockito.mock(Throwable.class);
    Buffer hBuffer = Mockito.mock(Buffer.class);
    Buffer bBuffer = Mockito.mock(Buffer.class);

    Deencapsulation.invoke(oTcpClient, "connect");
    Deencapsulation.invoke(oTcpClient, "onConnectSuccess", socket);
    Mockito.when(socket.localAddress()).thenReturn(new SocketAddressImpl(0, "127.0.0.1"));
    Deencapsulation.setField(oTcpClient, "netSocket", socket);
    Deencapsulation.invoke(oTcpClient, "onDisconnected", e);
    Deencapsulation.invoke(oTcpClient, "tryLogin");
    Deencapsulation.invoke(oTcpClient, "onLoginSuccess");
    Deencapsulation.invoke(oTcpClient, "onConnectFailed", e);
    long l = 10;
    Deencapsulation.invoke(oTcpClient, "onReply", l, hBuffer, bBuffer);
    oTcpClient.checkTimeout();
    Assert.assertNotNull(oTcpClient);

    vertx.close();
  }

  @Test
  public void testTcpOutputStream() {
    TcpOutputStream oStream = new TcpOutputStream(0);
    oStream.close();
    Buffer buffer = oStream.getBuffer();
    Assert.assertArrayEquals(TcpParser.TCP_MAGIC, buffer.getBytes(0, TcpParser.TCP_MAGIC.length));
    Assert.assertEquals(oStream.getMsgId(), buffer.getLong(TcpParser.TCP_MAGIC.length));
  }

  @Test
  public void testTcpServerStarter() {
    Vertx vertx = VertxUtils.init(null);

    URIEndpointObject endpiont = new URIEndpointObject("highway://127.0.0.1:9900");
    TcpServer oStarter = new TcpServer(endpiont);
    oStarter.init(vertx, "", null);
    Assert.assertNotNull(oStarter);
    //TODO Need to find a way to Assert TcpServerStarter as this obbject does not return any values.

    vertx.close();
  }

  @Test
  public void testTcpClientConfig() {
    TcpClientConfig tcpClientConfig = new TcpClientConfig();
    tcpClientConfig.getRequestTimeoutMillis();
    tcpClientConfig.setRequestTimeoutMillis(1);
    Assert.assertNotNull(tcpClientConfig);
  }

  @Test
  public void testTcpData() {
    Buffer hBuffer = Mockito.mock(Buffer.class);
    Buffer bBuffer = Mockito.mock(Buffer.class);
    TcpData tcpData = new TcpData(hBuffer, bBuffer);
    tcpData.getBodyBuffer();
    tcpData.setBodyBuffer(bBuffer);
    tcpData.getHeaderBuffer();
    tcpData.setBodyBuffer(hBuffer);
    Assert.assertNotNull(tcpData.getBodyBuffer());
    Assert.assertNotNull(tcpData.getHeaderBuffer());
  }
}
