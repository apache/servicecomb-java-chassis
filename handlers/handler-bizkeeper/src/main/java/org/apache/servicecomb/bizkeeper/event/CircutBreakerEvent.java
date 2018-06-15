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
package org.apache.servicecomb.bizkeeper.event;


import org.apache.servicecomb.bizkeeper.CustomizeCommandGroupKey;
import org.apache.servicecomb.foundation.common.event.AlarmEvent;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;

public class CircutBreakerEvent extends AlarmEvent {

  private String role;

  private String microservice;

  private String schema;

  private String operation;

  //当前总请求数
  private long currentTotalRequest;

  //当前请求出错计数
  private long currentErrorCount;

  //当前请求出错百分比
  private long currentErrorPercentage;

  private int requestVolumeThreshold;

  private int sleepWindowInMilliseconds;

  private int errorThresholdPercentage;

  public CircutBreakerEvent(HystrixCommandKey commandKey, Type type) {
    super(type);
    HystrixCommandMetrics hystrixCommandMetrics = HystrixCommandMetrics.getInstance(commandKey);
    if (hystrixCommandMetrics != null) {
      if (hystrixCommandMetrics.getCommandGroup() instanceof CustomizeCommandGroupKey) {
        CustomizeCommandGroupKey customCommandGroupKey =
            (CustomizeCommandGroupKey) hystrixCommandMetrics.getCommandGroup();
        this.microservice = customCommandGroupKey.getInstance().getMicroserviceName();
        this.role = customCommandGroupKey.getInstance().getInvocationType().name();
        this.schema = customCommandGroupKey.getInstance().getSchemaId();
        this.operation = customCommandGroupKey.getInstance().getOperationName();
      }
      this.currentTotalRequest = hystrixCommandMetrics.getHealthCounts().getTotalRequests();
      this.currentErrorPercentage = hystrixCommandMetrics.getHealthCounts().getErrorCount();
      this.currentErrorCount = hystrixCommandMetrics.getHealthCounts().getErrorPercentage();
      this.requestVolumeThreshold = hystrixCommandMetrics.getProperties().circuitBreakerRequestVolumeThreshold().get();
      this.sleepWindowInMilliseconds =
          hystrixCommandMetrics.getProperties().circuitBreakerSleepWindowInMilliseconds().get();
      this.errorThresholdPercentage =
          hystrixCommandMetrics.getProperties().circuitBreakerErrorThresholdPercentage().get();
    }
  }

  public String getRole() {
    return role;
  }

  public String getMicroservice() {
    return microservice;
  }

  public String getSchema() {
    return schema;
  }

  public String getOperation() {
    return operation;
  }

  public long getCurrentTotalRequest() {
    return currentTotalRequest;
  }

  public long getCurrentErrorCount() {
    return currentErrorCount;
  }

  public long getCurrentErrorPercentage() {
    return currentErrorPercentage;
  }

  public int getRequestVolumeThreshold() {
    return requestVolumeThreshold;
  }

  public int getSleepWindowInMilliseconds() {
    return sleepWindowInMilliseconds;
  }

  public int getErrorThresholdPercentage() {
    return errorThresholdPercentage;
  }
}
