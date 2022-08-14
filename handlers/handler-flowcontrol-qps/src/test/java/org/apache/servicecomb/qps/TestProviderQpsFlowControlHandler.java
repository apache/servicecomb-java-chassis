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

package org.apache.servicecomb.qps;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.qps.strategy.AbstractQpsStrategy;
import org.apache.servicecomb.qps.strategy.FixedWindowStrategy;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class TestProviderQpsFlowControlHandler {
  ProviderQpsFlowControlHandler handler;

  final Invocation invocation = Mockito.mock(Invocation.class);

  final AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

  @BeforeEach
  public void setUP() {
    ArchaiusUtils.resetConfig();
    handler = new ProviderQpsFlowControlHandler();
    ArchaiusUtils.setProperty(Config.PROVIDER_LIMIT_KEY_PREFIX + "test", 1);
  }

  @AfterEach
  public void afterTest() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testGlobalQpsControl() throws Exception {
    Invocation invocation = Mockito.mock(Invocation.class);
    AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);
    OperationMeta operationMeta = QpsControllerManagerTest.getMockOperationMeta("pojo", "server", "opr");

    Mockito.when(invocation.getHandlerIndex()).thenReturn(0);
    Mockito.when(invocation.getContext(Const.SRC_MICROSERVICE)).thenReturn("test");
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(invocation.getSchemaId()).thenReturn("server");
    Mockito.doThrow(new RuntimeException("test error")).when(asyncResp).producerFail(Mockito.any());

    ProviderQpsFlowControlHandler gHandler = new ProviderQpsFlowControlHandler();
    gHandler.handle(invocation, asyncResp);

    ArchaiusUtils.setProperty(Config.PROVIDER_LIMIT_KEY_GLOBAL, 3);

    RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
      gHandler.handle(invocation, asyncResp);
      gHandler.handle(invocation, asyncResp);
    });
    Assertions.assertEquals("test error", exception.getMessage());
  }

  @Test
  public void testQpsController() {
    AbstractQpsStrategy qpsStrategy = new FixedWindowStrategy();
    qpsStrategy.setKey("abc");
    qpsStrategy.setQpsLimit(100L);
    Assertions.assertFalse(qpsStrategy.isLimitNewRequest());

    qpsStrategy.setQpsLimit(1L);
    Assertions.assertTrue(qpsStrategy.isLimitNewRequest());
  }

  @Test
  public void testHandleOnSourceMicroserviceNameIsNull() throws Exception {
    Mockito.when(invocation.getContext(Const.SRC_MICROSERVICE)).thenReturn(null);
    OperationMeta operationMeta = QpsControllerManagerTest.getMockOperationMeta("pojo", "server", "opr");
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(invocation.getSchemaId()).thenReturn("server");
    // only when handler index <= 0, the qps logic works
    Mockito.when(invocation.getHandlerIndex()).thenReturn(0);
    ArchaiusUtils.setProperty("servicecomb.flowcontrol.Provider.qps.global.limit", 1);

    handler.handle(invocation, asyncResp);
    handler.handle(invocation, asyncResp);

    // Invocation#getContext(String) is only invoked when the qps logic works
    Mockito.verify(invocation, Mockito.times(2)).getContext(Const.SRC_MICROSERVICE);
    Mockito.verify(asyncResp, Mockito.times(1)).producerFail(Mockito.any(Exception.class));
  }

  @Test
  public void testHandleOnSourceOnHandlerIndexIsGreaterThan0() throws Exception {
    Mockito.when(invocation.getContext(Const.SRC_MICROSERVICE)).thenReturn(null);

    Mockito.when(invocation.getHandlerIndex()).thenReturn(1);
    handler.handle(invocation, asyncResp);
    handler.handle(invocation, asyncResp);

    Mockito.verify(invocation, Mockito.times(0)).getContext(Mockito.anyString());
  }

  @Test
  public void testHandle() throws Exception {
    Mockito.when(invocation.getContext(Const.SRC_MICROSERVICE)).thenReturn("test");
    OperationMeta mockOperationMeta = QpsControllerManagerTest.getMockOperationMeta("pojo", "server", "opr");
    Mockito.when(invocation.getOperationMeta()).thenReturn(mockOperationMeta);
    Mockito.when(invocation.getSchemaId()).thenReturn("server");

    AbstractQpsStrategy strategy = new FixedWindowStrategy();
    strategy.setKey("");
    strategy.setQpsLimit(1L);
    QpsControllerManager qpsControllerMgr = Mockito.spy(handler.getQpsControllerMgr());
    Mockito.doReturn(strategy).when(qpsControllerMgr).create("test.server.opr", "test", invocation);

    handler.handle(invocation, asyncResp);
    handler.handle(invocation, asyncResp);

    ArgumentCaptor<InvocationException> captor = ArgumentCaptor.forClass(InvocationException.class);
    Mockito.verify(asyncResp, Mockito.times(1)).producerFail(captor.capture());

    InvocationException invocationException = captor.getValue();
    Assertions.assertEquals(QpsConst.TOO_MANY_REQUESTS_STATUS, invocationException.getStatus());
    Assertions.assertEquals("provider request rejected by qps flowcontrol",
        ((CommonExceptionData) invocationException.getErrorData()).getMessage());
  }

  @Test
  public void testHandleIsLimitNewRequestAsFalse() throws Exception {
    Mockito.when(invocation.getContext(Const.SRC_MICROSERVICE)).thenReturn("test");
    OperationMeta mockOperationMeta = QpsControllerManagerTest
        .getMockOperationMeta("pojo", "server", "opr");
    Mockito.when(invocation.getOperationMeta()).thenReturn(mockOperationMeta);
    Mockito.when(invocation.getSchemaId()).thenReturn("server");

    AbstractQpsStrategy strategy = new FixedWindowStrategy();
    strategy.setKey("");
    strategy.setQpsLimit(1L);
    QpsControllerManager qpsControllerMgr = Mockito.spy(handler.getQpsControllerMgr());
    Mockito.doReturn(strategy).when(qpsControllerMgr).create("test.server.opr", "test", invocation);

    handler.handle(invocation, asyncResp);

    Mockito.verify(invocation, Mockito.times(0)).next(asyncResp);
    Mockito.verify(asyncResp, Mockito.times(0)).producerFail(Mockito.any(Exception.class));
  }
}
