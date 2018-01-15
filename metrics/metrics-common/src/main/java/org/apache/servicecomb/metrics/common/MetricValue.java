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

import java.util.Arrays;
import java.util.Map;

public class MetricValue<T extends Number> {
  private final String key;

  private final T value;

  private final Map<String, String> dimensions;

  public String getKey() {
    return key;
  }

  public T getValue() {
    return value;
  }

  public Map<String, String> getDimensions() {
    return dimensions;
  }

  public MetricValue(T value, Map<String, String> dimensions) {
    String finalKey = "{}";
    if (dimensions != null && dimensions.size() != 0) {
      String[] keys = dimensions.keySet().toArray(new String[0]);
      Arrays.sort(keys);
      StringBuilder builder = new StringBuilder("{");
      for (String key : keys) {
        builder.append(String.format("%s=%s,", key, dimensions.get(key)));
      }
      builder.deleteCharAt(builder.length() - 1);
      builder.append("}");
      finalKey = builder.toString();
    }
    this.key = finalKey;
    this.value = value;
    this.dimensions = dimensions;
  }

  public MetricValue(String key, T value, Map<String, String> dimensions) {
    this.key = key;
    this.value = value;
    this.dimensions = dimensions;
  }

  public boolean containDimension(String dimensionKey, String dimensionValue) {
    return this.getDimensions().containsKey(dimensionKey) &&
        dimensionValue.equals(this.getDimensions().get(dimensionKey));
  }
}
