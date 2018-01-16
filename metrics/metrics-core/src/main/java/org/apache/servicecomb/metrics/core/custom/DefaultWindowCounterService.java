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

package org.apache.servicecomb.metrics.core.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.springframework.stereotype.Component;

@Component
public class DefaultWindowCounterService implements WindowCounterService {

  private final Map<String, WindowCounter> counters;

  public DefaultWindowCounterService() {
    this.counters = new ConcurrentHashMapEx<>();
  }

  @Override
  public void record(String name, long value) {
    WindowCounter counter = counters.computeIfAbsent(name, WindowCounter::new);
    counter.update(value);
  }

  public Map<String, Double> toMetrics(int windowTimeIndex) {
    Map<String, Double> metrics = new HashMap<>();
    for (Entry<String, WindowCounter> counter : counters.entrySet()) {
      metrics.putAll(counter.getValue().toMetric(windowTimeIndex));
    }
    return metrics;
  }
}
