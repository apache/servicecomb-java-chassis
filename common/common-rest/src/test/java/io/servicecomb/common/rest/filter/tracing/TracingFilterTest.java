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

import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.core.Const;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.tracing.TraceIdGenerator;
import io.servicecomb.foundation.vertx.http.HttpServletRequestEx;

public class TracingFilterTest {
  private static final TestTracingFilter FILTER = new TestTracingFilter();

  @Test
  public void testAfterReceiveRequestOnInvocationContainsTraceId() {
    Invocation invocation = Mockito.mock(Invocation.class);
    String traceId = "traceIdTest";
    HttpServletRequestEx requestEx = Mockito.mock(HttpServletRequestEx.class);

    Mockito.when(invocation.getContext(Const.TRACE_ID_NAME)).thenReturn(traceId);

    FILTER.afterReceiveRequest(invocation, requestEx);

    Mockito.verify(requestEx, Mockito.times(0)).getHeader(Const.TRACE_ID_NAME);
  }

  @Test
  public void testAfterReceiveRequestOnHeaderContainsTraceId() {
    Invocation invocation = Mockito.mock(Invocation.class);
    String traceId = "traceIdTest";
    HttpServletRequestEx requestEx = Mockito.mock(HttpServletRequestEx.class);

    Mockito.when(invocation.getContext(Const.TRACE_ID_NAME)).thenReturn(null);
    Mockito.when(requestEx.getHeader(Const.TRACE_ID_NAME)).thenReturn(traceId);

    FILTER.afterReceiveRequest(invocation, requestEx);

    Mockito.verify(invocation).addContext(Const.TRACE_ID_NAME, traceId);
  }

  @Test
  public void testAfterReceiveRequestOnGenerateTraceId() {
    Invocation invocation = Mockito.mock(Invocation.class);
    HttpServletRequestEx requestEx = Mockito.mock(HttpServletRequestEx.class);

    Mockito.when(invocation.getContext(Const.TRACE_ID_NAME)).thenReturn(null);
    Mockito.when(requestEx.getHeader(Const.TRACE_ID_NAME)).thenReturn(null);

    FILTER.afterReceiveRequest(invocation, requestEx);

    Mockito.verify(invocation).addContext(Const.TRACE_ID_NAME, TestTracingFilter.TRACE_ID);
  }

  static class TestTracingFilter extends TracingFilter {

    static final String TRACE_ID = "" + Long.MAX_VALUE;

    @Override
    protected TraceIdGenerator getTraceIdGenerator() {
      return () -> TRACE_ID;
    }
  }
}