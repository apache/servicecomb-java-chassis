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

package org.apache.servicecomb.metrics.core.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.metrics.common.MetricsConst;
import org.springframework.stereotype.Component;

@Component
public class DefaultSystemMonitor implements SystemMonitor {

  private final OperatingSystemMXBean systemMXBean;

  private final ThreadMXBean threadMXBean;

  private final MemoryMXBean memoryMXBean;

  public DefaultSystemMonitor() {
    this(ManagementFactory.getOperatingSystemMXBean(), ManagementFactory.getThreadMXBean(),
        ManagementFactory.getMemoryMXBean());
  }

  public DefaultSystemMonitor(OperatingSystemMXBean systemMXBean, ThreadMXBean threadMXBean,
      MemoryMXBean memoryMXBean) {
    this.systemMXBean = systemMXBean;
    this.threadMXBean = threadMXBean;
    this.memoryMXBean = memoryMXBean;
  }

  @Override
  public double getCpuLoad() {
    return systemMXBean.getSystemLoadAverage();
  }

  @Override
  public int getCpuRunningThreads() {
    return threadMXBean.getThreadCount();
  }

  @Override
  public long getHeapInit() {
    return memoryMXBean.getHeapMemoryUsage().getInit();
  }

  @Override
  public long getHeapMax() {
    return memoryMXBean.getHeapMemoryUsage().getMax();
  }

  @Override
  public long getHeapCommit() {
    return memoryMXBean.getHeapMemoryUsage().getCommitted();
  }

  @Override
  public long getHeapUsed() {
    return memoryMXBean.getHeapMemoryUsage().getUsed();
  }

  @Override
  public long getNonHeapInit() {
    return memoryMXBean.getNonHeapMemoryUsage().getInit();
  }

  @Override
  public long getNonHeapMax() {
    return memoryMXBean.getNonHeapMemoryUsage().getMax();
  }

  @Override
  public long getNonHeapCommit() {
    return memoryMXBean.getNonHeapMemoryUsage().getCommitted();
  }

  @Override
  public long getNonHeapUsed() {
    return memoryMXBean.getNonHeapMemoryUsage().getUsed();
  }

  @Override
  public Map<String, Double> toMetric() {
    Map<String, Double> metrics = new HashMap<>();
    metrics.put(String.format(MetricsConst.JVM + "(%s=%s,%s=%s)", MetricsConst.TAG_STATISTIC, "gauge",
        MetricsConst.TAG_NAME, "cpuLoad"), getCpuLoad());
    metrics.put(String.format(MetricsConst.JVM + "(%s=%s,%s=%s)", MetricsConst.TAG_STATISTIC, "gauge",
        MetricsConst.TAG_NAME, "cpuRunningThreads"), (double) getCpuRunningThreads());
    metrics.put(String.format(MetricsConst.JVM + "(%s=%s,%s=%s)", MetricsConst.TAG_STATISTIC, "gauge",
        MetricsConst.TAG_NAME, "heapInit"), (double) getHeapInit());
    metrics.put(String.format(MetricsConst.JVM + "(%s=%s,%s=%s)", MetricsConst.TAG_STATISTIC, "gauge",
        MetricsConst.TAG_NAME, "heapMax"), (double) getHeapMax());
    metrics.put(String.format(MetricsConst.JVM + "(%s=%s,%s=%s)", MetricsConst.TAG_STATISTIC, "gauge",
        MetricsConst.TAG_NAME, "heapCommit"), (double) getHeapCommit());
    metrics.put(String.format(MetricsConst.JVM + "(%s=%s,%s=%s)", MetricsConst.TAG_STATISTIC, "gauge",
        MetricsConst.TAG_NAME, "heapUsed"), (double) getHeapUsed());
    metrics.put(String.format(MetricsConst.JVM + "(%s=%s,%s=%s)", MetricsConst.TAG_STATISTIC, "gauge",
        MetricsConst.TAG_NAME, "nonHeapInit"), (double) getNonHeapInit());
    metrics.put(String.format(MetricsConst.JVM + "(%s=%s,%s=%s)", MetricsConst.TAG_STATISTIC, "gauge",
        MetricsConst.TAG_NAME, "nonHeapMax"), (double) getNonHeapMax());
    metrics.put(String.format(MetricsConst.JVM + "(%s=%s,%s=%s)", MetricsConst.TAG_STATISTIC, "gauge",
        MetricsConst.TAG_NAME, "nonHeapCommit"), (double) getNonHeapCommit());
    metrics.put(String.format(MetricsConst.JVM + "(%s=%s,%s=%s)", MetricsConst.TAG_STATISTIC, "gauge",
        MetricsConst.TAG_NAME, "nonHeapUsed"), (double) getNonHeapUsed());
    return metrics;
  }
}
