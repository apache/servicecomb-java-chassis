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
package com.google.common.eventbus;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class TestEventBus {
  private EventBus eventBus = new EventBus();

  private List<Object> events = new ArrayList<>();

  class SubscriberForTest {
    @Subscribe
    @AllowConcurrentEvents
    public void s1(Integer event) {
      events.add(event);
    }

    @Subscribe
    public void s2(String event) {
      events.add(event);
    }
  }

  @Test
  public void unregister() {
    Object obj = new SubscriberForTest();

    eventBus.register(obj);
    eventBus.unregister(obj);
  }

  @Test
  public void oneSubscriber() {
    Object obj = new SubscriberForTest();

    eventBus.register(obj);

    eventBus.post(0.1);
    eventBus.post(1);
    eventBus.post("str");
    Assert.assertThat(events, Matchers.contains(1, "str"));

    eventBus.unregister(obj);

    events.clear();
    eventBus.post(0.1);
    eventBus.post(1);
    eventBus.post("str");
    Assert.assertThat(events, Matchers.empty());
  }

  @Test
  public void twoSubscriber() {
    Object obj1 = new SubscriberForTest();
    Object obj2 = new SubscriberForTest();

    eventBus.register(obj1);
    eventBus.register(obj2);

    eventBus.post(0.1);
    eventBus.post(1);
    eventBus.post("str");
    Assert.assertThat(events, Matchers.contains(1, 1, "str", "str"));

    events.clear();
    eventBus.unregister(obj1);
    eventBus.post(0.1);
    eventBus.post(1);
    eventBus.post("str");
    Assert.assertThat(events, Matchers.contains(1, "str"));
  }
}
