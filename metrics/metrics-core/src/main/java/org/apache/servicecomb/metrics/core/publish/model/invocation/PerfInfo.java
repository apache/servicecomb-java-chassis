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
  private double totalRequests;

  private double msTotalTime;

  private double msMaxLatency;

  public double getTotalRequests() {
    return totalRequests;
  }

  public void setTotalRequests(double totalRequests) {
    this.totalRequests = totalRequests;
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
    totalRequests += other.totalRequests;
    msTotalTime += other.msTotalTime;
    if (msMaxLatency < other.msMaxLatency) {
      msMaxLatency = other.msMaxLatency;
    }
  }

  public double calcMsLatency() {
    return (totalRequests != 0) ? msTotalTime / totalRequests : 0;
  }

  @Override
  public String toString() {
    return "PerfInfo [tps=" + totalRequests + ", msTotalTime=" + msTotalTime + ", msLatency=" + calcMsLatency()
        + ", msMaxLatency="
        + msMaxLatency + "]";
  }
}
