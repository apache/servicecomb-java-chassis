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

/**
 * PerfResult
 *
 *
 */
public class PerfResult {
  private String name;

  private long callCount;

  private long msgCount;

  private long avgCallCount;

  private double msAvgLatency;

  private long[] msLatencySegments;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getCallCount() {
    return callCount;
  }

  public void setCallCount(long callCount) {
    this.callCount = callCount;
  }

  public long getMsgCount() {
    return msgCount;
  }

  public void setMsgCount(long msgCount) {
    this.msgCount = msgCount;
  }

  public long getAvgCallCount() {
    return avgCallCount;
  }

  public void setAvgCallCount(long avgCallCount) {
    this.avgCallCount = avgCallCount;
  }

  public double getMsAvgLatency() {
    return msAvgLatency;
  }

  public void setMsAvgLatency(double msAvgLatency) {
    this.msAvgLatency = msAvgLatency;
  }

  public long[] getMsLatencySegments() {
    return msLatencySegments;
  }

  public void setMsLatencySegments(long[] msLatencySegments) {
    this.msLatencySegments = msLatencySegments;
  }

  public String segmentsToString(String fmt) {
    StringBuilder sb = new StringBuilder();
    for (long segCount : msLatencySegments) {
      sb.append(String.format(fmt, segCount));
    }
    return sb.toString();
  }
}
