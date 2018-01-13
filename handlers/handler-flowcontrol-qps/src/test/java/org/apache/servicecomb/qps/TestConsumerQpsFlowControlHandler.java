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

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import mockit.Mock;
import mockit.MockUp;

/**
 *
 *
 */
public class TestConsumerQpsFlowControlHandler {

  ConsumerQpsFlowControlHandler handler = new ConsumerQpsFlowControlHandler();

  Invocation invocation = Mockito.mock(Invocation.class);

  AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

  OperationMeta operationMeta = Mockito.mock(OperationMeta.class);

  @Test
  public void testQpsController() throws Exception {
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
      Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
      Mockito.when(operationMeta.getMicroserviceQualifiedName()).thenReturn("MicroserviceQualifiedName");

      new MockUp<QpsController>() {
        @Mock
        public boolean isLimitNewRequest() {
          return true;
        }
      };

      new MockUp<ConsumerQpsControllerManager>() {

        @Mock
        protected QpsController create(OperationMeta operationMeta) {
          return new QpsController("key", 12);
        }
      };
      handler.handle(invocation, asyncResp);
    } catch (Exception e) {
      validAssert = false;
    }
    Assert.assertTrue(validAssert);
  }

  @Test
  public void testHandleIsLimitNewRequestAsFalse() {
    boolean validAssert;
    try {
      validAssert = true;
      Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
      Mockito.when(operationMeta.getMicroserviceQualifiedName()).thenReturn("MicroserviceQualifiedName");

      new MockUp<QpsController>() {
        @Mock
        public boolean isLimitNewRequest() {
          return false;
        }
      };

      new MockUp<ConsumerQpsControllerManager>() {

        @Mock
        protected QpsController create(OperationMeta operationMeta) {
          return new QpsController("key", 12);
        }
      };
      handler.handle(invocation, asyncResp);
    } catch (Exception e) {
      validAssert = false;
    }
    Assert.assertTrue(validAssert);
  }
}
