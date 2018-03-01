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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestEventBus {

  private AtomicBoolean eventReceived = new AtomicBoolean(false);

  private EventListener<String> listener = new EventListener<String>() {
    @Override
    public Class<String> getEventClass() {
      return String.class;
    }

    @Override
    public void process(String data) {
      eventReceived.set(true);
    }
  };

  @Before
  public void reset() {
    EventBus.getInstance().unregisterEventListener(listener);
  }

  @Test
  public void checkEventReceivedAndProcessed() {
    EventBus.getInstance().registerEventListener(listener);

    EventBus.getInstance().triggerEvent("xxx");
    await().atMost(1, TimeUnit.SECONDS)
        .until(eventReceived::get);
    Assert.assertTrue(eventReceived.get());
  }

  @Test
  public void checkEventCanNotReceivedAfterUnregister() {
    EventBus.getInstance().registerEventListener(listener);

    EventBus.getInstance().triggerEvent("xxx");
    await().atMost(1, TimeUnit.SECONDS)
        .until(eventReceived::get);
    Assert.assertTrue(eventReceived.get());

    eventReceived.set(false);

    EventBus.getInstance().unregisterEventListener(listener);
    EventBus.getInstance().triggerEvent("xxx");
    Assert.assertFalse(eventReceived.get());
  }

  @Test
  public void checkUnmatchTypeWillNotReceived() {
    EventBus.getInstance().registerEventListener(listener);

    //trigger a Integer type event object
    EventBus.getInstance().triggerEvent(1);
    Assert.assertFalse(eventReceived.get());
  }
}
