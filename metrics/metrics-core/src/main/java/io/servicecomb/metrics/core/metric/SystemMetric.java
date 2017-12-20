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

package io.servicecomb.metrics.core.metric;

public class SystemMetric {
  private final double cpuLoad;

  private final int cpuRunningThreads;

  private final long heapInit;

  private final long heapMax;

  private final long heapCommit;

  private final long heapUsed;

  private final long nonHeapInit;

  private final long nonHeapMax;

  private final long nonHeapCommit;

  private final long nonHeapUsed;

  public double getCpuLoad() {
    return cpuLoad;
  }

  public int getCpuRunningThreads() {
    return cpuRunningThreads;
  }

  public long getHeapInit() {
    return heapInit;
  }

  public long getHeapMax() {
    return heapMax;
  }

  public long getHeapCommit() {
    return heapCommit;
  }

  public long getHeapUsed() {
    return heapUsed;
  }

  public long getNonHeapInit() {
    return nonHeapInit;
  }

  public long getNonHeapMax() {
    return nonHeapMax;
  }

  public long getNonHeapCommit() {
    return nonHeapCommit;
  }

  public long getNonHeapUsed() {
    return nonHeapUsed;
  }

  public SystemMetric(double cpuLoad, int cpuRunningThreads,
      long heapInit, long heapMax, long heapCommit, long heapUsed,
      long nonHeapInit, long nonHeapMax, long nonHeapCommit, long nonHeapUsed) {
    this.cpuLoad = cpuLoad;
    this.cpuRunningThreads = cpuRunningThreads;
    this.heapInit = heapInit;
    this.heapMax = heapMax;
    this.heapCommit = heapCommit;
    this.heapUsed = heapUsed;
    this.nonHeapInit = nonHeapInit;
    this.nonHeapMax = nonHeapMax;
    this.nonHeapCommit = nonHeapCommit;
    this.nonHeapUsed = nonHeapUsed;
  }
}
