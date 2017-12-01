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

package io.servicecomb.metrics.core.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.servicecomb.metrics.core.EmbeddedMetricsName;
import io.servicecomb.metrics.core.registry.MetricsRegistry;

public class DefaultMetricsPublisher implements MetricsPublisher {

  private final MetricsRegistry registry;

  public DefaultMetricsPublisher(MetricsRegistry registry) {
    this.registry = registry;
  }

  @Override
  public Map<String, Number> metrics() {
    Map<String, Number> output = formatMetricsName(registry.getAllMetricsValue());
    removeUselessMetrics(output);
    return output;
  }

  private Map<String, Number> formatMetricsName(Map<String, Number> input) {
    Map<String, Number> output = new HashMap<>();

    for (Entry<String, Number> entry : input.entrySet()) {
      String name = entry.getKey();
      if (entry.getKey().startsWith(EmbeddedMetricsName.INSTANCE_QUEUE_EXECUTIONTIME)) {
        String newName = EmbeddedMetricsName.INSTANCE_QUEUE + entry.getKey().substring(entry.getKey().lastIndexOf("."))
            + "ExecutionTime";
      } else if (entry.getKey().startsWith(EmbeddedMetricsName.INSTANCE_QUEUE_LIFETIMEINQUEUE)) {
        String newName = EmbeddedMetricsName.INSTANCE_QUEUE + entry.getKey().substring(entry.getKey().lastIndexOf("."))
            + "LifeTimeInQueue";
      }
    }

    return output;
  }

  private void removeUselessMetrics(Map<String, Number> input) {
  }
}
