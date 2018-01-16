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

import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.MonitorConfig;

@Component
public class DefaultCounterService implements CounterService {

  private final Map<String, BasicCounter> counters;

  public DefaultCounterService() {
    this.counters = new ConcurrentHashMapEx<>();
  }

  @Override
  public void increment(String name) {
    getCounter(name).increment();
  }

  @Override
  public void increment(String name, long value) {
    getCounter(name).increment(value);
  }

  @Override
  public void decrement(String name) {
    getCounter(name).increment(-1);
  }

  @Override
  public void reset(String name) {
    counters.remove(name);
    this.increment(name, 0);
  }

  private BasicCounter getCounter(String name) {
    return counters.computeIfAbsent(name, n -> new BasicCounter(MonitorConfig.builder(n).build()));
  }

  public Map<String, Double> toMetrics() {
    Map<String, Double> metrics = new HashMap<>();
    for (Entry<String, BasicCounter> counter : counters.entrySet()) {
      metrics.put(counter.getKey(), counter.getValue().getValue().doubleValue());
    }
    return metrics;
  }
}
