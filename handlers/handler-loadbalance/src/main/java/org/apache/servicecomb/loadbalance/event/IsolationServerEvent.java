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

import java.util.HashMap;

import org.apache.servicecomb.foundation.common.event.AlarmEvent;

public class IsolationServerEvent extends AlarmEvent {

  private static int id = 1003;

  /**
   * msg部分字段说明：
   * currentTotalRequest:当前实例总请求数
   * currentCountinuousFailureCount:当前实例连续出错次数
   * currentErrorPercentage:当前实例出错百分比
   */
  public IsolationServerEvent(String microserviceName, long totalRequest, int currentCountinuousFailureCount,
      double currentErrorPercentage, int continuousFailureThreshold,
      int errorThresholdPercentage, long enableRequestThreshold, long singleTestTime, Type type) {
    super(type, id);
    HashMap<String, Object> msg = new HashMap<>();
    msg.put("microserviceName", microserviceName);
    msg.put("currentTotalRequest", totalRequest);
    msg.put("currentCountinuousFailureCount", currentCountinuousFailureCount);
    msg.put("currentErrorPercentage", currentErrorPercentage);
    msg.put("continuousFailureThreshold", continuousFailureThreshold);
    msg.put("errorThresholdPercentage", errorThresholdPercentage);
    msg.put("enableRequestThreshold", enableRequestThreshold);
    msg.put("singleTestTime", singleTestTime);
    super.setMsg(msg);
  }
}
