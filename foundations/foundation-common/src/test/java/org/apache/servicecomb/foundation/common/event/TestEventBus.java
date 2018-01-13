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

package org.apache.servicecomb.foundation.common.event;

import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.servicecomb.foundation.common.utils.EventUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestEventBus {

  @Test
  public void test() throws InterruptedException {
    AtomicBoolean eventReceived = new AtomicBoolean(false);

    EventListener listener = new EventListener() {
      @Override
      public Class<? extends Event> getConcernedEvent() {
        return TestEvent.class;
      }

      @Override
      public void process(Event data) {
        eventReceived.set(true);
      }
    };

    EventUtils.registerEventListener(listener);
    EventUtils.triggerEvent(new TestEvent());
    await().atMost(1, TimeUnit.SECONDS)
        .until(eventReceived::get);
    Assert.assertTrue(eventReceived.get());

    eventReceived.set(false);

    EventUtils.unregisterEventListener(listener);
    EventUtils.triggerEvent(new TestEvent());
    Thread.sleep(1000);
    Assert.assertFalse(eventReceived.get());
  }

  private class TestEvent implements Event {
  }
}
