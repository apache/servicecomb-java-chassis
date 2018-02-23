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

package org.apache.servicecomb.foundation.metrics.publish;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.metrics.MetricsConst;

public class Metric {
  private final String name;

  private final Map<String, String> tags;

  private double value;

  public String getName() {
    return name;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public double getValue() {
    return value;
  }

  public double getValue(TimeUnit unit) {
    if (tags.containsKey(MetricsConst.TAG_UNIT)) {
      if (!tags.get(MetricsConst.TAG_UNIT).equals(String.valueOf(unit))) {
        return unit.convert((long) value, TimeUnit.valueOf(tags.get(MetricsConst.TAG_UNIT)));
      }
    }
    return value;
  }

  public Metric(String id, double value) {
    String[] nameAndTag = id.split("\\(");
    this.tags = new HashMap<>();
    String[] tagAnValues = nameAndTag[1].split("[=,)]");
    for (int i = 0; i < tagAnValues.length; i += 2) {
      this.tags.put(tagAnValues[i], tagAnValues[i + 1]);
    }
    this.name = nameAndTag[0];
    this.value = value;
  }

  public boolean containTag(String tagKey, String tagValue) {
    return tags.containsKey(tagKey) && tagValue.equals(tags.get(tagKey));
  }

  public boolean containTag(String... tags) {
    for (int i = 0; i < tags.length; i += 2) {
      if (!containTag(tags[i], tags[i + 1])) {
        return false;
      }
    }
    return true;
  }
}