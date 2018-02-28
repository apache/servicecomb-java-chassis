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

package org.apache.servicecomb.samples.mwf;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.foundation.metrics.MetricsConst;

import com.netflix.config.DynamicPropertyFactory;

public class SimpleFileContentConvertor implements FileContentConvertor {

  private static final String METRICS_ROUND_PLACES = "servicecomb.metrics.round_places";

  private final int doubleRoundPlaces;

  private final String doubleStringFormatter;

  public SimpleFileContentConvertor() {
    doubleRoundPlaces = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_ROUND_PLACES, 1).get();
    doubleStringFormatter = "%." + String.valueOf(doubleRoundPlaces) + "f";
  }

  @Override
  public Map<String, String> convert(Map<String, Double> registryMetric) {
    Map<String, String> pickedMetrics = new HashMap<>();
    for (Entry<String, Double> metric : registryMetric.entrySet()) {
      pickedMetrics.put(convertMetricKey(metric.getKey()),
          String.format(doubleStringFormatter, round(metric.getValue(), doubleRoundPlaces)));
    }
    return pickedMetrics;
  }

  private double round(double value, int places) {
    if (!Double.isNaN(value)) {
      BigDecimal decimal = new BigDecimal(value);
      return decimal.setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
    return 0;
  }

  private String convertMetricKey(String key) {
    String[] nameAndTag = key.split("\\(");
    Map<String, String> tags = new HashMap<>();
    String[] tagAnValues = nameAndTag[1].split("[=,)]");
    for (int i = 0; i < tagAnValues.length; i += 2) {
      tags.put(tagAnValues[i], tagAnValues[i + 1]);
    }
    if (nameAndTag[0].startsWith(MetricsConst.JVM)) {
      return "jvm." + tags.get(MetricsConst.TAG_NAME);
    } else {
      StringBuilder builder = new StringBuilder();
      builder.append(tags.get(MetricsConst.TAG_OPERATION));
      builder.append(".");
      builder.append(tags.get(MetricsConst.TAG_ROLE).toLowerCase());
      builder.append(".");
      builder.append(tags.get(MetricsConst.TAG_STAGE));
      builder.append(".");
      builder.append(tags.get(MetricsConst.TAG_STATISTIC));
      if (tags.containsKey(MetricsConst.TAG_STATUS)) {
        builder.append(".");
        builder.append(tags.get(MetricsConst.TAG_STATUS));
      }
      return builder.toString();
    }
  }
}
