/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.metrics.event;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MetricsEventManager {
  private static final Map<Class<? extends MetricsEvent>, List<MetricsEventListener>> allEvents = new ConcurrentHashMap<>();

  public static void registerEventListener(MetricsEventListener eventListener) {
    List<MetricsEventListener> eventListeners = allEvents
        .computeIfAbsent(eventListener.getConcernedEvent(), f -> new CopyOnWriteArrayList<>());
    eventListeners.add(eventListener);
  }

  public static void triggerEvent(MetricsEvent event) {
    List<MetricsEventListener> eventListeners = allEvents.getOrDefault(event.getClass(), Collections.emptyList());
    for (MetricsEventListener eventListener : eventListeners) {
      eventListener.process(event);
    }
  }
}
