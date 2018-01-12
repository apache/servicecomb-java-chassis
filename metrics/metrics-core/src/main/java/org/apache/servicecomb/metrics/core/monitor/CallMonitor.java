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

import java.util.ArrayList;
import java.util.List;

import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StepCounter;

import io.servicecomb.metrics.common.CallMetric;
import io.servicecomb.metrics.common.DoubleMetricValue;
import io.servicecomb.metrics.common.LongMetricValue;
import io.servicecomb.metrics.common.MetricsDimension;
import io.servicecomb.metrics.core.utils.MonitorUtils;

public class CallMonitor {
  private final String prefix;

  private final List<BasicCounter> totalCounters;

  private final List<StepCounter> tpsCounters;

  public CallMonitor(String prefix, String... dimensionKeys) {
    this.prefix = prefix;
    this.totalCounters = new ArrayList<>();
    this.tpsCounters = new ArrayList<>();
    if (dimensionKeys.length == 0) {
      this.totalCounters.add(new BasicCounter(MonitorConfig.builder(prefix + ".total").build()));
      this.tpsCounters.add(new StepCounter(MonitorConfig.builder(prefix + ".tps").build()));
    } else {
      for (String dimensionKey : dimensionKeys) {
        for (String option : MetricsDimension.getDimensionOptions(dimensionKey)) {
          this.totalCounters
              .add(new BasicCounter(MonitorConfig.builder(prefix + ".total").withTag(dimensionKey, option).build()));
          this.tpsCounters
              .add(new StepCounter(MonitorConfig.builder(prefix + ".tps").withTag(dimensionKey, option).build()));
        }
      }
    }
  }

  public void increment() {
    for (int i = 0; i < totalCounters.size(); i++) {
      totalCounters.get(i).increment();
      tpsCounters.get(i).increment();
    }
  }

  public void increment(String dimensionKey, String dimensionValue) {
    for (int i = 0; i < totalCounters.size(); i++) {
      BasicCounter totalCounter = totalCounters.get(i);
      if (MonitorUtils.containsTagValue(totalCounter, dimensionKey, dimensionValue)) {
        totalCounter.increment();
      }
      StepCounter tpsCounter = tpsCounters.get(i);
      if (MonitorUtils.containsTagValue(tpsCounter, dimensionKey, dimensionValue)) {
        tpsCounter.increment();
      }
    }
  }

  public CallMetric toMetric(int windowTimeIndex) {
    List<LongMetricValue> totalValues = new ArrayList<>();
    List<DoubleMetricValue> tpsValues = new ArrayList<>();
    for (int i = 0; i < totalCounters.size(); i++) {
      BasicCounter totalCounter = totalCounters.get(i);
      totalValues.add(new LongMetricValue(totalCounter.getValue(windowTimeIndex).longValue(),
          MonitorUtils.convertTags(totalCounter)));
      StepCounter tpsCounter = tpsCounters.get(i);
      tpsValues.add(
          new DoubleMetricValue(MonitorUtils.adjustValue(tpsCounter.getValue(windowTimeIndex).doubleValue()),
              MonitorUtils.convertTags(tpsCounter)));
    }
    return new CallMetric(this.prefix, totalValues, tpsValues);
  }
}
