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

package org.apache.servicecomb.metrics.common;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

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

  public SystemMetric(@JsonProperty("cpuLoad") double cpuLoad,
      @JsonProperty("cpuRunningThreads") int cpuRunningThreads,
      @JsonProperty("heapInit") long heapInit, @JsonProperty("heapMax") long heapMax,
      @JsonProperty("heapCommit") long heapCommit, @JsonProperty("heapUsed") long heapUsed,
      @JsonProperty("nonHeapInit") long nonHeapInit, @JsonProperty("nonHeapMax") long nonHeapMax,
      @JsonProperty("nonHeapCommit") long nonHeapCommit, @JsonProperty("nonHeapUsed") long nonHeapUsed) {
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

  public Map<String, Number> toMap() {
    String prefix = "servicecomb.instance.system";
    Map<String, Number> metrics = new HashMap<>();
    metrics.put(prefix + ".cpu.load", cpuLoad);
    metrics.put(prefix + ".cpu.runningThreads", cpuRunningThreads);
    metrics.put(prefix + ".heap.init", heapInit);
    metrics.put(prefix + ".heap.max", heapMax);
    metrics.put(prefix + ".heap.commit", heapCommit);
    metrics.put(prefix + ".heap.used", heapUsed);
    metrics.put(prefix + ".nonHeap.init", nonHeapInit);
    metrics.put(prefix + ".nonHeap.max", nonHeapMax);
    metrics.put(prefix + ".nonHeap.commit", nonHeapCommit);
    metrics.put(prefix + ".nonHeap.used", nonHeapUsed);
    return metrics;
  }
}
