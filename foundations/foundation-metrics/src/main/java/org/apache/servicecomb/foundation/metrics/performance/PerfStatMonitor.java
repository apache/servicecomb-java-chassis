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

package org.apache.servicecomb.foundation.metrics.performance;

import java.util.ArrayList;
import java.util.List;

/**
 * PerfStatMonitor
 *
 *
 */
public class PerfStatMonitor {
  // 各线程内部的统计数据
  private List<PerfStat> threadStats = new ArrayList<>();

  private String name;

  private int index;

  // 每周期，对threadStats进行汇总，结果保存在这里
  private PerfStat sumStat;

  // 每周期计算产生的结果
  private List<PerfResult> perfResultList;

  public PerfStatMonitor(String name, int index) {
    this.name = name;
    this.index = index;
  }

  public String getName() {
    return name;
  }

  public int getIndex() {
    return index;
  }

  public void addThreadStat(PerfStat threadStat) {
    threadStats.add(threadStat);
  }

  public void calcCycle(long msNow, long msCycle) {
    PerfStat newSumStat = new PerfStatImpl(null);
    for (PerfStat threadStat : threadStats) {
      newSumStat.mergeFrom(threadStat);
    }

    perfResultList = new ArrayList<>();
    newSumStat.calc(msNow, perfResultList);
    newSumStat.calc(sumStat, msCycle, perfResultList);

    sumStat = newSumStat;
  }

  public void format(StringBuilder sb, String fmt) {
    for (PerfResult result : perfResultList) {
      String msg = String.format(result.getName() + fmt,
          result.getCallCount(),
          result.getMsgCount(),
          result.getAvgCallCount(),
          result.getMsAvgLatency(),
          result.segmentsToString("%-10d"));
      sb.append(msg);
    }
  }

  public PerfStat getPerfStat() {
    return sumStat;
  }

  public List<PerfResult> getPerfResultList() {
    return perfResultList;
  }
}
