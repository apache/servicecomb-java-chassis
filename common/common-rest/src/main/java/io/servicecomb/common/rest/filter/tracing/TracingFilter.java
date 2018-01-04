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

package io.servicecomb.common.rest.filter.tracing;

import org.springframework.util.StringUtils;

import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.core.Const;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.tracing.BraveTraceIdGenerator;
import io.servicecomb.core.tracing.TraceIdGenerator;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import io.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import io.servicecomb.swagger.invocation.Response;

/**
 * Ensure the invocation contains traceId
 */
public class TracingFilter implements HttpServerFilter {
  private TraceIdGenerator traceIdGenerator = getTraceIdGenerator();

  @Override
  public int getOrder() {
    return 0;
  }

  /**
   * Ensure the invocation contains traceId
   * @return {@code null}
   */
  @Override
  public Response afterReceiveRequest(Invocation invocation, HttpServletRequestEx requestEx) {
    if (!StringUtils.isEmpty(invocation.getContext(Const.TRACE_ID_NAME))) {
      // if invocation context contains traceId, nothing needed to do
      return null;
    }

    String traceId = requestEx.getHeader(Const.TRACE_ID_NAME);
    if (!StringUtils.isEmpty(traceId)) {
      // if request header contains traceId, move traceId into invocation context
      invocation.addContext(Const.TRACE_ID_NAME, traceId);
      return null;
    }

    // if traceId not found, generate a traceId
    invocation.addContext(Const.TRACE_ID_NAME, traceIdGenerator.generateStringId());

    return null;
  }

  /**
   * nothing to do
   */
  @Override
  public void beforeSendResponse(Invocation invocation, HttpServletResponseEx responseEx) {
  }

  protected TraceIdGenerator getTraceIdGenerator() {
    return BraveTraceIdGenerator.INSTANCE;
  }
}
