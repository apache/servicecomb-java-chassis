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
import java.util.List;
import java.util.Map;

public class Metric {
  private final String id;

  private final Map<String, String> tags;

  private double value;

  public String getId() {
    return id;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public double getValue() {
    return value;
  }

  public Metric(String fullId, double value) {
    String[] nameAndTag = fullId.split("\\(");
    this.tags = new HashMap<>();
    String[] tagAnValues = nameAndTag[1].split("[=,)]");
    for (int i = 0; i < tagAnValues.length; i += 2) {
      this.tags.put(tagAnValues[i], tagAnValues[i + 1]);
    }
    this.id = nameAndTag[0];
    this.value = value;
  }

  public boolean containTag(List<String> tagKeys, List<String> tagValues) {
    for (int i = 0; i < tagKeys.size(); i++) {
      if (!containTag(tagKeys.get(i), tagValues.get(i))) {
        return false;
      }
    }
    return true;
  }

  public boolean containTag(String tagKey, String tagValue) {
    return tags.containsKey(tagKey) && tagValue.equals(tags.get(tagKey));
  }
}
