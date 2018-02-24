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

package org.apache.servicecomb.metrics.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.servicecomb.foundation.metrics.MetricsConst;

import com.netflix.servo.monitor.Counter;
import com.netflix.servo.monitor.MaxGauge;
import com.netflix.servo.monitor.StepCounter;
import com.netflix.servo.monitor.Timer;

abstract class AbstractInvocationMetrics {
  private final Counter tps;

  private final Counter count;

  private final Map<String, Timer> averageLatencies;

  private final Map<String, MaxGauge> maxLatencies;

  AbstractInvocationMetrics(String... tags) {
    String[] tagsWithStage = ArrayUtils.addAll(tags, MetricsConst.TAG_STAGE, MetricsConst.STAGE_TOTAL);
    this.tps = MonitorManager.getInstance().getCounter(StepCounter::new, MetricsConst.SERVICECOMB_INVOCATION,
        ArrayUtils.addAll(tagsWithStage, MetricsConst.TAG_STATISTIC, "tps"));
    this.count = MonitorManager.getInstance().getCounter(MetricsConst.SERVICECOMB_INVOCATION,
        ArrayUtils.addAll(tagsWithStage, MetricsConst.TAG_STATISTIC, "count"));

    this.averageLatencies = new HashMap<>();
    this.maxLatencies = new HashMap<>();
    this.addLatencyMonitors(MetricsConst.STAGE_TOTAL, tags);
  }

  void updateCallMonitors() {
    tps.increment();
    count.increment();
  }

  void updateLatencyMonitors(String stage, long value, TimeUnit unit) {
    averageLatencies.get(stage).record(value, unit);
    maxLatencies.get(stage).update(unit.toMillis(value));
  }

  void addLatencyMonitors(String stage, String... tags) {
    String[] tagsWithStageAndUnit = ArrayUtils
        .addAll(tags, MetricsConst.TAG_STAGE, stage, MetricsConst.TAG_UNIT, String.valueOf(TimeUnit.MILLISECONDS));
    this.averageLatencies.put(stage, MonitorManager.getInstance()
        .getTimer(MetricsConst.SERVICECOMB_INVOCATION,
            ArrayUtils.addAll(tagsWithStageAndUnit, MetricsConst.TAG_STATISTIC, "latency")));
    this.maxLatencies.put(stage, MonitorManager.getInstance()
        .getMaxGauge(MetricsConst.SERVICECOMB_INVOCATION,
            ArrayUtils.addAll(tagsWithStageAndUnit, MetricsConst.TAG_STATISTIC, "max")));
  }
}
