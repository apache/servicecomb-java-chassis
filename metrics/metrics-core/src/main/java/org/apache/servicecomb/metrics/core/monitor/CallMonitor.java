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

import org.apache.servicecomb.metrics.common.CallMetric;

import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StepCounter;

public class CallMonitor {
  private final String prefix;

  private final BasicCounter total;

  private final StepCounter tps;

  public CallMonitor(String prefix) {
    this.prefix = prefix;
    this.total = new BasicCounter(MonitorConfig.builder(prefix + ".total").build());
    this.tps = new StepCounter(MonitorConfig.builder(prefix + ".tps").build());
  }

  public void increment() {
    total.increment();
    tps.increment();
  }

  public CallMetric toMetric(int windowTimeIndex) {
    return new CallMetric(this.prefix, total.getValue(windowTimeIndex).longValue(),
        this.adjustValue(tps.getValue(windowTimeIndex).doubleValue()));
  }

  //for time-related monitor type, if stop poll value over one window time,
  //the value may return -1 because servo can't known precise value of previous step
  //so must change to return 0
  public double adjustValue(double value) {
    return value < 0 ? 0 : value;
  }
}
