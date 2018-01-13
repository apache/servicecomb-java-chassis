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

import java.util.Arrays;

/**
 * PerfStatData
 *
 *
 */
public class PerfStatData {
  private static long processBegin = System.currentTimeMillis();

  private static long[] segmentDef;

  private static final long[] SEGMENT_BOUNDRYS = new long[] {20, 100, 300, 500};

  static {
    setSegmentDef(SEGMENT_BOUNDRYS);
  }

  //CodeDEX 不允许public static 非final的成员
  private static String strSegmentDef;

  private String name;

  private long callCount;

  private long msgCount;

  private long msLatency;

  private long[] msLatencySegments;

  private static final int MILLI_COUNT_IN_SECOND = 1000;

  public PerfStatData(String name) {
    msLatencySegments = new long[segmentDef.length + 1];
    this.name = name;
  }

  public static void setSegmentDef(long[] segmentDef) {
    PerfStatData.segmentDef = segmentDef;

    StringBuilder sb = new StringBuilder();
    long last = 0;
    for (long def : segmentDef) {
      sb.append(String.format("%-10s", String.format("[%d,%d)", last, def)));
      last = def;
    }
    sb.append(String.format("%-10s", String.format("[%d,...)", last)));
    strSegmentDef = sb.toString();
  }

  public static String getStrSegmentDef() {
    return strSegmentDef;
  }

  public String getName() {
    return name;
  }

  public long getCallCount() {
    return callCount;
  }

  public long getMsgCount() {
    return msgCount;
  }

  public long getMsLatency() {
    return msLatency;
  }

  public long[] getMsLatencySegments() {
    return msLatencySegments;
  }

  protected int findSegmentIdx(long latency) {
    long lastDef = 0;
    for (int idx = 0; idx < segmentDef.length; idx++) {
      long def = segmentDef[idx];
      if (latency >= lastDef && latency < def) {
        return idx;
      }

      lastDef = def;
    }

    return segmentDef.length;
  }

  public void add(int count, long latency) {
    this.callCount++;
    this.msgCount += count;
    this.msLatency += latency;

    int segmentIdx = findSegmentIdx(latency);
    this.msLatencySegments[segmentIdx]++;
  }

  public void add(PerfStatContext context) {
    add(context.getMsgCount(), context.getLatency());
  }

  public void mergeFrom(PerfStatData other) {
    callCount += other.callCount;
    msgCount += other.msgCount;
    msLatency += other.msLatency;
    for (int idx = 0; idx < msLatencySegments.length; idx++) {
      msLatencySegments[idx] += other.msLatencySegments[idx];
    }
  }

  public PerfResult calc(long msNow) {
    PerfResult perf = new PerfResult();
    perf.setName("  all " + name + "  :");
    perf.setCallCount(callCount);
    perf.setMsgCount(msgCount);
    perf.setAvgCallCount(
        callCount * MILLI_COUNT_IN_SECOND / (msNow - processBegin > 0 ? msNow - processBegin : 1));
    perf.setMsAvgLatency((callCount != 0) ? (double) msLatency / callCount : 0);
    perf.setMsLatencySegments(msLatencySegments);
    return perf;
  }

  public PerfResult calc(PerfStatData lastCycle, long msCycle) {
    PerfResult perf = new PerfResult();

    long diffCount = callCount - lastCycle.callCount;
    perf.setName("  cycle " + name + ":");
    perf.setCallCount(diffCount);
    perf.setMsgCount(msgCount - lastCycle.msgCount);
    perf.setAvgCallCount(diffCount * MILLI_COUNT_IN_SECOND / msCycle);
    perf.setMsAvgLatency((diffCount != 0) ? (double) (msLatency - lastCycle.msLatency) / diffCount : 0);

    long[] clone = Arrays.copyOf(msLatencySegments, msLatencySegments.length);
    long[] lastCycleSegments = lastCycle.getMsLatencySegments();
    for (int idx = 0; idx < clone.length; idx++) {
      clone[idx] -= lastCycleSegments[idx];
    }
    perf.setMsLatencySegments(clone);
    return perf;
  }
}
