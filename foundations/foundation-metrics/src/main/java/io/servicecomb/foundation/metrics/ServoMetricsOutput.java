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

package io.servicecomb.foundation.metrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.servo.Metric;
import com.netflix.servo.publish.MemoryMetricObserver;

public class ServoMetricsOutput implements MetricsOutput {

  private static final Logger log = LoggerFactory.getLogger(ServoMetricsOutput.class);

  private final MemoryMetricObserver observer;

  public ServoMetricsOutput(MemoryMetricObserver observer) {
    this.observer = observer;
  }

  @Override
  public Map<String, String> getMetricsData() {
    Map<String, String> data = new HashMap<>();
    if (observer != null) {
      List<List<Metric>> metrics = observer.getObservations();
      if (!metrics.isEmpty()) {
        for (Metric metric : metrics.get(0)) {
          String key = metric.getConfig().getName() +
              (metric.getConfig().getTags().containsKey("statistic") ? "." + metric.getConfig().getTags()
                  .getValue("statistic") : "");
          data.put(key, metric.getValue().toString());
        }
      }
    }
    return data;
  }
}
