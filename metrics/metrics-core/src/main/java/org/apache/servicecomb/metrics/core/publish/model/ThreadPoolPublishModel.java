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
package org.apache.servicecomb.metrics.core.publish.model;

public class ThreadPoolPublishModel {
  private double avgTaskCount;

  private double avgCompletedTaskCount;

  private int currentThreadsBusy;

  private int maxThreads;

  private int poolSize;

  private int corePoolSize;

  private int queueSize;

  public double getAvgTaskCount() {
    return avgTaskCount;
  }

  public void setAvgTaskCount(double avgTaskCount) {
    this.avgTaskCount = avgTaskCount;
  }

  public double getAvgCompletedTaskCount() {
    return avgCompletedTaskCount;
  }

  public void setAvgCompletedTaskCount(double avgCompletedTaskCount) {
    this.avgCompletedTaskCount = avgCompletedTaskCount;
  }

  public int getCurrentThreadsBusy() {
    return currentThreadsBusy;
  }

  public void setCurrentThreadsBusy(int currentThreadsBusy) {
    this.currentThreadsBusy = currentThreadsBusy;
  }

  public int getMaxThreads() {
    return maxThreads;
  }

  public void setMaxThreads(int maxThreads) {
    this.maxThreads = maxThreads;
  }

  public int getPoolSize() {
    return poolSize;
  }

  public void setPoolSize(int poolSize) {
    this.poolSize = poolSize;
  }

  public int getCorePoolSize() {
    return corePoolSize;
  }

  public void setCorePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  public int getQueueSize() {
    return queueSize;
  }

  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }
}
