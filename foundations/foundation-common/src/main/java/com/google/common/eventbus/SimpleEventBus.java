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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

import com.google.common.collect.Multimap;

/**
 * <p>EventBus has performance problem, we create a new simpler eventBus
 * 
 * <p>extend from EventBus is not necessary<br>
 * but EventManager has a public EventBus field, we need to make compatible
 * 
 * <p>different between this class and EventBus:<br>
 * 1. not support cycle trigger:<br>
 *   subscribe a, when handle a, trigger event b<br>
 *   subscribe b, when handle b, trigger event a
 */
public class SimpleEventBus extends com.google.common.eventbus.EventBus {
  private AnnotatedSubscriberFinder finder = new AnnotatedSubscriberFinder();

  // key is event type
  // value is CopyOnWriteArrayList
  private Map<Class<?>, List<EventSubscriber>> subscribersByType = new ConcurrentHashMapEx<>();

  @Override
  public void register(Object listener) {
    Multimap<Class<?>, EventSubscriber> methodsInListener = finder.findAllSubscribers(listener);

    for (Entry<Class<?>, Collection<EventSubscriber>> entry : methodsInListener.asMap().entrySet()) {
      List<EventSubscriber> exists = subscribersByType.computeIfAbsent(entry.getKey(), cls -> {
        return new CopyOnWriteArrayList<>();
      });
      exists.addAll(entry.getValue());
    }
  }

  @Override
  public void unregister(Object listener) {
    Multimap<Class<?>, EventSubscriber> methodsInListener = finder.findAllSubscribers(listener);

    for (Entry<Class<?>, Collection<EventSubscriber>> entry : methodsInListener.asMap().entrySet()) {
      List<EventSubscriber> exists = subscribersByType.get(entry.getKey());
      if (exists == null) {
        continue;
      }

      exists.removeAll(entry.getValue());
    }
  }

  @Override
  public void post(Object event) {
    Set<Class<?>> dispatchTypes = flattenHierarchy(event.getClass());
    for (Class<?> dispatchType : dispatchTypes) {
      List<EventSubscriber> subscribers = subscribersByType.getOrDefault(dispatchType, Collections.emptyList());

      for (EventSubscriber subscriber : subscribers) {
        dispatch(event, subscriber);
      }
    }
  }
}
