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

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.junit.jupiter.api.Test;

public class TestEventBus {
  private final EventBus eventBus = new SimpleEventBus();

  private final List<Object> events = new ArrayList<>();

  public static class SubscriberForTest {
    private final List<Object> events;

    public SubscriberForTest(List<Object> events) {
      this.events = events;
    }
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

  public static class SubscriberWithOrderForTest {
    private final List<Object> events;

    public SubscriberWithOrderForTest(List<Object> events) {
      this.events = events;
    }

    @Subscribe
    @SubscriberOrder(100)
    public void s1(String event) {
      events.add("s1:" + event);
    }

    @Subscribe
    @SubscriberOrder(-100)
    public void s2(String event) {
      events.add("s2:" + event);
    }
  }

  @Test
  public void order() {
    eventBus.register(new SubscriberWithOrderForTest(events));

    eventBus.post("value");
    MatcherAssert.assertThat(events, Matchers.contains("s2:value", "s1:value"));
  }

  @Test
  public void unregister() {
    Object obj = new SubscriberForTest(events);

    eventBus.register(obj);
    eventBus.unregister(obj);
  }

  @Test
  public void oneSubscriber() {
    Object obj = new SubscriberForTest(events);

    eventBus.register(obj);

    eventBus.post(0.1);
    eventBus.post(1);
    eventBus.post("str");
    MatcherAssert.assertThat(events, Matchers.contains(1, "str"));

    eventBus.unregister(obj);

    events.clear();
    eventBus.post(0.1);
    eventBus.post(1);
    eventBus.post("str");
    MatcherAssert.assertThat(events, Matchers.empty());
  }

  @Test
  public void twoSubscriber() {
    Object obj1 = new SubscriberForTest(events);
    Object obj2 = new SubscriberForTest(events);

    eventBus.register(obj1);
    eventBus.register(obj2);

    eventBus.post(0.1);
    eventBus.post(1);
    eventBus.post("str");
    MatcherAssert.assertThat(events, Matchers.contains(1, 1, "str", "str"));

    events.clear();
    eventBus.unregister(obj1);
    eventBus.post(0.1);
    eventBus.post(1);
    eventBus.post("str");
    MatcherAssert.assertThat(events, Matchers.contains(1, "str"));
  }
}
