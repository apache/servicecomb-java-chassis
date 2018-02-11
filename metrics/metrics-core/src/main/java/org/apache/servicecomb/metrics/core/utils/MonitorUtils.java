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

import java.util.concurrent.TimeUnit;

import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.tag.Tag;
import com.netflix.servo.tag.TagList;

public class MonitorUtils {

  //for time-related monitor type, if stop poll value over one window time,
  //the value may return -1 because servo can't known precise value of previous step
  //so must change to return 0
  public static long adjustValue(long value) {
    return value < 0 ? 0 : value;
  }

  //Counting use System.nano get more precise time
  //so we need change unit to millisecond when ouput
  public static long convertNanosecondToMillisecond(long nanoValue) {
    return TimeUnit.NANOSECONDS.toMillis(nanoValue);
  }

  public static String getMonitorName(MonitorConfig config) {
    TagList tags = config.getTags();
    StringBuilder tagPart = new StringBuilder("(");
    for (Tag tag : tags) {
      if (!"type".equals(tag.getKey())) {
        tagPart.append(String.format("%s=%s,", tag.getKey(), tag.getValue()));
      }
    }
    tagPart.deleteCharAt(tagPart.length() - 1);
    tagPart.append(")");
    return config.getName() + tagPart.toString();
  }
}
