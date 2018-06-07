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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.eventbus.Subscribe;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixEventType;

import mockit.Expectations;

public class TestCircutBreakerEventNotifier {
  private List<AlarmEvent> taskList = null;

  CircutBreakerEventNotifier circutBreakerEventNotifier;

  HystrixCommandKey commandKey = null;

  Object receiveEvent = null;

  @Before
  public void setUp() throws Exception {
    taskList = new ArrayList<>();
    circutBreakerEventNotifier = new CircutBreakerEventNotifier();
    commandKey = Mockito.mock(HystrixCommandKey.class);
    receiveEvent = new Object() {
      @Subscribe
      public void onEvent(AlarmEvent circutBreakerEvent) {
        taskList.add(circutBreakerEvent);
      }
    };
    circutBreakerEventNotifier.eventBus.register(receiveEvent);
  }

  @After
  public void tearDown() throws Exception {
    circutBreakerEventNotifier.eventBus.unregister(receiveEvent);
  }

  @Test
  public void testMarkEvent() {
    Mockito.when(commandKey.name()).thenReturn("Consumer.springmvc.springmvcHello.sayHi");
    new Expectations(HystrixCommandMetrics.class) {
      {
        HystrixCommandMetrics.getInstance(commandKey);
        result = null;
      }
    };
    circutBreakerEventNotifier.markEvent(HystrixEventType.SHORT_CIRCUITED, commandKey);
    Assert.assertEquals(1, taskList.size());
    circutBreakerEventNotifier.markEvent(HystrixEventType.SUCCESS, commandKey);
    Assert.assertEquals(2, taskList.size());
  }
}
