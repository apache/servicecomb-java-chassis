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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.metrics.common.CallMetric;
import org.apache.servicecomb.metrics.common.DoubleMetricValue;
import org.apache.servicecomb.metrics.common.LongMetricValue;
import org.apache.servicecomb.metrics.common.MetricsDimension;
import org.apache.servicecomb.metrics.core.utils.MonitorUtils;

import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StepCounter;

public class CallMonitor {
  private final String prefix;

  private final Map<String, Map<String, DimensionCounter>> dimensionCounters;

  public CallMonitor(String prefix) {
    this.prefix = prefix;
    this.dimensionCounters = new ConcurrentHashMapEx<>();
    this.dimensionCounters.put(MetricsDimension.DIMENSION_STATUS, new ConcurrentHashMapEx<>());
  }

  public void increment(String dimensionKey, String... dimensionValues) {
    for (String dimensionValue : dimensionValues) {
      DimensionCounter counter = dimensionCounters.get(dimensionKey)
          .computeIfAbsent(dimensionValue, d -> new DimensionCounter(
              new BasicCounter(MonitorConfig.builder(prefix + ".total").withTag(dimensionKey, dimensionValue).build()),
              new StepCounter(MonitorConfig.builder(prefix + ".tps").withTag(dimensionKey, dimensionValue).build())));
      counter.increment();
    }
  }

  public CallMetric toMetric(int windowTimeIndex) {
    List<LongMetricValue> totalValues = new ArrayList<>();
    List<DoubleMetricValue> tpsValues = new ArrayList<>();
    for (Map<String, DimensionCounter> dimensionCounter : dimensionCounters.values()) {
      for (DimensionCounter counter : dimensionCounter.values()) {
        totalValues.add(new LongMetricValue(counter.getTotal().getValue(windowTimeIndex).longValue(),
            MonitorUtils.convertTags(counter.getTotal())));
        tpsValues.add(
            new DoubleMetricValue(MonitorUtils.adjustValue(counter.getTps().getValue(windowTimeIndex).doubleValue()),
                MonitorUtils.convertTags(counter.getTps())));
      }
    }

    return new CallMetric(this.prefix, totalValues, tpsValues);
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
