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
import org.apache.servicecomb.metrics.common.MetricsDimension;
import org.apache.servicecomb.metrics.core.utils.MonitorUtils;

import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StepCounter;

public class CallMonitor {
  private final Map<String, Map<String, DimensionCounter>> dimensionCounters;

  private final String operation;

  private final String stage;

  private final String role;

  public CallMonitor(String operation, String stage, String role) {
    this.operation = operation;
    this.stage = stage;
    this.role = role;

    this.dimensionCounters = new ConcurrentHashMapEx<>();
    this.dimensionCounters.put(MetricsDimension.DIMENSION_STATUS, new ConcurrentHashMapEx<>());
  }

  public void increment(String dimensionKey, String... dimensionValues) {
    for (String dimensionValue : dimensionValues) {
      DimensionCounter counter = dimensionCounters.get(dimensionKey)
          .computeIfAbsent(dimensionValue, d -> new DimensionCounter(
              new BasicCounter(MonitorConfig.builder(MetricsConst.SERVICECOMB_INVOCATION)
                  .withTag(dimensionKey, dimensionValue)
                  .withTag(MetricsConst.TAG_OPERATION, operation)
                  .withTag(MetricsConst.TAG_STAGE, stage)
                  .withTag(MetricsConst.TAG_ROLE, role)
                  .withTag(MetricsConst.TAG_STATISTIC, "count")
                  .build()),
              new StepCounter(MonitorConfig.builder(MetricsConst.SERVICECOMB_INVOCATION)
                  .withTag(dimensionKey, dimensionValue)
                  .withTag(MetricsConst.TAG_OPERATION, operation)
                  .withTag(MetricsConst.TAG_STAGE, stage)
                  .withTag(MetricsConst.TAG_ROLE, role)
                  .withTag(MetricsConst.TAG_STATISTIC, "tps")
                  .build())));
      counter.increment();
    }
  }

  public Map<String, Double> toMetric(int windowTimeIndex) {
    Map<String, Double> metrics = new HashMap<>();
    for (Map<String, DimensionCounter> dimensionCounter : dimensionCounters.values()) {
      for (DimensionCounter counter : dimensionCounter.values()) {
        metrics.put(MonitorUtils.getMonitorName(counter.getTotal().getConfig()),
            counter.getTotal().getValue(windowTimeIndex).doubleValue());
        metrics.put(MonitorUtils.getMonitorName(counter.getTps().getConfig()),
            counter.getTps().getValue(windowTimeIndex).doubleValue());
      }
    }
    return metrics;
  }

  class DimensionCounter {
    private final BasicCounter total;

    private final StepCounter tps;

    public BasicCounter getTotal() {
      return total;
    }

    public StepCounter getTps() {
      return tps;
    }

    public DimensionCounter(BasicCounter total, StepCounter tps) {
      this.total = total;
      this.tps = tps;
    }

    public void increment() {
      total.increment();
      tps.increment();
    }
  }
}
