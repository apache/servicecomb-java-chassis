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

package org.apache.servicecomb.metrics.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CallMetric {
  private final String prefix;

  private final List<LongMetricValue> totalValues;

  private final List<DoubleMetricValue> tpsValues;

  public String getPrefix() {
    return prefix;
  }

  public List<LongMetricValue> getTotalValues() {
    return totalValues;
  }

  public LongMetricValue getTotalValue(String dimensionKey, String dimensionValue) {
    for (LongMetricValue value : totalValues) {
      if (value.containDimension(dimensionKey, dimensionValue)) {
        return value;
      }
    }
    return new LongMetricValue(dimensionValue, 0L, null);
  }

  public List<DoubleMetricValue> getTpsValues() {
    return tpsValues;
  }

  public DoubleMetricValue getTpsValue(String dimensionKey, String dimensionValue) {
    for (DoubleMetricValue value : tpsValues) {
      if (value.containDimension(dimensionKey, dimensionValue)) {
        return value;
      }
    }
    return new DoubleMetricValue(dimensionValue, 0.0, null);
  }

  public CallMetric(String prefix) {
    this(prefix, new ArrayList<>(), new ArrayList<>());
  }

  public CallMetric(@JsonProperty("prefix") String prefix,
      @JsonProperty("totalValues") List<LongMetricValue> totalValues,
      @JsonProperty("tpsValues") List<DoubleMetricValue> tpsValues) {
    this.prefix = prefix;
    this.totalValues = totalValues;
    this.tpsValues = tpsValues;
  }

  public CallMetric merge(CallMetric metric) {
    return new CallMetric(this.prefix,
        LongMetricValue.merge(metric.getTotalValues(), this.getTotalValues()),
        DoubleMetricValue.merge(metric.getTpsValues(), this.getTpsValues()));
  }

  public Map<String, Number> toMap() {
    Map<String, Number> metrics = new HashMap<>();
    for (LongMetricValue totalValue : totalValues) {
      metrics.put(prefix + ".total." + totalValue.getKey(), totalValue.getValue());
    }
    for (DoubleMetricValue tpsValue : tpsValues) {
      metrics.put(prefix + ".tps." + tpsValue.getKey(), tpsValue.getValue());
    }
    return metrics;
  }
}
