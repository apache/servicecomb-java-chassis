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

import org.apache.servicecomb.metrics.common.RegistryMetric;

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
  public Map<String, String> convert(RegistryMetric registryMetric) {
    Map<String, String> pickedMetrics = new HashMap<>();
    for (Entry<String, Number> metric : registryMetric.toMap().entrySet()) {
      pickedMetrics.put(metric.getKey(),
          String.format(doubleStringFormatter,
              round(metric.getValue().doubleValue(), doubleRoundPlaces)));
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
}
