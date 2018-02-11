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

import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StepCounter;
import com.netflix.servo.tag.Tags;

public class CallMonitor {
  private final Map<String, StatusCounter> statusCounters;

  private final String operation;

  private final String stage;

  private final String role;

  public CallMonitor(String operation, String stage, String role) {
    this.operation = operation;
    this.stage = stage;
    this.role = role;

    this.statusCounters = new ConcurrentHashMapEx<>();
  }

  public void increment(String statusCode) {
    StatusCounter counter = statusCounters
        .computeIfAbsent(statusCode, d -> new StatusCounter(operation, stage, role, statusCode));
    counter.increment();
  }

  public Map<String, Double> measure(int windowTimeIndex) {
    Map<String, Double> metrics = new HashMap<>();
    for (StatusCounter counter : statusCounters.values()) {
      metrics.putAll(counter.measure(windowTimeIndex));
    }
    return metrics;
  }

  class StatusCounter {
    private final BasicCounter totalCount;

    private final StepCounter tps;

    public StatusCounter(String operation, String stage, String role, String statusCode) {
      MonitorConfig config = MonitorConfig.builder(MetricsConst.SERVICECOMB_INVOCATION)
          .withTag(MetricsConst.TAG_STATUS, statusCode).withTag(MetricsConst.TAG_OPERATION, operation)
          .withTag(MetricsConst.TAG_STAGE, stage).withTag(MetricsConst.TAG_ROLE, role).build();

      this.totalCount = new BasicCounter(
          config.withAdditionalTag(Tags.newTag(MetricsConst.TAG_STATISTIC, "totalCount")));
      this.tps = new StepCounter(config.withAdditionalTag(Tags.newTag(MetricsConst.TAG_STATISTIC, "tps")));
    }

    public void increment() {
      totalCount.increment();
      tps.increment();
    }

    public Map<String, Double> measure(int windowTimeIndex) {
      Map<String, Double> measurements = new HashMap<>();
      measurements.put(MonitorUtils.getMonitorName(this.totalCount.getConfig()),
          this.totalCount.getValue(windowTimeIndex).doubleValue());
      measurements.put(MonitorUtils.getMonitorName(this.tps.getConfig()),
          this.tps.getValue(windowTimeIndex).doubleValue());
      return measurements;
    }
  }
}
