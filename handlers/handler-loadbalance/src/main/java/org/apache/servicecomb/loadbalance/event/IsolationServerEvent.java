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
package org.apache.servicecomb.loadbalance.event;

import org.apache.servicecomb.foundation.common.event.AlarmEvent;

public class IsolationServerEvent extends AlarmEvent {

  private String microserviceName;

  //当前实例总请求数
  private long currentTotalRequest;

  //当前实例连续出错次数
  private long currentCountinuousFailureCount;

  //当前实例出错百分比
  private double currentErrorPercentage;

  private long enableRequestThreshold;

  private int continuousFailureThreshold;

  private int errorThresholdPercentage;

  private long singleTestTime;

  public IsolationServerEvent(String microserviceName, long totalRequest, long currentCountinuousFailureCount,
      double currentErrorPercentage, int continuousFailureThreshold,
      int errorThresholdPercentage, long enableRequestThreshold, long singleTestTime, Type type) {
    super(type);
    this.microserviceName = microserviceName;
    this.currentTotalRequest = totalRequest;
    this.currentCountinuousFailureCount = currentCountinuousFailureCount;
    this.currentErrorPercentage = currentErrorPercentage;
    this.enableRequestThreshold = enableRequestThreshold;
    this.continuousFailureThreshold = continuousFailureThreshold;
    this.errorThresholdPercentage = errorThresholdPercentage;
    this.singleTestTime = singleTestTime;
  }

  public String getMicroserviceName() {
    return microserviceName;
  }

  public long getCurrentTotalRequest() {
    return currentTotalRequest;
  }

  public long getCurrentCountinuousFailureCount() {
    return currentCountinuousFailureCount;
  }

  public double getCurrentErrorPercentage() {
    return currentErrorPercentage;
  }

  public long getEnableRequestThreshold() {
    return enableRequestThreshold;
  }

  public int getContinuousFailureThreshold() {
    return continuousFailureThreshold;
  }

  public int getErrorThresholdPercentage() {
    return errorThresholdPercentage;
  }

  public long getSingleTestTime() {
    return singleTestTime;
  }
}
