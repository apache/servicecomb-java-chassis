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

package io.servicecomb.metrics.core.monitor;

import com.netflix.servo.monitor.MaxGauge;
import com.netflix.servo.monitor.MinGauge;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StepCounter;

import io.servicecomb.metrics.core.metric.TimerMetric;

public class TimerMonitor {
  private final StepCounter total;

  private final StepCounter count;

  private final MinGauge min;

  private final MaxGauge max;

  public void update(long value) {
    if (value > 0) {
      total.increment(value);
      count.increment();
      max.update(value);
      min.update(value);
    }
  }

  public TimerMonitor(String name) {
    total = new StepCounter(MonitorConfig.builder(name + ".total").build());
    count = new StepCounter(MonitorConfig.builder(name + ".count").build());
    min = new MinGauge(MonitorConfig.builder(name + ".min").build());
    max = new MaxGauge(MonitorConfig.builder(name + ".max").build());
  }

  public TimerMetric toTimerMetric(int pollerIndex) {
    return new TimerMetric(total.getCount(pollerIndex), count.getCount(pollerIndex),
        min.getValue(pollerIndex), max.getValue(pollerIndex));
  }
}
