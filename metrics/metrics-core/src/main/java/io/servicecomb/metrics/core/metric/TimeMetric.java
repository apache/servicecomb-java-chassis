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

package io.servicecomb.metrics.core.metric;

public class TimeMetric {
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

  public TimeMetric() {
    this(0, 0, 0, 0);
  }

  public TimeMetric(long total, long count, long min, long max) {
    this.total = total;
    this.count = count;
    this.average = (double) total / (double) count;
    this.min = min;
    this.max = max;
  }

  public TimeMetric merge(TimeMetric metric) {
    return new TimeMetric(this.total + metric.total, this.count + metric.count,
        getMin(this.min, metric.min), getMax(this.max, metric.max));
  }

  private long getMin(long value1, long value2) {
    return value1 == 0 || (value2 != 0 && value2 < value1) ? value2 : value1;
  }

  private long getMax(long value1, long value2) {
    return value1 == 0 || value2 > value1 ? value2 : value1;
  }
}
