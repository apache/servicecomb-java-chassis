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

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.event.AlarmEvent;
import org.apache.servicecomb.loadbalance.ServiceCombServerStats;
import org.apache.servicecomb.loadbalance.filter.IsolationDiscoveryFilter;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

public class IsolationServerEvent extends AlarmEvent {

  private String microserviceName;

  private Endpoint endpoint;

  private MicroserviceInstance instance;

  //当前实例总请求数
  private long currentTotalRequest;

  //当前实例连续出错次数
  private long currentCountinuousFailureCount;

  //当前实例出错百分比
  private double currentErrorPercentage;

  private int minIsolationTime;

  private long enableRequestThreshold;

  private int continuousFailureThreshold;

  private int errorThresholdPercentage;

  private long singleTestTime;

  public IsolationServerEvent(Invocation invocation, MicroserviceInstance instance,
      ServiceCombServerStats serverStats,
      IsolationDiscoveryFilter.Settings settings, Type type, Endpoint endpoint) {
    super(type);
    this.microserviceName = invocation.getMicroserviceName();
    this.endpoint = endpoint;
    this.currentTotalRequest = serverStats.getTotalRequests();
    this.currentCountinuousFailureCount = serverStats.getCountinuousFailureCount();
    this.currentErrorPercentage = serverStats.getFailedRate();
    this.minIsolationTime = settings.minIsolationTime;
    this.enableRequestThreshold = settings.enableRequestThreshold;
    this.continuousFailureThreshold = settings.continuousFailureThreshold;
    this.errorThresholdPercentage = settings.errorThresholdPercentage;
    this.singleTestTime = settings.singleTestTime;
    this.instance = instance;
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

  public MicroserviceInstance getInstance() {
    return instance;
  }

  public int getMinIsolationTime() {
    return minIsolationTime;
  }

  public Endpoint getEndpoint() {
    return endpoint;
  }
}
