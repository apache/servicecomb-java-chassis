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

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.metrics.MetricsConst;

public class Metric {
  private final String name;

  private final Map<String, String> tags;

  private double value;

  public String getName() {
    return name;
  }

  public Metric(String id, double value) {
    if (isCorrectId(id)) {
      this.tags = new HashMap<>();
      this.value = value;
      String[] nameAndTag = id.split("[()]");
      if (nameAndTag.length == 1) {
        if (!id.endsWith(")")) {
          this.name = nameAndTag[0];
        } else {
          throw new ServiceCombException("bad format id");
        }
      } else if (nameAndTag.length == 2) {
        this.name = nameAndTag[0];
        String[] tagAnValues = nameAndTag[1].split(",");
        for (String tagAnValue : tagAnValues) {
          String[] kv = tagAnValue.split("=");
          if (kv.length == 2) {
            this.tags.put(kv[0], kv[1]);
          } else {
            throw new ServiceCombException("bad format tag");
          }
        }
      } else {
        throw new ServiceCombException("bad format id");
      }
    } else {
      throw new ServiceCombException("bad format id");
    }
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

  public int getTagsCount() {
    return tags.size();
  }

  public boolean containsTagKey(String tagKey) {
    return tags.containsKey(tagKey);
  }

  public String getTagValue(String tagKey) {
    return tags.get(tagKey);
  }

  public boolean containsTag(String tagKey, String tagValue) {
    return tags.containsKey(tagKey) && tagValue.equals(tags.get(tagKey));
  }

  public boolean containsTag(String... tags) {
    if (tags.length >= 2 && tags.length % 2 == 0) {
      for (int i = 0; i < tags.length; i += 2) {
        if (!containsTag(tags[i], tags[i + 1])) {
          return false;
        }
      }
      return true;
    }
    throw new ServiceCombException("bad tags count");
  }

  private int getCharCount(String id, char c) {
    int count = 0;
    for (char cr : id.toCharArray()) {
      if (cr == c) {
        count++;
      }
    }
    return count;
  }

  private boolean isCorrectId(String id) {
    return id != null && !id.endsWith("(") && getCharCount(id, '(') <= 1 && getCharCount(id, ')') <= 1;
  }
}