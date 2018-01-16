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

import com.netflix.servo.monitor.MaxGauge;
import com.netflix.servo.monitor.MinGauge;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StepCounter;

public class WindowCounter {
  private final String name;

  private final StepCounter total;

  private final StepCounter count;

  private final MinGauge min;

  private final MaxGauge max;

  public WindowCounter(String name) {
    this.name = name;
    total = new StepCounter(MonitorConfig.builder(name).build());
    count = new StepCounter(MonitorConfig.builder(name).build());
    min = new MinGauge(MonitorConfig.builder(name).build());
    max = new MaxGauge(MonitorConfig.builder(name).build());
  }

  public void update(long value) {
    if (value > 0) {
      total.increment(value);
      count.increment();
      max.update(value);
      min.update(value);
    }
  }

  public Map<String, Double> toMetric(int windowTimeIndex) {
    Map<String, Double> metrics = new HashMap<>();
    metrics.put(name + ".total", this.adjustValue(total.getCount(windowTimeIndex)));
    metrics.put(name + ".count", this.adjustValue(count.getCount(windowTimeIndex)));
    metrics.put(name + ".max", this.adjustValue(max.getValue(windowTimeIndex)));
    metrics.put(name + ".min", this.adjustValue(min.getValue(windowTimeIndex)));
    double value = count.getCount(windowTimeIndex) == 0 ? 0 :
        (double) this.total.getCount(windowTimeIndex) / (double) this.count.getCount(windowTimeIndex);
    metrics.put(name + ".average", value);
    metrics.put(name + ".rate", this.adjustValue(total.getValue(windowTimeIndex).doubleValue()));
    metrics.put(name + ".tps", this.adjustValue(count.getValue(windowTimeIndex).doubleValue()));
    return metrics;
  }

  //for time-related monitor type, if stop poll value over one window time,
  //the value may return -1 because servo can't known precise value of previous step
  //so must change to return 0
  private double adjustValue(double value) {
    return value < 0 ? 0 : value;
  }
}
