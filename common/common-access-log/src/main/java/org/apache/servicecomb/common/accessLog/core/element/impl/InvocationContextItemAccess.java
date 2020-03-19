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

package org.apache.servicecomb.common.accessLog.core.element.impl;

import java.util.Map;

import org.apache.servicecomb.common.accessLog.core.element.AccessLogItem;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.springframework.util.StringUtils;

import io.vertx.ext.web.RoutingContext;

public class InvocationContextItemAccess implements AccessLogItem<RoutingContext> {

  public static final String NOT_FOUND = "-";

  String varName;

  public InvocationContextItemAccess(String varName) {
    this.varName = varName;
  }

  @Override
  public void appendServerFormattedItem(ServerAccessLogEvent accessLogEvent, StringBuilder builder) {
    String invocationContextValue = getValueFromInvocationContext(accessLogEvent);
    if (StringUtils.isEmpty(invocationContextValue)) {
      builder.append(NOT_FOUND);
      return;
    }
    builder.append(invocationContextValue);
  }

  @Override
  public void appendClientFormattedItem(InvocationFinishEvent finishEvent, StringBuilder builder) {
    Invocation invocation = finishEvent.getInvocation();
    if (null == invocation || invocation.getContext() == null
        || StringUtils.isEmpty(finishEvent.getInvocation().getContext().get(varName))) {
      builder.append(NOT_FOUND);
      return;
    }
    builder.append(finishEvent.getInvocation().getContext().get(varName));
  }


  protected String getValueFromInvocationContext(ServerAccessLogEvent accessLogEvent) {
    Map<String, Object> data = accessLogEvent.getRoutingContext().data();
    if (null == data || null == data.get(RestConst.REST_INVOCATION_CONTEXT)) {
      return null;
    }
    return ((Invocation) data.get(RestConst.REST_INVOCATION_CONTEXT)).getContext(varName);
  }

  public String getVarName() {
    return varName;
  }
}
