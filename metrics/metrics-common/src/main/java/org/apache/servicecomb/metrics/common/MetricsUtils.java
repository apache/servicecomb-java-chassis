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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MetricsUtils {
  public static Double getFirstMatchMetricValue(Map<String, Double> metrics, String name, List<String> tagKeys,
      List<String> tagValues) {
    List<String> formattedTags = new ArrayList<>();
    for (int i = 0; i < tagKeys.size(); i++) {
      formattedTags.add(String.format("%s=%s", tagKeys.get(i), tagValues.get(i)));
    }

    for (Entry<String, Double> metric : metrics.entrySet()) {
      if (metric.getKey().startsWith(name)) {
        if (containsAll(metric.getKey(), formattedTags)) {
          return metric.getValue();
        }
      }
    }
    return Double.NaN;
  }

  private static boolean containsAll(String s, List<String> items) {
    for (String item : items) {
      if (!s.contains(item)) {
        return false;
      }
    }
    return true;
  }
}
