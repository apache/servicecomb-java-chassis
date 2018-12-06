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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import javax.xml.ws.Holder;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.SCBStatus;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.core.executor.ReactiveExecutor;
import org.apache.servicecomb.core.unittest.UnitTestMeta;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.vertx.tcp.TcpConnection;
import org.apache.servicecomb.swagger.invocation.context.HttpStatus;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.apache.servicecomb.transport.common.MockUtil;
import org.apache.servicecomb.transport.highway.message.RequestHeader;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestHighwayServerInvoke {
  class Impl {
    public int add(int x, int y) {
      return x + y;
    }
  }

  private static UnitTestMeta unitTestMeta;

  private ByteBuf netSocketBuffer;

  private TcpConnection connection;

  private NetSocket netSocket;

  private SocketAddress socketAddress;

  static long nanoTime = 1;

  @BeforeClass
  public static void classSetup() {
    EventManager.eventBus = new EventBus();
    SCBEngine.getInstance().setStatus(SCBStatus.UP);
    unitTestMeta = new UnitTestMeta();

    new MockUp<System>() {
      @Mock
      long nanoTime() {
        return nanoTime;
      }
    };
  }

  @AfterClass
  public static void classTeardown() {
    EventManager.eventBus = new EventBus();
    SCBEngine.getInstance().setStatus(SCBStatus.DOWN);
  }

  @Before
  public void setup() {
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
    Holder<InvocationStartEvent> startHolder = new Holder<>();
    Holder<InvocationFinishEvent> finishHolder = new Holder<>();
    Object subscriber = new Object() {
      @Subscribe
      public void onStart(InvocationStartEvent event) {
        startHolder.value = event;
      }

      @Subscribe
      public void onFinish(InvocationFinishEvent event) {
        finishHolder.value = event;
      }
    };
    EventManager.register(subscriber);

    MockUtil.getInstance().mockHighwayCodec();

    SchemaMeta schemaMeta = unitTestMeta.getOrCreateSchemaMeta(Impl.class);
    OperationMeta operationMeta = schemaMeta.ensureFindOperation("add");
    operationMeta.setExecutor(new ReactiveExecutor());

    HighwayServerInvoke highwayServerInvoke = new HighwayServerInvoke();

    RequestHeader requestHeader = MockUtil.getInstance().requestHeader;

    // 初始化失败
    requestHeader.setDestMicroservice(null);
    Assert.assertFalse(highwayServerInvoke.init(connection, 0, null, null));

    // 初始化成功
    requestHeader.setDestMicroservice(schemaMeta.getMicroserviceName());
    requestHeader.setSchemaId(schemaMeta.getSchemaId());
    requestHeader.setOperationName(operationMeta.getOperationId());
    assertTrue(highwayServerInvoke.init(connection, 0, requestHeader, null));

    // exe失败
    MockUtil.getInstance().decodeRequestSucc = false;
    highwayServerInvoke.execute();
    EventManager.unregister(subscriber);

    assertTrue(Buffer.buffer(netSocketBuffer).toString().startsWith("CSE.TCP"));
    assertSame(highwayServerInvoke.invocation, startHolder.value.getInvocation());
    assertSame(highwayServerInvoke.invocation, finishHolder.value.getInvocation());
    assertTrue(highwayServerInvoke.invocation.getInvocationStageTrace().getStartExecution() != 0);
    assertEquals(1, highwayServerInvoke.invocation.getInvocationStageTrace().getStart());
    assertEquals(1, highwayServerInvoke.invocation.getInvocationStageTrace().getStartHandlersRequest());
    assertEquals(1, highwayServerInvoke.invocation.getInvocationStageTrace().getFinishHandlersResponse());
    assertEquals(1, highwayServerInvoke.invocation.getInvocationStageTrace().getStartSchedule());
    assertEquals(1, highwayServerInvoke.invocation.getInvocationStageTrace().getStartHandlersRequest());
    assertEquals(1, highwayServerInvoke.invocation.getInvocationStageTrace().getFinishHandlersResponse());
  }

  @Test
  public void testFlowControlQps() {
    MockUtil.getInstance().mockHighwayCodec();
    SchemaMeta schemaMeta = unitTestMeta.getOrCreateSchemaMeta(Impl.class);
    OperationMeta operationMeta = schemaMeta.ensureFindOperation("add");
    operationMeta.setExecutor(new ReactiveExecutor());
    Deencapsulation.setField(operationMeta, "providerQpsFlowControlHandlerSearched", true);
    Deencapsulation.setField(operationMeta, "providerQpsFlowControlHandler",
        (Handler) (invocation, asyncResp) -> asyncResp.producerFail(new InvocationException(
            new HttpStatus(429, "Too Many Requests"),
            new CommonExceptionData("rejected by qps flowcontrol"))));

    RequestHeader requestHeader = MockUtil.getInstance().requestHeader;
    requestHeader.setDestMicroservice(schemaMeta.getMicroserviceName());
    requestHeader.setSchemaId(schemaMeta.getSchemaId());
    requestHeader.setOperationName(operationMeta.getOperationId());

    HighwayServerInvoke highwayServerInvoke = new HighwayServerInvoke();

    assertTrue(highwayServerInvoke.init(connection, 0, requestHeader, null));
    MockUtil.getInstance().decodeRequestSucc = true;

    ArchaiusUtils.setProperty("servicecomb.flowcontrol.Provider.qps.enabled", "true");
    ArchaiusUtils.setProperty("servicecomb.flowcontrol.Provider.qps.global.limit", "1");

    highwayServerInvoke.execute();
    String bodyString = Buffer.buffer(netSocketBuffer).toString();
    assertTrue(bodyString.contains("Too Many Requests"));
    assertTrue(bodyString.contains("rejected by qps flowcontrol"));

    ArchaiusUtils.resetConfig();
  }
}
