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
import org.apache.servicecomb.metrics.common.MetricsConst;
import org.apache.servicecomb.metrics.core.utils.MonitorUtils;

import com.netflix.servo.monitor.MaxGauge;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StepCounter;

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
        .computeIfAbsent(statusCode, d -> new StatusCounter(
            new StepCounter(MonitorConfig.builder(MetricsConst.SERVICECOMB_INVOCATION)
                .withTag(MetricsConst.TAG_STATUS, statusCode)
                .withTag(MetricsConst.TAG_OPERATION, operation)
                .withTag(MetricsConst.TAG_STAGE, stage)
                .withTag(MetricsConst.TAG_ROLE, role)
                .withTag(MetricsConst.TAG_STATISTIC, "totalTime")
                .build()),
            new StepCounter(MonitorConfig.builder(MetricsConst.SERVICECOMB_INVOCATION)
                .withTag(MetricsConst.TAG_STATUS, statusCode)
                .withTag(MetricsConst.TAG_OPERATION, operation)
                .withTag(MetricsConst.TAG_STAGE, stage)
                .withTag(MetricsConst.TAG_ROLE, role)
                .withTag(MetricsConst.TAG_STATISTIC, "count")
                .build()),
            new MaxGauge(MonitorConfig.builder(MetricsConst.SERVICECOMB_INVOCATION)
                .withTag(MetricsConst.TAG_STATUS, statusCode)
                .withTag(MetricsConst.TAG_OPERATION, operation)
                .withTag(MetricsConst.TAG_STAGE, stage)
                .withTag(MetricsConst.TAG_ROLE, role)
                .withTag(MetricsConst.TAG_STATISTIC, "max")
                .build()),
            MonitorConfig.builder(MetricsConst.SERVICECOMB_INVOCATION)
                .withTag(MetricsConst.TAG_STATUS, statusCode)
                .withTag(MetricsConst.TAG_OPERATION, operation)
                .withTag(MetricsConst.TAG_STAGE, stage)
                .withTag(MetricsConst.TAG_ROLE, role)
                .withTag(MetricsConst.TAG_STATISTIC, "latency")
                .build()));
    counter.update(value);
  }

  public Map<String, Double> toMetric(int windowTimeIndex, boolean calculateLatency) {
    Map<String, Double> metrics = new HashMap<>();
    for (StatusCounter counter : statusCounters.values()) {
      double total = (double) MonitorUtils.convertNanosecondToMillisecond(
          MonitorUtils.adjustValue(counter.getTotal().getCount(windowTimeIndex)));
      double count = (double) MonitorUtils.adjustValue(counter.getCount().getCount(windowTimeIndex));
      metrics.put(MonitorUtils.getMonitorName(counter.getTotal().getConfig()), total);
      metrics.put(MonitorUtils.getMonitorName(counter.getCount().getConfig()), count);
      metrics.put(MonitorUtils.getMonitorName(counter.getMax().getConfig()),
          (double) MonitorUtils
              .convertNanosecondToMillisecond(
                  MonitorUtils.adjustValue(counter.getMax().getValue(windowTimeIndex))));
      if (calculateLatency) {
        metrics.put(MonitorUtils.getMonitorName(counter.getLatency()), total / count);
      }
    }
    return metrics;
  }

  class StatusCounter {
    //nanosecond sum
    private final StepCounter total;

    private final StepCounter count;

    //nanosecond max
    private final MaxGauge max;

    private final MonitorConfig latency;

    public StepCounter getTotal() {
      return total;
    }

    public StepCounter getCount() {
      return count;
    }

    public MaxGauge getMax() {
      return max;
    }

    public MonitorConfig getLatency() {
      return latency;
    }

    public StatusCounter(StepCounter total, StepCounter count, MaxGauge max, MonitorConfig latency) {
      this.total = total;
      this.count = count;
      this.max = max;
      this.latency = latency;
    }

    public void update(long value) {
      if (value > 0) {
        total.increment(value);
        count.increment();
        max.update(value);
      }
    }
  }
}
