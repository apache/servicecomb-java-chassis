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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.InvocationRuntimeType;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.junit.BeforeClass;
import org.junit.Test;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestInvocationStageTrace {
  Invocation invocation;

  InvocationStageTrace stageTrace;

  @Mocked
  Endpoint endpoint;

  @Mocked
  ReferenceConfig referenceConfig;

  @Mocked
  OperationMeta operationMeta;

  @Mocked
  InvocationRuntimeType invocationRuntimeType;

  Map<String, Object> args = new HashMap<>();

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
    invocation = new Invocation(referenceConfig, operationMeta, invocationRuntimeType, args);
    stageTrace = new InvocationStageTrace(invocation);

    stageTrace.start(1);
    nanoTime = 2;
    stageTrace.startHandlersRequest();
    nanoTime = 3;
    stageTrace.startClientFiltersRequest();
    nanoTime = 4;
    stageTrace.startGetConnection();
    stageTrace.startSend();
    nanoTime = 5;
    stageTrace.finishGetConnection();
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

    Assertions.assertEquals(1, stageTrace.getStart());
    Assertions.assertEquals(2, stageTrace.getStartHandlersRequest());
    Assertions.assertEquals(3, stageTrace.getStartClientFiltersRequest());
    Assertions.assertEquals(4, stageTrace.getStartSend());
    Assertions.assertEquals(5, stageTrace.getFinishGetConnection());
    Assertions.assertEquals(6, stageTrace.getFinishWriteToBuffer());
    Assertions.assertEquals(7, stageTrace.getFinishReceiveResponse());
    Assertions.assertEquals(8, stageTrace.getStartClientFiltersResponse());
    Assertions.assertEquals(9, stageTrace.getFinishClientFiltersResponse());
    Assertions.assertEquals(10, stageTrace.getFinishHandlersResponse());
    Assertions.assertEquals(11, stageTrace.getFinish());

    Assertions.assertEquals(1f, stageTrace.calcInvocationPrepareTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcHandlersRequestTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcClientFiltersRequestTime(), 0.1f);
    Assertions.assertEquals(2f, stageTrace.calcSendRequestTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcGetConnectionTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcWriteToBufferTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcReceiveResponseTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcWakeConsumer(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcClientFiltersResponseTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcHandlersResponseTime(), 0.1f);
    Assertions.assertEquals(10f, stageTrace.calcTotalTime(), 0.1f);
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

    Assertions.assertEquals(1, stageTrace.getStart());
    Assertions.assertEquals(2, stageTrace.getStartSchedule());
    Assertions.assertEquals(3, stageTrace.getStartExecution());
    Assertions.assertEquals(4, stageTrace.getStartServerFiltersRequest());
    Assertions.assertEquals(5, stageTrace.getStartHandlersRequest());
    Assertions.assertEquals(6, stageTrace.getStartBusinessMethod());
    Assertions.assertEquals(7, stageTrace.getFinishBusiness());
    Assertions.assertEquals(8, stageTrace.getFinishHandlersResponse());
    Assertions.assertEquals(9, stageTrace.getFinishServerFiltersResponse());
    Assertions.assertEquals(10, stageTrace.getFinish());

    Assertions.assertEquals(1f, stageTrace.calcInvocationPrepareTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcThreadPoolQueueTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcServerFiltersRequestTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcHandlersRequestTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcBusinessTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcHandlersResponseTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcServerFiltersResponseTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcSendResponseTime(), 0.1f);
    Assertions.assertEquals(9f, stageTrace.calcTotalTime(), 0.1f);
  }

  @Test
  public void edge() {
    invocation = new Invocation(referenceConfig, operationMeta, invocationRuntimeType, args);
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
    stageTrace.startGetConnection();
    stageTrace.startSend();
    nanoTime = 8;
    stageTrace.finishGetConnection();
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

    Assertions.assertEquals(1, stageTrace.getStart());
    Assertions.assertEquals(2, stageTrace.getStartSchedule());
    Assertions.assertEquals(3, stageTrace.getStartExecution());
    Assertions.assertEquals(4, stageTrace.getStartServerFiltersRequest());
    Assertions.assertEquals(5, stageTrace.getStartHandlersRequest());
    Assertions.assertEquals(6, stageTrace.getStartClientFiltersRequest());
    Assertions.assertEquals(7, stageTrace.getStartSend());
    Assertions.assertEquals(8, stageTrace.getFinishGetConnection());
    Assertions.assertEquals(9, stageTrace.getFinishWriteToBuffer());
    Assertions.assertEquals(10, stageTrace.getFinishReceiveResponse());
    Assertions.assertEquals(11, stageTrace.getStartClientFiltersResponse());
    Assertions.assertEquals(12, stageTrace.getFinishClientFiltersResponse());
    Assertions.assertEquals(13, stageTrace.getFinishHandlersResponse());
    Assertions.assertEquals(14, stageTrace.getFinishServerFiltersResponse());
    Assertions.assertEquals(15, stageTrace.getFinish());

    Assertions.assertEquals(1f, stageTrace.calcInvocationPrepareTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcThreadPoolQueueTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcServerFiltersRequestTime(), 0.1f);

    Assertions.assertEquals(1f, stageTrace.calcHandlersRequestTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcClientFiltersRequestTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcGetConnectionTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcWriteToBufferTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcReceiveResponseTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcWakeConsumer(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcClientFiltersResponseTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcHandlersResponseTime(), 0.1f);

    Assertions.assertEquals(1f, stageTrace.calcServerFiltersResponseTime(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcSendResponseTime(), 0.1f);

    Assertions.assertEquals(14f, stageTrace.calcTotalTime(), 0.1f);
  }
}
