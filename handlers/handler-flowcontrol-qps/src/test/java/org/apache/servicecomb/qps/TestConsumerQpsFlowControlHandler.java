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

  ConsumerQpsFlowControlHandler handler = new ConsumerQpsFlowControlHandler();

  Invocation invocation = Mockito.mock(Invocation.class);

  AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

  OperationMeta operationMeta = Mockito.mock(OperationMeta.class);

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUP() {
    ArchaiusUtils.resetConfig();
    QpsControllerManagerTest.clearState(ConsumerQpsFlowControlHandler.qpsControllerMgr);
  }


  @After
  public void afterTest() {
    ArchaiusUtils.resetConfig();
    QpsControllerManagerTest.clearState(ConsumerQpsFlowControlHandler.qpsControllerMgr);
  }

  @Test
  public void testQpsController() {
    // to avoid time influence on QpsController
    new MockUp<System>() {
      @Mock
      long currentTimeMillis() {
        return 1L;
      }
    };
    QpsController qpsController = new QpsController("abc", 100);
    Assert.assertEquals(false, qpsController.isLimitNewRequest());

    qpsController.setQpsLimit(1);
    Assert.assertEquals(true, qpsController.isLimitNewRequest());
  }

  @Test
  public void testHandle() throws Exception {
    String key = "svc.schema.opr";
    QpsController qpsController = new QpsController("key", 12);
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("schema.opr");
    Mockito.when(invocation.getSchemaId()).thenReturn("schema");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("svc");
    setQpsController(key, qpsController);
    new MockUp<QpsController>() {
      @Mock
      public boolean isLimitNewRequest() {
        return true;
      }
    };

    new MockUp<QpsControllerManager>() {
      @Mock
      protected QpsController create(String qualifiedNameKey) {
        return qpsController;
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
    QpsController qpsController = new QpsController("service", 12);
    Mockito.when(invocation.getMicroserviceName()).thenReturn("service");
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    
    Mockito.when(operationMeta.getSchemaQualifiedName()).thenReturn("schema.id");
    setQpsController(key, qpsController);

    new MockUp<QpsController>() {
      @Mock
      public boolean isLimitNewRequest() {
        return false;
      }
    };

    new MockUp<QpsControllerManager>() {

      @Mock
      protected QpsController create(String qualifiedNameKey) {
        return qpsController;
      }
    };
    handler.handle(invocation, asyncResp);

    Mockito.verify(invocation).next(asyncResp);
  }

  private void setQpsController(String key, QpsController qpsController) {
    QpsControllerManager qpsControllerManager = Deencapsulation.getField(handler, "qpsControllerMgr");
    ConcurrentHashMap<String, QpsController> objMap = Deencapsulation
        .getField(qpsControllerManager, "qualifiedNameControllerMap");
    objMap.put(key, qpsController);
  }
}
