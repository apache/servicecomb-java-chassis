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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetricsEventManager {
  private static final Map<Class<? extends MetricsEvent>, List<MetricsEventListener>> allEvents = new ConcurrentHashMap<>();

  public static void registerEventListener(MetricsEventListener eventListener) {
    allEvents.putIfAbsent(eventListener.getConcernedEvent(), new ArrayList<>());
    allEvents.get(eventListener.getConcernedEvent()).add(eventListener);
  }

  public static void triggerEvent(MetricsEvent event) {
    List<MetricsEventListener> eventListeners = allEvents.getOrDefault(event.getClass(), null);
    if (eventListeners != null) {
      for (MetricsEventListener eventListener : eventListeners) {
        eventListener.process(event);
      }
    }
  }
}
