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
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.executor.FixedThreadExecutor;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestProducerProviderManager {
  @Test
  public void allowedNoProvider() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(ProducerProviderManager.class);
    // must not throw exception
    context.refresh();

    context.close();
  }

  @Test
  public void onBootEvent_notClose() {
    BootEvent event = new BootEvent();
    event.setEventType(EventType.BEFORE_CLOSE);

    ProducerProviderManager producerProviderManager = new ProducerProviderManager();
    // should not throw exception
    producerProviderManager.onBootEvent(event);
  }

  @Test
  public void onBootEvent_close(@Mocked MicroserviceMeta microserviceMeta, @Mocked OperationMeta op1,
      @Mocked OperationMeta op2, @Mocked FixedThreadExecutor closeable) {
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
        microserviceMeta.getOperations();
        result = Arrays.asList(op1, op2);
        op1.getExecutor();
        result = executorService;
        op2.getExecutor();
        result = closeable;
      }
    };
    BootEvent event = new BootEvent();
    event.setEventType(EventType.AFTER_CLOSE);

    ProducerProviderManager producerProviderManager = new ProducerProviderManager();
    Deencapsulation.setField(producerProviderManager, "microserviceMeta", microserviceMeta);

    producerProviderManager.onBootEvent(event);

    Assert.assertEquals(2, count.get());
  }

  @Test
  public void onBootEvent_close_unknown(@Mocked MicroserviceMeta microserviceMeta, @Mocked OperationMeta op1) {
    Executor executor = new UnCloseableExecutor();
    new Expectations() {
      {
        microserviceMeta.getOperations();
        result = Arrays.asList(op1);
        op1.getExecutor();
        result = executor;
      }
    };
    LogCollector logCollector = new LogCollector();
    BootEvent event = new BootEvent();
    event.setEventType(EventType.AFTER_CLOSE);

    ProducerProviderManager producerProviderManager = new ProducerProviderManager();
    Deencapsulation.setField(producerProviderManager, "microserviceMeta", microserviceMeta);

    producerProviderManager.onBootEvent(event);

    Assert.assertEquals(
        "Executor org.apache.servicecomb.core.provider.producer.TestProducerProviderManager$UnCloseableExecutor "
            + "do not support close or shutdown, it may block service shutdown.",
        logCollector.getEvents().get(0).getMessage());
    logCollector.teardown();
  }

  public static class UnCloseableExecutor implements Executor {
    @Override
    public void execute(Runnable command) {
      command.run();
    }
  }
}
