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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TimerMetric {
  private final String prefix;

  private final long total;

  private final long count;

  private final double average;

  private final long min;

  private final long max;

  public long getTotal() {
    return total;
  }

  public long getCount() {
    return count;
  }

  public double getAverage() {
    return average;
  }

  public long getMin() {
    return min;
  }

  public long getMax() {
    return max;
  }

  public TimerMetric(String prefix) {
    this(prefix, 0, 0, 0, 0);
  }

  public TimerMetric(@JsonProperty("prefix") String prefix,
      @JsonProperty("total") long total, @JsonProperty("count") long count,
      @JsonProperty("min") long min, @JsonProperty("max") long max) {
    this.prefix = prefix;
    this.total = total;
    this.count = count;
    if (count != 0) {
      this.average = total / (double) count;
    } else {
      this.average = 0;
    }
    this.min = min;
    this.max = max;
  }

  public TimerMetric merge(TimerMetric metric) {
    return new TimerMetric(this.prefix, this.total + metric.total, this.count + metric.count,
        getMin(this.min, metric.min), getMax(this.max, metric.max));
  }

  private long getMin(long value1, long value2) {
    return value1 == 0 || (value2 != 0 && value2 < value1) ? value2 : value1;
  }

  private long getMax(long value1, long value2) {
    return value2 > value1 ? value2 : value1;
  }

  public Map<String, Number> toMap() {
    Map<String, Number> metrics = new HashMap<>();
    metrics.put(prefix + ".total", total);
    metrics.put(prefix + ".count", count);
    metrics.put(prefix + ".average", average);
    metrics.put(prefix + ".max", max);
    metrics.put(prefix + ".min", min);
    return metrics;
  }
}
