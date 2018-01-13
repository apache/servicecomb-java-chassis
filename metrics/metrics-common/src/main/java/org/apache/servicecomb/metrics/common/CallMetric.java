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

public class CallMetric {
  private final String prefix;

  private final long total;

  private final double tps;

  public long getTotal() {
    return total;
  }

  public double getTps() {
    return tps;
  }

  public CallMetric(String prefix) {
    this(prefix, 0, 0);
  }

  public CallMetric(@JsonProperty("prefix") String prefix, @JsonProperty("total") long total,
      @JsonProperty("tps") double tps) {
    this.prefix = prefix;
    this.total = total;
    this.tps = tps;
  }

  public CallMetric merge(CallMetric metric) {
    return new CallMetric(this.prefix, this.total + metric.total, this.tps + metric.tps);
  }

  public Map<String, Number> toMap() {
    Map<String, Number> metrics = new HashMap<>();
    metrics.put(prefix + ".total", total);
    metrics.put(prefix + ".tps", tps);
    return metrics;
  }
}
