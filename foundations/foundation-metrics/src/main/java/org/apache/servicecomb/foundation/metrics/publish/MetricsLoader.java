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

package org.apache.servicecomb.foundation.metrics.publish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;

//load origin metrics value and publish tree
public class MetricsLoader {

  private final Map<String, List<Metric>> metrics;

  public MetricsLoader(Map<String, Double> metrics) {
    this.metrics = new HashMap<>();
    for (Entry<String, Double> entry : metrics.entrySet()) {
      Metric metric = new Metric(entry.getKey(), entry.getValue());
      this.metrics.computeIfAbsent(metric.getName(), m -> new ArrayList<>()).add(metric);
    }
  }

  public MetricNode getMetricTree(String id, String... groupTagKeys) {
    if (containsId(id)) {
      return new MetricNode(metrics.get(id), groupTagKeys);
    }
    throw new ServiceCombException("no such id : " + id);
  }

  public boolean containsId(String id) {
    return metrics.containsKey(id);
  }

  public double getFirstMatchMetricValue(String name, String tagKey, String tagValue) {
    if (metrics.containsKey(name)) {
      for (Metric metric : this.metrics.get(name)) {
        if (metric.containsTag(tagKey, tagValue)) {
          return metric.getValue();
        }
      }
    }
    return Double.NaN;
  }
}