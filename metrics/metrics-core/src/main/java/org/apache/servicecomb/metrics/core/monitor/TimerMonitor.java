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

package org.apache.servicecomb.metrics.core.monitor;

import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.metrics.common.TimerMetric;

import com.netflix.servo.monitor.MaxGauge;
import com.netflix.servo.monitor.MinGauge;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StepCounter;

public class TimerMonitor {
  private final String prefix;

  //nanosecond sum
  private final StepCounter total;

  private final StepCounter count;

  //nanosecond min
  private final MinGauge min;

  //nanosecond max
  private final MaxGauge max;

  public void update(long value) {
    if (value > 0) {
      total.increment(value);
      count.increment();
      max.update(value);
      min.update(value);
    }
  }

  public TimerMonitor(String prefix) {
    this.prefix = prefix;
    total = new StepCounter(MonitorConfig.builder(prefix + ".total").build());
    count = new StepCounter(MonitorConfig.builder(prefix + ".count").build());
    min = new MinGauge(MonitorConfig.builder(prefix + ".min").build());
    max = new MaxGauge(MonitorConfig.builder(prefix + ".max").build());
  }

  public TimerMetric toMetric(int windowTimeIndex) {
    return new TimerMetric(this.prefix,
        this.convertNanosecondToMillisecond(this.adjustValue(total.getCount(windowTimeIndex))),
        this.adjustValue(count.getCount(windowTimeIndex)),
        this.convertNanosecondToMillisecond(this.adjustValue(min.getValue(windowTimeIndex))),
        this.convertNanosecondToMillisecond(this.adjustValue(max.getValue(windowTimeIndex))));
  }

  //for time-related monitor type, if stop poll value over one window time,
  //the value may return -1 because servo can't known precise value of previous step
  //so must change to return 0
  public long adjustValue(long value) {
    return value < 0 ? 0 : value;
  }

  //Counting use System.nano get more precise time
  //so we need change unit to millisecond when ouput
  public long convertNanosecondToMillisecond(long nanoValue) {
    return TimeUnit.NANOSECONDS.toMillis(nanoValue);
  }
}
