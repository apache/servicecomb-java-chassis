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

package org.apache.servicecomb.metrics.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.metrics.MetricsConst;

import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.tag.Tag;
import com.netflix.servo.tag.TagList;
import com.netflix.servo.tag.Tags;

public class MetricsUtils {

  private static Map<TimeUnit, Function<Double, Double>> convertFunctions;

  static {
    //don't use TimeUnit convert method because it will return long and lost decimal part
    convertFunctions = new HashMap<>();
    convertFunctions.put(TimeUnit.NANOSECONDS, value -> value);
    convertFunctions.put(TimeUnit.MICROSECONDS, value -> value * 0.001);
    convertFunctions.put(TimeUnit.MILLISECONDS, value -> value * 0.001 * 0.001);
    convertFunctions.put(TimeUnit.SECONDS, value -> value * 0.001 * 0.001 * 0.001);
  }

  public static Map<String, Double> convertMeasurements(Map<MonitorConfig, Double> measurements, TimeUnit unit) {
    if (validateTimeUnit(unit)) {
      Map<String, Double> metrics = new HashMap<>();
      for (Entry<MonitorConfig, Double> measurement : measurements.entrySet()) {
        if (measurement.getKey().getTags().containsKey(MetricsConst.TAG_UNIT)) {
          metrics.put(getMonitorKey(
              measurement.getKey().withAdditionalTag(Tags.newTag(MetricsConst.TAG_UNIT, String.valueOf(unit)))),
              convertFunctions.get(unit).apply(measurement.getValue()));
        } else {
          metrics.put(getMonitorKey(measurement.getKey()), measurement.getValue());
        }
      }
      return metrics;
    }
    //no need support MINUTES,HOURS,DAYS because latency under this unit is unsuitable
    throw new ServiceCombException(
        "illegal unit : " + String.valueOf(unit) + ", only support NANOSECONDS,MICROSECONDS,MILLISECONDS and SECONDS");
  }

  private static boolean validateTimeUnit(TimeUnit unit) {
    return unit == TimeUnit.NANOSECONDS || unit == TimeUnit.MICROSECONDS || unit == TimeUnit.MILLISECONDS
        || unit == TimeUnit.SECONDS;
  }

  private static String getMonitorKey(MonitorConfig config) {
    TagList tagList = config.getTags();
    List<String> tags = new ArrayList<>();
    for (Tag tag : tagList) {
      if (!"type".equals(tag.getKey())) {
        tags.add(tag.getKey());
        tags.add(tag.getValue());
      }
    }
    return getMonitorKey(config.getName(), tags.toArray(new String[0]));
  }

  public static String getMonitorKey(String name, String... tags) {
    if (tags.length != 0) {
      SortedMap<String, String> tagMap = new TreeMap<>();
      for (int i = 0; i < tags.length; i += 2) {
        tagMap.put(tags[i], tags[i + 1]);
      }
      StringBuilder builder = new StringBuilder("(");
      for (Entry<String, String> entry : tagMap.entrySet()) {
        builder.append(String.format("%s=%s,", entry.getKey(), entry.getValue()));
      }
      builder.deleteCharAt(builder.length() - 1);
      builder.append(")");
      return name + builder.toString();
    }
    return name;
  }
}
