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
package org.apache.servicecomb.it;

import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.apache.servicecomb.it.junit.SCBFailure;

public class ResultPrinter {
  private long start = System.currentTimeMillis();

  public String formatTotalTime() {
    long totalTime = System.currentTimeMillis() - start;
    long hour = 0;
    long minute = 0;
    long second = 0;
    second = totalTime / 1000;
    if (totalTime <= 1000 && totalTime > 0) {
      second = 1;
    }
    if (second > 60) {
      minute = second / 60;
      second = second % 60;
    }
    if (minute > 60) {
      hour = minute / 60;
      minute = minute % 60;
    }
    // 转换时分秒 00:00:00
    String duration =
        (hour >= 10 ? hour : "0" + hour) + ":" + (minute >= 10 ? minute : "0" + minute) + ":" + (second >= 10 ? second
            : "0" + second);
    return duration;
  }

  public void print() {
    StringBuilder sb = new StringBuilder();
    String totalTime = formatTotalTime();
    for (SCBFailure failure : ITJUnitUtils.getFailures()) {
      sb.append(String.format("%s, %s\n"
              + "%s\n",
          failure.getParents(),
          failure.getTestHeader(),
          failure.getTrace()));
    }
    sb.append(String.format("\nrun count:%d, failed count: %d, totalTime: %s.\n",
        ITJUnitUtils.getRunCount(),
        ITJUnitUtils.getFailures().size(),
        totalTime));
    System.out.println(sb.toString());
  }
}
