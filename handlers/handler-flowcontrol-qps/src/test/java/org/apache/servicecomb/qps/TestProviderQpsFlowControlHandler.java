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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.qps.strategy.AbstractQpsStrategy;
import org.apache.servicecomb.qps.strategy.FixedWindowStrategy;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;

public class TestProviderQpsFlowControlHandler {
  ProviderQpsFlowControlHandler handler;

  Invocation invocation = Mockito.mock(Invocation.class);

  AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUP() {
    ArchaiusUtils.resetConfig();
    handler = new ProviderQpsFlowControlHandler();
    ArchaiusUtils.setProperty(Config.PROVIDER_LIMIT_KEY_PREFIX + "test", 1);
  }

  @After
  public void afterTest() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testGlobalQpsControl(final @Injectable Invocation invocation,
      final @Injectable AsyncResponse asyncResp) throws Exception {
    new Expectations() {
      {
        invocation.getHandlerIndex();
        result = 0;
        invocation.getContext(Const.SRC_MICROSERVICE);
        result = "test";
        invocation.getOperationMeta();
        result = QpsControllerManagerTest.getMockOperationMeta("pojo", "server", "opr");
        invocation.getSchemaId();
        result = "server";
        asyncResp.producerFail((Throwable) any);
        result = new RuntimeException("test error");
      }
    };

    ProviderQpsFlowControlHandler gHandler = new ProviderQpsFlowControlHandler();
    gHandler.handle(invocation, asyncResp);

    ArchaiusUtils.setProperty(Config.PROVIDER_LIMIT_KEY_GLOBAL, 3);

    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("test error");

    gHandler.handle(invocation, asyncResp);
    gHandler.handle(invocation, asyncResp);
  }

  @Test
  public void testQpsController() {
    AbstractQpsStrategy qpsStrategy = new FixedWindowStrategy();
    qpsStrategy.setKey("abc");
    qpsStrategy.setQpsLimit(100L);
    assertFalse(qpsStrategy.isLimitNewRequest());

    qpsStrategy.setQpsLimit(1L);
    assertTrue(qpsStrategy.isLimitNewRequest());
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
    Mockito.verify(invocation, times(2)).getContext(Const.SRC_MICROSERVICE);
    Mockito.verify(asyncResp, times(1)).producerFail(Mockito.any(Exception.class));
  }

  @Test
  public void testHandleOnSourceOnHandlerIndexIsGreaterThan0() throws Exception {
    Mockito.when(invocation.getContext(Const.SRC_MICROSERVICE)).thenReturn(null);

    Mockito.when(invocation.getHandlerIndex()).thenReturn(1);
    handler.handle(invocation, asyncResp);
    handler.handle(invocation, asyncResp);

    Mockito.verify(invocation, times(0)).getContext(Mockito.anyString());
  }

  @Test
  public void testHandle() throws Exception {
    Mockito.when(invocation.getContext(Const.SRC_MICROSERVICE)).thenReturn("test");
    OperationMeta mockOperationMeta = QpsControllerManagerTest.getMockOperationMeta("pojo", "server", "opr");
    Mockito.when(invocation.getOperationMeta()).thenReturn(mockOperationMeta);
    Mockito.when(invocation.getSchemaId()).thenReturn("server");

    new MockUp<QpsControllerManager>() {
      @Mock
      protected QpsStrategy create(String qualifiedNameKey) {
        AbstractQpsStrategy strategy = new FixedWindowStrategy();
        strategy.setKey(qualifiedNameKey);
        strategy.setQpsLimit(1L);
        return strategy;
      }
    };

    handler.handle(invocation, asyncResp);
    handler.handle(invocation, asyncResp);

    ArgumentCaptor<InvocationException> captor = ArgumentCaptor.forClass(InvocationException.class);
    Mockito.verify(asyncResp, times(1)).producerFail(captor.capture());

    InvocationException invocationException = captor.getValue();
    assertEquals(QpsConst.TOO_MANY_REQUESTS_STATUS, invocationException.getStatus());
    assertEquals("rejected by qps flowcontrol",
        ((CommonExceptionData) invocationException.getErrorData()).getMessage());
  }

  @Test
  public void testHandleIsLimitNewRequestAsFalse() throws Exception {
    Mockito.when(invocation.getContext(Const.SRC_MICROSERVICE)).thenReturn("test");
    OperationMeta mockOperationMeta = QpsControllerManagerTest
        .getMockOperationMeta("pojo", "server", "opr");
    Mockito.when(invocation.getOperationMeta()).thenReturn(mockOperationMeta);
    Mockito.when(invocation.getSchemaId()).thenReturn("server");

    new MockUp<QpsControllerManager>() {
      @Mock
      protected QpsStrategy create(String qualifiedNameKey) {
        AbstractQpsStrategy strategy = new FixedWindowStrategy();
        strategy.setKey(qualifiedNameKey);
        strategy.setQpsLimit(1L);
        return strategy;
      }
    };
    handler.handle(invocation, asyncResp);

    Mockito.verify(invocation, times(0)).next(asyncResp);
    Mockito.verify(asyncResp, times(0)).producerFail(Mockito.any(Exception.class));
  }
}
