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
package org.apache.servicecomb.metrics.core.publish.model.invocation;

public class PerfInfo {
  private int tps;

  private double msTotalTime;

  private double msMaxLatency;

  public int getTps() {
    return tps;
  }

  public void setTps(int tps) {
    this.tps = tps;
  }

  public double getMsTotalTime() {
    return msTotalTime;
  }

  public void setMsTotalTime(double msTotalTime) {
    this.msTotalTime = msTotalTime;
  }

  public double getMsMaxLatency() {
    return msMaxLatency;
  }

  public void setMsMaxLatency(double msMaxLatency) {
    this.msMaxLatency = msMaxLatency;
  }

  public void add(PerfInfo other) {
    tps += other.tps;
    msTotalTime += other.msTotalTime;
    if (msMaxLatency < other.msMaxLatency) {
      msMaxLatency = other.msMaxLatency;
    }
  }

  public double calcMsLatency() {
    return (tps != 0) ? msTotalTime / tps : 0;
  }

  @Override
  public String toString() {
    return "PerfInfo [tps=" + tps + ", msTotalTime=" + msTotalTime + ", msLatency=" + calcMsLatency()
        + ", msMaxLatency="
        + msMaxLatency + "]";
  }
}
