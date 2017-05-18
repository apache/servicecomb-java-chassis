/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.paas.foundation.metrics;

import java.util.HashMap;
import java.util.Map;

import com.huawei.paas.foundation.common.CommonThread;
import com.huawei.paas.foundation.metrics.performance.PerfStat;
import com.huawei.paas.foundation.metrics.performance.PerfStatMonitorMgr;
import com.huawei.paas.foundation.metrics.performance.PerfStatSuccFail;

/**
 * Metrics
 * core layer performance logger
 * @author  
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

    /**
     * 注册监控项
     * @param perfStat  perfStat
     * @param index     序号
     */
    public static void registerPerfStat(PerfStat perfStat, int index) {
        synchronized (perfMonitorMgr) {
            perfMonitorMgr.registerPerfStat(perfStat, index);
        }
    }

    /**
     * getOrCreateLocalPerfStat
     * @param name      统计名称
     * @param index     序号
     * @return PerfStatSuccFail
     */
    public static PerfStatSuccFail getOrCreateLocalPerfStat(String name, int index) {
        Map<String, PerfStatSuccFail> map = LOCAL_PERF_STAT_MAP.get();
        if (map == null) {
            map = new HashMap<>();
            LOCAL_PERF_STAT_MAP.set(map);
        }

        PerfStatSuccFail perfStat = map.get(name);
        if (perfStat == null) {
            perfStat = new PerfStatSuccFail(name);
            // System.out.println(" *************************create stat " +
            // name);
            map.put(name, perfStat);

            registerPerfStat(perfStat, index);

        }

        return perfStat;
    }

    /**
     * 获取统计数据
     * @return name -> perfstat
     */
    public static Map<String, PerfStat> getMonitorPerfStat() {
        synchronized (perfMonitorMgr) {
            return perfMonitorMgr.getMonitorPerfStat();
        }
    }
}
