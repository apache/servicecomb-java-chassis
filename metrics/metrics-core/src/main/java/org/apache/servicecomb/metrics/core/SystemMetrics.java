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

package org.apache.servicecomb.metrics.core;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

public class SystemMetrics {
  private final OperatingSystemMXBean systemMXBean;

  private final ThreadMXBean threadMXBean;

  private final MemoryMXBean memoryMXBean;

  public SystemMetrics() {
    this(ManagementFactory.getOperatingSystemMXBean(), ManagementFactory.getThreadMXBean(),
        ManagementFactory.getMemoryMXBean());
  }

  public SystemMetrics(OperatingSystemMXBean systemMXBean, ThreadMXBean threadMXBean,
      MemoryMXBean memoryMXBean) {
    this.systemMXBean = systemMXBean;
    this.threadMXBean = threadMXBean;
    this.memoryMXBean = memoryMXBean;
  }

  public double getCpuLoad() {
    return systemMXBean.getSystemLoadAverage();
  }

  public int getCpuRunningThreads() {
    return threadMXBean.getThreadCount();
  }

  public long getHeapInit() {
    return memoryMXBean.getHeapMemoryUsage().getInit();
  }

  public long getHeapMax() {
    return memoryMXBean.getHeapMemoryUsage().getMax();
  }

  public long getHeapCommit() {
    return memoryMXBean.getHeapMemoryUsage().getCommitted();
  }

  public long getHeapUsed() {
    return memoryMXBean.getHeapMemoryUsage().getUsed();
  }

  public long getNonHeapInit() {
    return memoryMXBean.getNonHeapMemoryUsage().getInit();
  }

  public long getNonHeapMax() {
    return memoryMXBean.getNonHeapMemoryUsage().getMax();
  }

  public long getNonHeapCommit() {
    return memoryMXBean.getNonHeapMemoryUsage().getCommitted();
  }

  public long getNonHeapUsed() {
    return memoryMXBean.getNonHeapMemoryUsage().getUsed();
  }
}
