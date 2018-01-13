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

package org.apache.servicecomb.transport.highway;

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.executor.ReactiveExecutor;
import org.apache.servicecomb.core.unittest.UnitTestMeta;
import org.apache.servicecomb.foundation.vertx.tcp.TcpConnection;
import org.apache.servicecomb.transport.common.MockUtil;
import org.apache.servicecomb.transport.highway.message.RequestHeader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import mockit.Mock;
import mockit.MockUp;

public class TestHighwayServerInvoke {
  class Impl {
    public int add(int x, int y) {
      return x + y;
    }
  }

  private UnitTestMeta unitTestMeta;

  private ByteBuf netSocketBuffer;

  private TcpConnection connection;

  private NetSocket netSocket;

  private SocketAddress socketAddress;

  @Before
  public void setup() {
    unitTestMeta = new UnitTestMeta();
    socketAddress = new MockUp<SocketAddress>() {
      @Mock
      public String host() {
        return "127.0.0.1";
      }

      @Mock
      public int port() {
        return 8080;
      }
    }.getMockInstance();
    netSocket = new MockUp<NetSocket>() {
      @Mock
      public SocketAddress remoteAddress() {
        return socketAddress;
      }
    }.getMockInstance();
    connection = new MockUp<TcpConnection>() {
      @Mock
      public void write(ByteBuf data) {
        netSocketBuffer = data;
      }

      @Mock
      public NetSocket getNetSocket() {
        return netSocket;
      }
    }.getMockInstance();
  }

  @Test
  public void test() {
    MockUtil.getInstance().mockHighwayCodec();

    SchemaMeta schemaMeta = unitTestMeta.getOrCreateSchemaMeta(Impl.class);
    OperationMeta operationMeta = schemaMeta.ensureFindOperation("add");
    operationMeta.setExecutor(new ReactiveExecutor());

    HighwayServerInvoke highwayServerInvoke = new HighwayServerInvoke();
    highwayServerInvoke.setMicroserviceMetaManager(unitTestMeta.getMicroserviceMetaManager());

    RequestHeader requestHeader = MockUtil.getInstance().requestHeader;

    // 初始化失败
    requestHeader.setDestMicroservice(null);
    Assert.assertFalse(highwayServerInvoke.init(connection, 0, null, null));

    // 初始化成功
    requestHeader.setDestMicroservice(schemaMeta.getMicroserviceName());
    requestHeader.setSchemaId(schemaMeta.getSchemaId());
    requestHeader.setOperationName(operationMeta.getOperationId());
    Assert.assertTrue(highwayServerInvoke.init(connection, 0, requestHeader, null));

    // exe失败
    MockUtil.getInstance().decodeRequestSucc = false;
    highwayServerInvoke.execute();
    Assert.assertEquals(true, Buffer.buffer(netSocketBuffer).toString().startsWith("CSE.TCP"));
  }
}
