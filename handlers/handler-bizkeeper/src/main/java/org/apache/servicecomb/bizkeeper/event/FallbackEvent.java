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

import org.apache.servicecomb.bizkeeper.FallbackPolicy;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.event.AlarmEvent;

public class FallbackEvent extends AlarmEvent {

  private static int id = 1002;

  /**
   * msg部分字段说明：
   * invocationQualifiedName:当前调用的接口
   * policy:当前容错策略
   */
  public FallbackEvent(FallbackPolicy policy, Invocation invocation, Type type) {
    super(type, id);
    HashMap<String, Object> msg = new HashMap<>();
    msg.put("microserviceName", invocation.getMicroserviceName());
    msg.put("invocationQualifiedName", invocation.getInvocationQualifiedName());
    msg.put("policy", policy.name());
    super.setMsg(msg);
  }

}
