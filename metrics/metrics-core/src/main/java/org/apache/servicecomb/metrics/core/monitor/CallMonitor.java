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

package org.apache.servicecomb.metrics.core.monitor;

import org.apache.servicecomb.metrics.common.CallMetric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.StepCounter;
import com.netflix.servo.tag.Tag;
import com.netflix.servo.tag.TagList;

import io.servicecomb.metrics.common.CallMetric;
import io.servicecomb.metrics.common.DoubleMetricValue;
import io.servicecomb.metrics.common.LongMetricValue;
import io.servicecomb.metrics.core.MetricsDimension;

public class CallMonitor {
  private final String prefix;

  private final List<BasicCounter> totalCounters;

  private final List<StepCounter> tpsCounters;

  public CallMonitor(String prefix, String... dimensions) {
    this.prefix = prefix;
    this.totalCounters = new ArrayList<>();
    this.tpsCounters = new ArrayList<>();
    if (dimensions.length == 0) {
      this.totalCounters.add(new BasicCounter(MonitorConfig.builder(prefix + ".total").build()));
      this.tpsCounters.add(new StepCounter(MonitorConfig.builder(prefix + ".tps").build()));
    } else {
      for (String dimension : dimensions) {
        for (String option : MetricsDimension.getDimensionOptions(dimension)) {
          this.totalCounters
              .add(new BasicCounter(MonitorConfig.builder(prefix + ".total").withTag(dimension, option).build()));
          this.tpsCounters
              .add(new StepCounter(MonitorConfig.builder(prefix + ".tps").withTag(dimension, option).build()));
        }
      }
    }
  }

  public void increment() {
    for (int i = 0; i < totalCounters.size(); i++) {
      totalCounters.get(i).increment();
      tpsCounters.get(i).increment();
    }
  }

  public CallMetric toMetric(int windowTimeIndex) {
    List<LongMetricValue> totalValues = new ArrayList<>();
    List<DoubleMetricValue> tpsValues = new ArrayList<>();
    for (int i = 0; i < totalCounters.size(); i++) {
      totalValues.add(new LongMetricValue(totalCounters.get(i).getValue(windowTimeIndex).longValue(),
          convertTags(totalCounters.get(i).getConfig().getTags())));
      tpsValues.add(new DoubleMetricValue(adjustValue(tpsCounters.get(i).getValue(windowTimeIndex).doubleValue()),
          convertTags(tpsCounters.get(i).getConfig().getTags())));
    }
    return new CallMetric(this.prefix, totalValues, tpsValues);
  }

  //for time-related monitor type, if stop poll value over one window time,
  //the value may return -1 because servo can't known precise value of previous step
  //so must change to return 0
  private double adjustValue(double value) {
    return value < 0 ? 0 : value;
  }

  private Map<String, String> convertTags(TagList tags) {
    if (tags.size() != 0) {
      Map<String, String> tagMap = new HashMap<>();
      for (Tag tag : tags) {
        tagMap.put(tag.getKey(), tag.getValue());
      }
      return tagMap;
    }
    return null;
  }
}
