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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.metrics.MetricsConst;
import org.apache.servicecomb.metrics.core.utils.MonitorUtils;

import com.netflix.servo.monitor.MaxGauge;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StepCounter;
import com.netflix.servo.tag.Tags;

public class TimerMonitor {
  private final Map<String, StatusCounter> statusCounters;

  private final String operation;

  private final String stage;

  private final String role;

  public TimerMonitor(String operation, String stage, String role) {
    this.operation = operation;
    this.stage = stage;
    this.role = role;

    this.statusCounters = new ConcurrentHashMapEx<>();
  }

  public void update(long value, String statusCode) {
    StatusCounter counter = statusCounters
        .computeIfAbsent(statusCode, d -> new StatusCounter(operation, stage, role, statusCode));
    counter.update(value);
  }

  public Map<String, Double> measure(int windowTimeIndex, boolean calculateLatency) {
    Map<String, Double> measurements = new HashMap<>();
    for (StatusCounter counter : statusCounters.values()) {
      measurements.putAll(counter.measure(windowTimeIndex, calculateLatency));
    }
    return measurements;
  }

  class StatusCounter {
    //nanosecond sum
    private final StepCounter totalTime;

    private final StepCounter count;

    //nanosecond max
    private final MaxGauge max;

    private final MonitorConfig latency;

    public StatusCounter(String operation, String stage, String role, String statusCode) {
      MonitorConfig config = MonitorConfig.builder(MetricsConst.SERVICECOMB_INVOCATION)
          .withTag(MetricsConst.TAG_STATUS, statusCode).withTag(MetricsConst.TAG_OPERATION, operation)
          .withTag(MetricsConst.TAG_STAGE, stage).withTag(MetricsConst.TAG_ROLE, role).build();

      this.latency = config.withAdditionalTag(Tags.newTag(MetricsConst.TAG_STATISTIC, "latency"));
      this.totalTime = new StepCounter(config.withAdditionalTag(Tags.newTag(MetricsConst.TAG_STATISTIC, "totalTime")));
      this.count = new StepCounter(config.withAdditionalTag(Tags.newTag(MetricsConst.TAG_STATISTIC, "count")));
      this.max = new MaxGauge(config.withAdditionalTag(Tags.newTag(MetricsConst.TAG_STATISTIC, "max")));
    }

    public void update(long value) {
      if (value > 0) {
        totalTime.increment(value);
        count.increment();
        max.update(value);
      }
    }

    public Map<String, Double> measure(int windowTimeIndex, boolean calculateLatency) {
      Map<String, Double> measurements = new HashMap<>();
      double totalTime = (double) MonitorUtils.convertNanosecondToMillisecond(
          MonitorUtils.adjustValue(this.totalTime.getCount(windowTimeIndex)));
      double count = (double) MonitorUtils.adjustValue(this.count.getCount(windowTimeIndex));
      measurements.put(MonitorUtils.getMonitorName(this.totalTime.getConfig()), totalTime);
      measurements.put(MonitorUtils.getMonitorName(this.count.getConfig()), count);
      measurements.put(MonitorUtils.getMonitorName(this.max.getConfig()), (double) MonitorUtils
          .convertNanosecondToMillisecond(MonitorUtils.adjustValue(this.max.getValue(windowTimeIndex))));
      if (calculateLatency) {
        measurements.put(MonitorUtils.getMonitorName(latency), totalTime / count);
      }
      return measurements;
    }
  }
}
