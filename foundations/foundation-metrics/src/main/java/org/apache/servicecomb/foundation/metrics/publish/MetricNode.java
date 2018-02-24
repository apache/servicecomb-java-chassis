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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.metrics.MetricsConst;

import com.google.common.collect.Lists;

public class MetricNode {
  private final String tagKey;

  private final List<Metric> metrics;

  private final Map<String, MetricNode> children;

  public MetricNode(Iterable<Metric> metrics, String... groupTagKeys) {
    if (groupTagKeys == null || groupTagKeys.length == 0) {
      this.tagKey = null;
      this.metrics = Lists.newArrayList(metrics);
      this.children = null;
    } else {
      this.tagKey = groupTagKeys[0];
      this.metrics = null;
      this.children = new HashMap<>();
      Map<String, List<Metric>> groups = groupByTag(metrics, this.tagKey);
      if (groupTagKeys.length == 1) {
        for (Entry<String, List<Metric>> group : groups.entrySet()) {
          this.children.put(group.getKey(), new MetricNode(null, group.getValue(), null));
        }
      } else {
        for (Entry<String, List<Metric>> group : groups.entrySet()) {
          this.children.put(group.getKey(),
              new MetricNode(group.getValue(), Arrays.copyOfRange(groupTagKeys, 1, groupTagKeys.length)));
        }
      }
    }
  }

  private MetricNode(String tagKey, List<Metric> metrics, Map<String, MetricNode> children) {
    this.tagKey = tagKey;
    this.metrics = metrics;
    this.children = children;
  }

  public Iterable<Metric> getMetrics() {
    return metrics;
  }

  public int getMetricCount() {
    return metrics.size();
  }

  public Iterable<Entry<String, MetricNode>> getChildren() {
    return children.entrySet();
  }

  public MetricNode getChildren(String tagValue) {
    return children.get(tagValue);
  }

  public int getChildrenCount() {
    return children.size();
  }

  public MetricNode getChildrenNode(String tagValue) {
    return children.get(tagValue);
  }

  public Double getFirstMatchMetricValue(String tagKey, String tagValue) {
    for (Metric metric : this.metrics) {
      if (metric.containsTag(tagKey, tagValue)) {
        return metric.getValue();
      }
    }
    return Double.NaN;
  }

  public Double getFirstMatchMetricValue(TimeUnit unit, String tagKey, String tagValue) {
    for (Metric metric : this.metrics) {
      if (metric.containsTag(tagKey, tagValue)) {
        return metric.getValue(unit);
      }
    }
    return Double.NaN;
  }

  public Double getFirstMatchMetricValue(String... tags) {
    for (Metric metric : this.metrics) {
      if (metric.containsTag(tags)) {
        return metric.getValue();
      }
    }
    return Double.NaN;
  }

  public Double getFirstMatchMetricValue(TimeUnit unit, String... tags) {
    for (Metric metric : this.metrics) {
      if (metric.containsTag(tags)) {
        return metric.getValue(unit);
      }
    }
    return Double.NaN;
  }

  public double getMatchStatisticMetricValue(String statisticValue) {
    return getFirstMatchMetricValue(MetricsConst.TAG_STATISTIC, statisticValue);
  }

  public double getMatchStatisticMetricValue(TimeUnit unit, String statisticValue) {
    return getFirstMatchMetricValue(unit, MetricsConst.TAG_STATISTIC, statisticValue);
  }

  private Map<String, List<Metric>> groupByTag(Iterable<Metric> metrics, String tagKey) {
    Map<String, List<Metric>> groups = new HashMap<>();
    for (Metric metric : metrics) {
      if (metric.containsTagKey(tagKey)) {
        groups.computeIfAbsent(metric.getTagValue(tagKey), g -> new ArrayList<>()).add(metric);
      }
    }
    return groups;
  }
}