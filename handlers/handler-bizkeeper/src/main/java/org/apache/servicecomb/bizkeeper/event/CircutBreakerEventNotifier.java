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

import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.event.AlarmEvent.Type;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixEventType;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;

public class CircutBreakerEventNotifier extends HystrixEventNotifier {

  /**
   * 使用circuitMarker来记录被熔断的接口
   */
  private static ConcurrentHashMap<String, Boolean> circuitMarker = new ConcurrentHashMap<>();

  @Override
  public void markEvent(HystrixEventType eventType, HystrixCommandKey key) {
    String keyName = key.name();
    switch (eventType) {
      case SHORT_CIRCUITED:
        if (circuitMarker.get(keyName) == null) {
          EventManager.post(new CircutBreakerEvent(key, Type.OPEN));
          circuitMarker.put(keyName, true);
        }
        break;

      case SUCCESS:
        if (circuitMarker.get(keyName) != null) {
          EventManager.post(new CircutBreakerEvent(key, Type.CLOSE));
          circuitMarker.remove(keyName);
        }
        break;

      default:
        break;
    }
  }
}
