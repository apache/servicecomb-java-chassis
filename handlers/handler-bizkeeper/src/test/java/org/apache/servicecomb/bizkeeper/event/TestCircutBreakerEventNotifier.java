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

package org.apache.servicecomb.bizkeeper.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.foundation.common.event.AlarmEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.common.eventbus.Subscribe;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixEventType;

public class TestCircutBreakerEventNotifier {
  private List<AlarmEvent> taskList = null;

  CircutBreakerEventNotifier circutBreakerEventNotifier;

  HystrixCommandKey commandKey = null;

  Object receiveEvent = null;

  public static class EventSubscriber {
    final List<AlarmEvent> taskList;

    public EventSubscriber(List<AlarmEvent> taskList) {
      this.taskList = taskList;
    }

    @Subscribe
    public void onEvent(AlarmEvent circutBreakerEvent) {
      taskList.add(circutBreakerEvent);
    }
  }

  @BeforeEach
  public void setUp() throws Exception {
    taskList = new ArrayList<>();
    circutBreakerEventNotifier = new CircutBreakerEventNotifier();
    commandKey = Mockito.mock(HystrixCommandKey.class);
    receiveEvent = new EventSubscriber(taskList);
    circutBreakerEventNotifier.eventBus.register(receiveEvent);
  }

  @AfterEach
  public void tearDown() throws Exception {
    circutBreakerEventNotifier.eventBus.unregister(receiveEvent);
  }

  @Test
  public void testMarkEvent() {
    Mockito.when(commandKey.name()).thenReturn("Consumer.springmvc.springmvcHello.sayHi");
    try (MockedStatic<HystrixCommandMetrics> mockedStatic = Mockito.mockStatic(HystrixCommandMetrics.class)) {
      mockedStatic.when(() -> HystrixCommandMetrics.getInstance(commandKey)).thenReturn(null);
      circutBreakerEventNotifier.markEvent(HystrixEventType.SHORT_CIRCUITED, commandKey);
      Assertions.assertEquals(1, taskList.size());
      circutBreakerEventNotifier.markEvent(HystrixEventType.SUCCESS, commandKey);
      Assertions.assertEquals(2, taskList.size());
    }
  }
}
