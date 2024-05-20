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
import org.junit.jupiter.api.Assertions;

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

    stageTrace.startCreateInvocation(1);
    nanoTime = 2;
    stageTrace.finishCreateInvocation();
    stageTrace.startConsumerConnection();
    nanoTime = 3;
    stageTrace.finishConsumerConnection();
    stageTrace.startConsumerEncodeRequest();
    nanoTime = 4;
    stageTrace.finishConsumerEncodeRequest();
    stageTrace.startConsumerSendRequest();
    nanoTime = 5;
    stageTrace.finishConsumerSendRequest();
    stageTrace.startWaitResponse();
    nanoTime = 6;
    stageTrace.finishWaitResponse();
    stageTrace.startConsumerDecodeResponse();
    nanoTime = 7;
    stageTrace.finishConsumerDecodeResponse();

    stageTrace.finish();

    Assertions.assertEquals(1f, stageTrace.calcPrepare(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcConnection(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcConsumerEncodeRequest(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcConsumerSendRequest(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcWait(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcConsumerDecodeResponse(), 0.1f);
    Assertions.assertEquals(6f, stageTrace.calcTotal(), 0.1f);
  }

  @Test
  public void producer() {
    invocation = new Invocation(endpoint, operationMeta, args);
    stageTrace = new InvocationStageTrace(invocation);

    stageTrace.startCreateInvocation(1);
    nanoTime = 2;
    stageTrace.finishCreateInvocation();
    stageTrace.startProviderDecodeRequest();
    nanoTime = 3;
    stageTrace.finishProviderDecodeRequest();
    stageTrace.startProviderQueue();
    nanoTime = 4;
    stageTrace.finishProviderQueue();
    stageTrace.startBusinessExecute();
    nanoTime = 5;
    stageTrace.finishBusinessExecute();
    stageTrace.startProviderEncodeResponse();
    nanoTime = 6;
    stageTrace.finishProviderEncodeResponse();
    stageTrace.startProviderSendResponse();
    nanoTime = 7;
    stageTrace.finishProviderSendResponse();

    stageTrace.finish();

    Assertions.assertEquals(1f, stageTrace.calcPrepare(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcQueue(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcProviderDecodeRequest(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcProviderEncodeResponse(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcBusinessExecute(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcProviderSendResponse(), 0.1f);
    Assertions.assertEquals(6f, stageTrace.calcTotal(), 0.1f);
  }

  @Test
  public void edge() {
    invocation = new Invocation(referenceConfig, operationMeta, invocationRuntimeType, args);
    stageTrace = new InvocationStageTrace(invocation);
    invocation.setEdge();

    stageTrace.startCreateInvocation(1);
    nanoTime = 2;
    stageTrace.finishCreateInvocation();
    stageTrace.startProviderDecodeRequest();
    nanoTime = 3;
    stageTrace.finishProviderDecodeRequest();
    stageTrace.startConsumerConnection();
    nanoTime = 4;
    stageTrace.finishConsumerConnection();
    stageTrace.startConsumerEncodeRequest();
    nanoTime = 5;
    stageTrace.finishConsumerEncodeRequest();
    stageTrace.startConsumerSendRequest();
    nanoTime = 6;
    stageTrace.finishConsumerSendRequest();
    stageTrace.startWaitResponse();
    nanoTime = 7;
    stageTrace.finishWaitResponse();
    stageTrace.startConsumerDecodeResponse();
    nanoTime = 8;
    stageTrace.finishConsumerDecodeResponse();
    stageTrace.startProviderEncodeResponse();
    nanoTime = 10;
    stageTrace.finishProviderEncodeResponse();
    stageTrace.startProviderSendResponse();
    nanoTime = 11;
    stageTrace.finishProviderSendResponse();
    stageTrace.finish();

    Assertions.assertEquals(1f, stageTrace.calcPrepare(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcConnection(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcConsumerEncodeRequest(), 0.1f);

    Assertions.assertEquals(1f, stageTrace.calcConsumerSendRequest(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcWait(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcConsumerDecodeResponse(), 0.1f);
    Assertions.assertEquals(2f, stageTrace.calcProviderEncodeResponse(), 0.1f);
    Assertions.assertEquals(1f, stageTrace.calcProviderSendResponse(), 0.1f);

    Assertions.assertEquals(10f, stageTrace.calcTotal(), 0.1f);
  }
}
