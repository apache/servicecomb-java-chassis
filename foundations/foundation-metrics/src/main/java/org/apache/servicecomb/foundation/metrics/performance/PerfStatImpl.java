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
 * PerfStatImpl
 *
 *
 */
public class PerfStatImpl implements PerfStat {
  // 接口或是场景名称
  private String name;

  private List<PerfStatData> dataList = new ArrayList<>();

  public PerfStatImpl(String name) {
    this.name = name;
  }

  public PerfStatImpl(String name, PerfStatData data) {
    this.name = name;
    addPerfStatData(data);
  }

  public void addPerfStatData(PerfStatData data) {
    dataList.add(data);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<PerfStatData> getPerfStatDataList() {
    return dataList;
  }

  @Override
  public void mergeFrom(PerfStat otherPerfStat) {
    name = otherPerfStat.getName();
    List<PerfStatData> otherDataList = otherPerfStat.getPerfStatDataList();
    if (dataList.isEmpty()) {
      otherDataList.forEach(otherData -> dataList.add(new PerfStatData(otherData.getName())));
    }

    for (int idx = 0; idx < otherDataList.size(); idx++) {
      dataList.get(idx).mergeFrom(otherDataList.get(idx));
    }
  }

  @Override
  public void calc(long msNow, List<PerfResult> perfResultList) {
    for (PerfStatData data : dataList) {
      perfResultList.add(data.calc(msNow));
    }
  }

  @Override
  public void calc(PerfStat lastCycle, long msCycle, List<PerfResult> perfResultList) {
    if (lastCycle == null) {
      return;
    }

    List<PerfStatData> lastCycleDataList = lastCycle.getPerfStatDataList();
    for (int idx = 0; idx < dataList.size(); idx++) {
      PerfStatData data = dataList.get(idx);
      PerfStatData lastCycleData = lastCycleDataList.get(idx);

      perfResultList.add(data.calc(lastCycleData, msCycle));
    }
  }
}
