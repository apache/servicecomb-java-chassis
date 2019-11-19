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

package org.apache.servicecomb.core.provider.producer;

import java.io.Closeable;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.core.BootListener.EventType;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.executor.GroupExecutor;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestProducerBootListener {
  ProducerBootListener producerBootListener = new ProducerBootListener();

  @Test
  public void onBootEvent_notClose() {
    BootEvent event = new BootEvent();
    event.setEventType(EventType.BEFORE_CLOSE);

    // should not throw exception
    producerBootListener.onBootEvent(event);
  }

  @Test
  public void onBootEvent_close(@Mocked SCBEngine scbEngine, @Mocked MicroserviceMeta microserviceMeta,
      @Mocked OperationMeta op1,
      @Mocked OperationMeta op2, @Mocked GroupExecutor closeable) {
    AtomicInteger count = new AtomicInteger();
    ExecutorService executorService = new MockUp<ExecutorService>() {
      @Mock
      void shutdown() {
        count.incrementAndGet();
      }
    }.getMockInstance();
    new MockUp<Closeable>(closeable) {
      @Mock
      void close() {
        count.incrementAndGet();
      }
    };
    new Expectations() {
      {
        scbEngine.getProducerMicroserviceMeta();
        result = microserviceMeta;
        microserviceMeta.getOperations();
        result = Arrays.asList(op1, op2);
        op1.getExecutor();
        result = executorService;
        op2.getExecutor();
        result = closeable;
      }
    };
    BootEvent event = new BootEvent();
    event.setScbEngine(scbEngine);
    event.setEventType(EventType.AFTER_CLOSE);

    producerBootListener.onBootEvent(event);

    Assert.assertEquals(2, count.get());
  }

  @Test
  public void onBootEvent_close_unknown(@Mocked SCBEngine scbEngine, @Mocked MicroserviceMeta microserviceMeta,
      @Mocked OperationMeta op1) {
    Executor executor = new UnCloseableExecutor();
    new Expectations() {
      {
        scbEngine.getProducerMicroserviceMeta();
        result = microserviceMeta;
        microserviceMeta.getOperations();
        result = Arrays.asList(op1);
        op1.getExecutor();
        result = executor;
      }
    };

    try (LogCollector logCollector = new LogCollector()) {
      BootEvent event = new BootEvent();
      event.setScbEngine(scbEngine);
      event.setEventType(EventType.AFTER_CLOSE);

      producerBootListener.onBootEvent(event);

      Assert.assertEquals(
          "Executor org.apache.servicecomb.core.provider.producer.TestProducerBootListener$UnCloseableExecutor "
              + "do not support close or shutdown, it may block service shutdown.",
          logCollector.getLastEvents().getMessage());
    }
  }

  public static class UnCloseableExecutor implements Executor {
    @Override
    public void execute(Runnable command) {
      command.run();
    }
  }
}
