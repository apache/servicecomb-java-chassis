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

package org.apache.servicecomb.metrics.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.netflix.servo.monitor.Monitor;
import com.netflix.servo.tag.Tag;
import com.netflix.servo.tag.TagList;

public class MonitorUtils {

  //for time-related monitor type, if stop poll value over one window time,
  //the value may return -1 or NaN because servo can't known precise value of previous step
  //so must change to return 0
  public static double adjustValue(double value) {
    return Double.isNaN(value) || value < 0 ? 0 : value;
  }

  //for time-related monitor type, if stop poll value over one window time,
  //the value may return -1 because servo can't known precise value of previous step
  //so must change to return 0
  public static long adjustValue(long value) {
    return value < 0 ? 0 : value;
  }

  public static boolean containsTagValue(Monitor monitor, String tagKey, String tagValue) {
    TagList tags = monitor.getConfig().getTags();
    return tags.containsKey(tagKey) && tagValue.equals(tags.getTag(tagKey).getValue());
  }

  public static Map<String, String> convertTags(Monitor monitor) {
    TagList tags = monitor.getConfig().getTags();
    if (tags.size() != 0) {
      Map<String, String> tagMap = new HashMap<>();
      for (Tag tag : tags) {
        //we don't need servo internal type tag for metrics
        if (!"type".equals(tag.getKey())) {
          tagMap.put(tag.getKey(), tag.getValue());
        }
      }
      return tagMap;
    }
    return null;
  }

  //Counting use System.nano get more precise time
  //so we need change unit to millisecond when ouput
  public static long convertNanosecondToMillisecond(long nanoValue) {
    return TimeUnit.NANOSECONDS.toMillis(nanoValue);
  }
}
