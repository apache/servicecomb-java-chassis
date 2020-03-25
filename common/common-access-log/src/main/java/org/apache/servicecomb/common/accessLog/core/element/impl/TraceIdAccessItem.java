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

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;

public class TraceIdAccessItem extends InvocationContextAccessItem {

  public static final String TRACE_ID = Const.TRACE_ID_NAME;

  public TraceIdAccessItem() {
    super(TRACE_ID);
  }

  @Override
  public void appendServerFormattedItem(ServerAccessLogEvent accessLogEvent, StringBuilder builder) {
    String traceId = getValueFromInvocationContext(accessLogEvent);
    if (StringUtils.isEmpty(traceId)) {
      traceId = accessLogEvent.getRoutingContext().request().getHeader(TRACE_ID);
    }
    builder.append(StringUtils.isEmpty(traceId) ? InvocationContextAccessItem.NOT_FOUND : traceId);
  }

  @Override
  public void appendClientFormattedItem(InvocationFinishEvent finishEvent, StringBuilder builder) {
    Invocation invocation = finishEvent.getInvocation();
    if (invocation == null || invocation.getContext() == null
        || StringUtils.isEmpty(invocation.getContext().get(TRACE_ID))) {
      builder.append(InvocationContextAccessItem.NOT_FOUND);
      return;
    }
    builder.append(invocation.getContext().get(TRACE_ID));
  }
}
