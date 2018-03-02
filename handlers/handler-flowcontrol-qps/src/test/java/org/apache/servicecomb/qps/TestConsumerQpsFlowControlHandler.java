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

import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestConsumerQpsFlowControlHandler {

  ConsumerQpsFlowControlHandler handler = new ConsumerQpsFlowControlHandler();

  Invocation invocation = Mockito.mock(Invocation.class);

  AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

  OperationMeta operationMeta = Mockito.mock(OperationMeta.class);

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
    QpsController qpsController = new QpsController("abc", 100);
    Assert.assertEquals(false, qpsController.isLimitNewRequest());

    qpsController.setQpsLimit(1);
    Assert.assertEquals(true, qpsController.isLimitNewRequest());
  }

  @Test
  public void testHandleWithException() {
    boolean validAssert;
    try {
      validAssert = true;

      handler.handle(invocation, asyncResp);
    } catch (Exception e) {
      validAssert = false;
    }
    Assert.assertFalse(validAssert);
  }

  @Test
  public void testHandle() {
    boolean validAssert;
    try {
      validAssert = true;
      String key = "svc.schema.opr";
      QpsController qpsController = new QpsController("key", 12);
      Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
      Mockito.when(operationMeta.getMicroserviceQualifiedName()).thenReturn(key);
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
    } catch (Exception e) {
      e.printStackTrace();
      validAssert = false;
    }
    Assert.assertTrue(validAssert);
  }

  @Test
  public void testHandleIsLimitNewRequestAsFalse() {
    boolean validAssert;
    try {
      validAssert = true;
      String key = "MicroserviceQualifiedName";
      QpsController qpsController = new QpsController("key", 12);
      Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
      Mockito.when(operationMeta.getMicroserviceQualifiedName()).thenReturn(key);
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
    } catch (Exception e) {
      e.printStackTrace();
      validAssert = false;
    }
    Assert.assertTrue(validAssert);
  }

  private void setQpsController(String key, QpsController qpsController) {
    QpsControllerManager qpsControllerManager = Deencapsulation.getField(handler, "qpsControllerMgr");
    ConcurrentHashMap<String, QpsController> objMap = Deencapsulation
        .getField(qpsControllerManager, "qualifiedNameControllerMap");
    objMap.put(key, qpsController);
  }
}
