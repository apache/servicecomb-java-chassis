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

package org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl;

import java.util.Map;

import org.apache.servicecomb.common.rest.AbstractRestInvocation;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;
import org.springframework.util.StringUtils;

import io.vertx.ext.web.RoutingContext;

public class TraceIdItem implements AccessLogItem<RoutingContext> {

  private static final String TRACE_ID_NOT_FOUND = "-";

  @Override
  public String getFormattedItem(AccessLogParam<RoutingContext> accessLogParam) {
    String traceId = getTraceIdFromInvocationContext(accessLogParam);
    if (StringUtils.isEmpty(traceId)) {
      traceId = accessLogParam.getContextData().request().getHeader(Const.TRACE_ID_NAME);
    }

    if (StringUtils.isEmpty(traceId)) {
      return TRACE_ID_NOT_FOUND;
    }

    return traceId;
  }

  private String getTraceIdFromInvocationContext(AccessLogParam<RoutingContext> accessLogParam) {
    Map<String, Object> data = accessLogParam.getContextData().data();
    if (null == data) {
      return null;
    }

    AbstractRestInvocation invocation = (AbstractRestInvocation) data
        .get("servicecomb-rest-producer-invocation");
    if (null == invocation) {
      return null;
    }

    String traceId = invocation.getContext("X-B3-TraceId");

    if (StringUtils.isEmpty(traceId)) {
      return null;
    }
    return traceId;
  }
}
