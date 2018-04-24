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

import java.util.HashMap;

import org.apache.servicecomb.bizkeeper.CommandKey;
import org.apache.servicecomb.bizkeeper.Configuration;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.event.AlarmEvent;
import com.netflix.hystrix.HystrixCommandMetrics;

public class CircutBreakerEvent extends AlarmEvent {

  private static int id = 1001;

  private HashMap<String, Object> msg = new HashMap<>();

  /**
   * msg部分字段说明：
   *   invocationQualifiedName:当前调用的接口
   *   currentTotalRequest:当前总请求数
   *   currentErrorCount:当前请求出错计数
   *   currentErrorPercentage:当前请求出错百分比
   */
  public CircutBreakerEvent(Invocation invocation, String groupname, Type type) {
    super(type, id);
    HystrixCommandMetrics hystrixCommandMetrics =
        HystrixCommandMetrics.getInstance(CommandKey.toHystrixCommandKey(groupname, invocation));
    String microserviceName = invocation.getMicroserviceName();
    String invocationQualifiedName = invocation.getInvocationQualifiedName();
    msg.put("microserviceName", microserviceName);
    msg.put("invocationQualifiedName", invocationQualifiedName);
    if (hystrixCommandMetrics != null) {
      msg.put("currentTotalRequest", hystrixCommandMetrics.getHealthCounts().getTotalRequests());
      msg.put("currentErrorCount", hystrixCommandMetrics.getHealthCounts().getErrorCount());
      msg.put("currentErrorPercentage", hystrixCommandMetrics.getHealthCounts().getErrorPercentage());
    }
    msg.put("requestVolumeThreshold",
        Configuration.INSTANCE.getCircuitBreakerRequestVolumeThreshold(groupname,
            microserviceName,
            invocationQualifiedName));
    msg.put("sleepWindowInMilliseconds",
        Configuration.INSTANCE.getCircuitBreakerSleepWindowInMilliseconds(groupname,
            microserviceName,
            invocationQualifiedName));
    msg.put("errorThresholdPercentage",
        Configuration.INSTANCE
            .getCircuitBreakerErrorThresholdPercentage(groupname, microserviceName, invocationQualifiedName));
    super.setMsg(msg);
  }
}
