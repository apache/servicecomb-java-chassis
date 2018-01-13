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

package org.apache.servicecomb.foundation.metrics;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.CommonThread;
import org.apache.servicecomb.foundation.metrics.performance.PerfStat;
import org.apache.servicecomb.foundation.metrics.performance.PerfStatMonitorMgr;
import org.apache.servicecomb.foundation.metrics.performance.PerfStatSuccFail;

/**
 * Metrics
 * core layer performance logger
 *
 */
public class Metrics extends CommonThread {
  // 每个线程只在本线程内部做统计，不涉及多线程并发
  // 每个周期，统计线程会对所有线程做一次汇总，并与前一周期结果做对比，得出本周期的统计数据
  protected static final ThreadLocal<Map<String, PerfStatSuccFail>> LOCAL_PERF_STAT_MAP = new ThreadLocal<>();

  private static PerfStatMonitorMgr perfMonitorMgr = new PerfStatMonitorMgr();

  public static void onCycle() throws Exception {
    long msNow = System.currentTimeMillis();

    synchronized (perfMonitorMgr) {
      perfMonitorMgr.onCycle(msNow, MetricsConfig.getMsCycle());
    }
  }

  public static long getMsTick() {
    return MetricsThread.getMsTick();
  }

  public static void registerPerfStat(PerfStat perfStat, int index) {
    synchronized (perfMonitorMgr) {
      perfMonitorMgr.registerPerfStat(perfStat, index);
    }
  }

  public static PerfStatSuccFail getOrCreateLocalPerfStat(String name, int index) {
    Map<String, PerfStatSuccFail> map = LOCAL_PERF_STAT_MAP.get();
    if (map == null) {
      map = new HashMap<>();
      LOCAL_PERF_STAT_MAP.set(map);
    }

    PerfStatSuccFail perfStat = map.get(name);
    if (perfStat == null) {
      perfStat = new PerfStatSuccFail(name);
      map.put(name, perfStat);

      registerPerfStat(perfStat, index);
    }

    return perfStat;
  }

  public static Map<String, PerfStat> getMonitorPerfStat() {
    synchronized (perfMonitorMgr) {
      return perfMonitorMgr.getMonitorPerfStat();
    }
  }
}
