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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * for register/unregister rarely
 */
public class SimpleEventBus extends EventBus {
  private final Map<Object, List<SimpleSubscriber>> subscribersMap = new ConcurrentHashMapEx<>();

  // key is event class
  private Map<Class<?>, List<SimpleSubscriber>> subscribersCache = new ConcurrentHashMapEx<>();

  private List<SimpleSubscriber> collectSubscribers(Object instance) {
    List<SimpleSubscriber> subscribers = new ArrayList<>();
    Method[] methods = MethodUtils.getMethodsWithAnnotation(instance.getClass(), Subscribe.class, true, true);
    for (Method method : methods) {
      SimpleSubscriber subscriber = new SimpleSubscriber(instance, method);
      subscribers.add(subscriber);
    }
    return subscribers;
  }

  @Override
  public void register(Object instance) {
    subscribersMap.computeIfAbsent(instance, this::collectSubscribers);
    // even ignored cause of duplicate register
    // still reset cache
    // this makes logic simpler
    subscribersCache = new ConcurrentHashMapEx<>();
  }

  @Override
  public void unregister(Object instance) {
    if (subscribersMap.remove(instance) != null) {
      subscribersCache = new ConcurrentHashMapEx<>();
    }
  }

  public void post(Object event) {
    // cache always reset after register/unregister
    // so cache always match latest subscribersMap at last
    // the worst scenes is invoke collectSubscriberForEvent multiple times, no problem
    List<SimpleSubscriber> subscribers = subscribersCache
        .computeIfAbsent(event.getClass(), this::collectSubscriberForEvent);
    for (SimpleSubscriber subscriber : subscribers) {
      subscriber.dispatchEvent(event);
    }
  }

  /**
   * subscribersMap almost stable<br>
   * so we not care for performance of collectSubscriberForEvent
   * @param eventClass
   */
  private List<SimpleSubscriber> collectSubscriberForEvent(Class<?> eventClass) {
    List<SimpleSubscriber> subscribersForEvent = new ArrayList<>();
    for (List<SimpleSubscriber> subscribers : subscribersMap.values()) {
      for (SimpleSubscriber subscriber : subscribers) {
        if (subscriber.getMethod().getParameterTypes()[0].isAssignableFrom(eventClass)) {
          subscribersForEvent.add(subscriber);
        }
      }
    }

    subscribersForEvent.sort(Comparator.comparingInt(SimpleSubscriber::getOrder));
    return subscribersForEvent;
  }
}
