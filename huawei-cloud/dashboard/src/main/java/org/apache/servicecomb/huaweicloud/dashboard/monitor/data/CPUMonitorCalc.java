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

package org.apache.servicecomb.huaweicloud.dashboard.monitor.data;


import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

public class CPUMonitorCalc {
  private static final int PERCENTAGE = 100;

  private static final CPUMonitorCalc instance = new CPUMonitorCalc();

  private final OperatingSystemMXBean osMxBean;

  private final ThreadMXBean threadMXBean;

  private long preTime = System.nanoTime();

  private long preUsedTime = 0;

  private CPUMonitorCalc() {
    osMxBean = ManagementFactory.getOperatingSystemMXBean();
    threadMXBean = ManagementFactory.getThreadMXBean();
  }

  public static CPUMonitorCalc getInstance() {
    return instance;
  }

  public double getProcessCpu() {
    long totalTime = 0;

    for (long id : threadMXBean.getAllThreadIds()) {
      totalTime += threadMXBean.getThreadCpuTime(id);
    }

    long curtime = System.nanoTime();
    long usedTime = totalTime - preUsedTime;
    long totalPassedTime = curtime - preTime;
    preTime = curtime;
    preUsedTime = totalTime;
    return (((double) usedTime) / totalPassedTime / osMxBean.getAvailableProcessors()) * PERCENTAGE;
  }
}
