/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.metrics.output.servo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.netflix.servo.Metric;
import com.netflix.servo.publish.BaseMetricObserver;
import com.netflix.servo.util.Preconditions;

import io.servicecomb.foundation.metrics.MetricsServoRegistry;
import io.servicecomb.foundation.metrics.output.MetricsFileOutput;

public class SeparatedMetricObserver extends BaseMetricObserver {
  private final MetricsServoRegistry metricsRegistry;
  private final MetricsFileOutput metricsOutput;

  public SeparatedMetricObserver(String observerName, MetricsFileOutput metricsOutput,
      MetricsServoRegistry metricsRegistry) {
    super(observerName);
    this.metricsRegistry = metricsRegistry;
    this.metricsOutput = metricsOutput;
  }

  @Override
  public void updateImpl(List<Metric> metrics) {
    Preconditions.checkNotNull(metrics, "metrics");
    //这些参数是一次一起计算的，所以不需要将它们转化为独立的Metric，直接取值输出
    Map<String, String> queueMetrics = metricsRegistry.calculateQueueMetrics();
    Map<String, String> systemMetrics = metricsRegistry.getSystemMetrics();
    Map<String, String> tpsAndLatencyMetrics = metricsRegistry.calculateTPSAndLatencyMetrics();

    Map<String, String> totalMetrics = new HashMap<>();
    totalMetrics.putAll(queueMetrics);
    totalMetrics.putAll(systemMetrics);
    totalMetrics.putAll(tpsAndLatencyMetrics);
    for (Metric metric : metrics) {
      totalMetrics.put(metric.getConfig().getName(), metric.getValue().toString());
    }

    metricsOutput.output(totalMetrics);
  }
}


