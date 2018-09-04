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
package org.apache.servicecomb.core.invocation;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestInvocationStageTrace {
  Invocation invocation;

  InvocationStageTrace stageTrace;

  @Mocked
  Endpoint endpoint;

  @Mocked
  ReferenceConfig referenceConfig;

  @Mocked
  OperationMeta operationMeta;

  Object[] args = new Object[] {};

  static long nanoTime = 0;

  @BeforeClass
  public static void classSetup() {
    new MockUp<System>() {
      @Mock
      long nanoTime() {
        return nanoTime;
      }
    };
  }

  @Test
  public void consumer() {
    invocation = new Invocation(referenceConfig, operationMeta, args);
    stageTrace = new InvocationStageTrace(invocation);

    stageTrace.start(1);
    nanoTime = 2;
    stageTrace.startHandlersRequest();
    nanoTime = 3;
    stageTrace.startClientFiltersRequest();
    nanoTime = 4;
    stageTrace.startSend();
    stageTrace.finishGetConnection(5);
    stageTrace.finishWriteToBuffer(6);
    nanoTime = 7;
    stageTrace.finishReceiveResponse();
    nanoTime = 8;
    stageTrace.startClientFiltersResponse();
    nanoTime = 9;
    stageTrace.finishClientFiltersResponse();
    nanoTime = 10;
    stageTrace.finishHandlersResponse();
    nanoTime = 11;
    stageTrace.finish();

    Assert.assertEquals(1, stageTrace.getStart());
    Assert.assertEquals(2, stageTrace.getStartHandlersRequest());
    Assert.assertEquals(3, stageTrace.getStartClientFiltersRequest());
    Assert.assertEquals(4, stageTrace.getStartSend());
    Assert.assertEquals(5, stageTrace.getFinishGetConnection());
    Assert.assertEquals(6, stageTrace.getFinishWriteToBuffer());
    Assert.assertEquals(7, stageTrace.getFinishReceiveResponse());
    Assert.assertEquals(8, stageTrace.getStartClientFiltersResponse());
    Assert.assertEquals(9, stageTrace.getFinishClientFiltersResponse());
    Assert.assertEquals(10, stageTrace.getFinishHandlersResponse());
    Assert.assertEquals(11, stageTrace.getFinish());

    Assert.assertEquals(1f, stageTrace.calcInvocationPrepareTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcHandlersRequestTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcClientFiltersRequestTime(), 0.1f);
    Assert.assertEquals(2f, stageTrace.calcSendRequestTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcGetConnectionTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcWriteToBufferTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcReceiveResponseTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcWakeConsumer(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcClientFiltersResponseTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcHandlersResponseTime(), 0.1f);
    Assert.assertEquals(10f, stageTrace.calcTotalTime(), 0.1f);
  }

  @Test
  public void producer() {
    invocation = new Invocation(endpoint, operationMeta, args);
    stageTrace = new InvocationStageTrace(invocation);

    stageTrace.start(1);
    nanoTime = 2;
    stageTrace.startSchedule();
    nanoTime = 3;
    stageTrace.startExecution();
    nanoTime = 4;
    stageTrace.startServerFiltersRequest();
    nanoTime = 5;
    stageTrace.startHandlersRequest();
    nanoTime = 6;
    stageTrace.startBusinessMethod();
    nanoTime = 7;
    stageTrace.finishBusiness();
    nanoTime = 8;
    stageTrace.finishHandlersResponse();
    nanoTime = 9;
    stageTrace.finishServerFiltersResponse();
    nanoTime = 10;
    stageTrace.finish();

    Assert.assertEquals(1, stageTrace.getStart());
    Assert.assertEquals(2, stageTrace.getStartSchedule());
    Assert.assertEquals(3, stageTrace.getStartExecution());
    Assert.assertEquals(4, stageTrace.getStartServerFiltersRequest());
    Assert.assertEquals(5, stageTrace.getStartHandlersRequest());
    Assert.assertEquals(6, stageTrace.getStartBusinessMethod());
    Assert.assertEquals(7, stageTrace.getFinishBusiness());
    Assert.assertEquals(8, stageTrace.getFinishHandlersResponse());
    Assert.assertEquals(9, stageTrace.getFinishServerFiltersResponse());
    Assert.assertEquals(10, stageTrace.getFinish());

    Assert.assertEquals(1f, stageTrace.calcInvocationPrepareTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcThreadPoolQueueTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcServerFiltersRequestTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcHandlersRequestTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcBusinessTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcHandlersResponseTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcServerFiltersResponseTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcSendResponseTime(), 0.1f);
    Assert.assertEquals(9f, stageTrace.calcTotalTime(), 0.1f);
  }

  @Test
  public void edge() {
    invocation = new Invocation(referenceConfig, operationMeta, args);
    stageTrace = new InvocationStageTrace(invocation);
    invocation.setEdge(true);

    stageTrace.start(1);
    nanoTime = 2;
    stageTrace.startSchedule();
    nanoTime = 3;
    stageTrace.startExecution();
    nanoTime = 4;
    stageTrace.startServerFiltersRequest();
    nanoTime = 5;
    stageTrace.startHandlersRequest();
    nanoTime = 6;
    stageTrace.startClientFiltersRequest();
    nanoTime = 7;
    stageTrace.startSend();
    stageTrace.finishGetConnection(8);
    stageTrace.finishWriteToBuffer(9);
    nanoTime = 10;
    stageTrace.finishReceiveResponse();
    nanoTime = 11;
    stageTrace.startClientFiltersResponse();
    nanoTime = 12;
    stageTrace.finishClientFiltersResponse();
    nanoTime = 13;
    stageTrace.finishHandlersResponse();
    nanoTime = 14;
    stageTrace.finishServerFiltersResponse();
    nanoTime = 15;
    stageTrace.finish();

    Assert.assertEquals(1, stageTrace.getStart());
    Assert.assertEquals(2, stageTrace.getStartSchedule());
    Assert.assertEquals(3, stageTrace.getStartExecution());
    Assert.assertEquals(4, stageTrace.getStartServerFiltersRequest());
    Assert.assertEquals(5, stageTrace.getStartHandlersRequest());
    Assert.assertEquals(6, stageTrace.getStartClientFiltersRequest());
    Assert.assertEquals(7, stageTrace.getStartSend());
    Assert.assertEquals(8, stageTrace.getFinishGetConnection());
    Assert.assertEquals(9, stageTrace.getFinishWriteToBuffer());
    Assert.assertEquals(10, stageTrace.getFinishReceiveResponse());
    Assert.assertEquals(11, stageTrace.getStartClientFiltersResponse());
    Assert.assertEquals(12, stageTrace.getFinishClientFiltersResponse());
    Assert.assertEquals(13, stageTrace.getFinishHandlersResponse());
    Assert.assertEquals(14, stageTrace.getFinishServerFiltersResponse());
    Assert.assertEquals(15, stageTrace.getFinish());

    Assert.assertEquals(1f, stageTrace.calcInvocationPrepareTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcThreadPoolQueueTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcServerFiltersRequestTime(), 0.1f);

    Assert.assertEquals(1f, stageTrace.calcHandlersRequestTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcClientFiltersRequestTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcGetConnectionTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcWriteToBufferTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcReceiveResponseTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcWakeConsumer(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcClientFiltersResponseTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcHandlersResponseTime(), 0.1f);

    Assert.assertEquals(1f, stageTrace.calcServerFiltersResponseTime(), 0.1f);
    Assert.assertEquals(1f, stageTrace.calcSendResponseTime(), 0.1f);

    Assert.assertEquals(14f, stageTrace.calcTotalTime(), 0.1f);
  }
}
