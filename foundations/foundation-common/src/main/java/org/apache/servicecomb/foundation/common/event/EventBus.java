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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBus {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventBus.class);

  //key is event object type,fast locate listener and fire
  @SuppressWarnings("rawtypes")
  private final Map<Class, List<EventListener>> allEventListeners = new ConcurrentHashMapEx<>();

  private static final EventBus INSTANCE = new EventBus();

  public static EventBus getInstance() {
    return INSTANCE;
  }

  //event class will get from getEventClass method
  @SuppressWarnings({"rawtypes"})
  private EventBus() {
    List<EventListener> listeners = SPIServiceUtils.getAllService(EventListener.class);
    for (EventListener listener : listeners) {
      this.registerEventListener(listener);
      LOGGER.info("EventBus register " + listener.getClass().getName()
          + " for process " + listener.getEventClass().getName());
    }
  }

  //event class will get from getEventClass method
  @SuppressWarnings("rawtypes")
  public void registerEventListener(EventListener eventListener) {
    List<EventListener> eventListeners = allEventListeners
        .computeIfAbsent(eventListener.getEventClass(), f -> new CopyOnWriteArrayList<>());
    eventListeners.add(eventListener);
  }

  //event class will get from getEventClass method
  @SuppressWarnings("rawtypes")
  public void unregisterEventListener(EventListener eventListener) {
    List<EventListener> eventListeners = allEventListeners
        .computeIfAbsent(eventListener.getEventClass(), f -> new CopyOnWriteArrayList<>());
    if (eventListeners.contains(eventListener)) {
      eventListeners.remove(eventListener);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public void triggerEvent(Object event) {
    List<EventListener> eventListeners = allEventListeners.getOrDefault(event.getClass(), Collections.emptyList());
    for (EventListener eventListener : eventListeners) {
      eventListener.process(event);
    }
  }
}
