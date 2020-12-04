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

import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.qps.strategy.AbstractQpsStrategy;
import org.apache.servicecomb.qps.strategy.FixedWindowStrategy;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestConsumerQpsFlowControlHandler {

  ConsumerQpsFlowControlHandler handler;

  Invocation invocation = Mockito.mock(Invocation.class);

  AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

  OperationMeta operationMeta = Mockito.mock(OperationMeta.class);

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUP() {
    ArchaiusUtils.resetConfig();
    handler = new ConsumerQpsFlowControlHandler();
  }


  @After
  public void afterTest() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testQpsController() {
    AbstractQpsStrategy qpsStrategy = new FixedWindowStrategy();
    qpsStrategy.setKey("abc");
    qpsStrategy.setQpsLimit(100L);
    Assert.assertEquals(false, qpsStrategy.isLimitNewRequest());

    qpsStrategy.setQpsLimit(1L);
    Assert.assertEquals(true, qpsStrategy.isLimitNewRequest());
  }

  @Test
  public void testHandle() throws Exception {
    String key = "svc.schema.opr";
    AbstractQpsStrategy qpsStrategy = new FixedWindowStrategy();
    qpsStrategy.setKey("key");
    qpsStrategy.setQpsLimit(12L);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("schema.opr");
    Mockito.when(invocation.getSchemaId()).thenReturn("schema");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("svc");
    setQpsController(key, qpsStrategy);
    new MockUp<FixedWindowStrategy>() {
      @Mock
      public boolean isLimitNewRequest() {
        return true;
      }
    };

    new MockUp<QpsControllerManager>() {
      @Mock
      protected QpsStrategy create(String qualifiedNameKey) {
        return qpsStrategy;
      }
    };

    handler.handle(invocation, asyncResp);

    ArgumentCaptor<InvocationException> captor = ArgumentCaptor.forClass(InvocationException.class);
    Mockito.verify(asyncResp).consumerFail(captor.capture());
    InvocationException invocationException = captor.getValue();
    assertEquals(QpsConst.TOO_MANY_REQUESTS_STATUS, invocationException.getStatus());
    assertEquals("rejected by qps flowcontrol",
        ((CommonExceptionData) invocationException.getErrorData()).getMessage());
  }

  @Test
  public void testHandleIsLimitNewRequestAsFalse() throws Exception {
    String key = "service.schema.id";
    AbstractQpsStrategy qpsStrategy = new FixedWindowStrategy();
    qpsStrategy.setKey("service");
    qpsStrategy.setQpsLimit(12L);
    Mockito.when(invocation.getMicroserviceName()).thenReturn("service");
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);

    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("schema.id");
    setQpsController(key, qpsStrategy);

    new MockUp<QpsStrategy>() {
      @Mock
      public boolean isLimitNewRequest() {
        return false;
      }
    };

    new MockUp<QpsControllerManager>() {

      @Mock
      protected QpsStrategy create(String qualifiedNameKey) {
        return qpsStrategy;
      }
    };
    handler.handle(invocation, asyncResp);

    Mockito.verify(invocation).next(asyncResp);
  }

  private void setQpsController(String key, QpsStrategy qpsStrategy) {
    QpsControllerManager qpsControllerManager = Deencapsulation.getField(handler, "qpsControllerMgr");
    ConcurrentHashMap<String, QpsStrategy> objMap = Deencapsulation
        .getField(qpsControllerManager, "qualifiedNameControllerMap");
    objMap.put(key, qpsStrategy);
  }
}
