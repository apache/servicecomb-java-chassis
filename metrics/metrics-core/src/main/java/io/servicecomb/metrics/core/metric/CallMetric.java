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

public class CallMetric {
  private final long total;

  private final double tps;

  public long getTotal() {
    return total;
  }

  public double getTps() {
    return tps;
  }

  public CallMetric() {
    this(0, 0);
  }

  public CallMetric(long total, double tps) {
    this.total = total;
    this.tps = tps;
  }

  public CallMetric merge(CallMetric metric) {
    return new CallMetric(this.total + metric.total, this.tps + metric.tps);
  }
}
