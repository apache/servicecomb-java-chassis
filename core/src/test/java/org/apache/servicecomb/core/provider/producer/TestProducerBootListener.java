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

import java.util.Arrays;
import java.util.concurrent.Executor;

import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.core.BootListener.EventType;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
  public void onBootEvent_close_unknown() {
    SCBEngine scbEngine = Mockito.mock(SCBEngine.class);
    MicroserviceMeta microserviceMeta = Mockito.mock(MicroserviceMeta.class);
    OperationMeta op = Mockito.mock(OperationMeta.class);

    Executor executor = new UnCloseableExecutor();
    Mockito.when(scbEngine.getProducerMicroserviceMeta()).thenReturn(microserviceMeta);
    Mockito.when(microserviceMeta.getOperations()).thenReturn(Arrays.asList(op));
    Mockito.when(op.getExecutor()).thenReturn(executor);

    try (LogCollector logCollector = new LogCollector()) {
      BootEvent event = new BootEvent();
      event.setScbEngine(scbEngine);
      event.setEventType(EventType.AFTER_CLOSE);

      producerBootListener.onBootEvent(event);

      Assertions.assertEquals(
          "Executor org.apache.servicecomb.core.provider.producer.TestProducerBootListener$UnCloseableExecutor "
              + "do not support close or shutdown, it may block service shutdown.",
          logCollector.getLastEvents().getMessage().getFormattedMessage());
    }
  }

  public static class UnCloseableExecutor implements Executor {
    @Override
    public void execute(Runnable command) {
      command.run();
    }
  }
}
