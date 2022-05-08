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

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class TraceIdItemTest {
  private static final TraceIdAccessItem ELEMENT = new TraceIdAccessItem();

  private StringBuilder strBuilder;

  private InvocationFinishEvent finishEvent;

  private ServerAccessLogEvent accessLogEvent;

  private RoutingContext routingContext;

  private HttpServerRequest serverRequest;

  private Invocation invocation;

  private final Map<String, String> clientContext = new HashMap<>();

  @BeforeEach
  public void initStrBuilder() {
    accessLogEvent = new ServerAccessLogEvent();
    routingContext = Mockito.mock(RoutingContext.class);
    finishEvent = Mockito.mock(InvocationFinishEvent.class);
    invocation = Mockito.mock(Invocation.class);
    serverRequest = Mockito.mock(HttpServerRequest.class);

    accessLogEvent.setRoutingContext(routingContext);
    strBuilder = new StringBuilder();
    clientContext.clear();
  }

  @Test
  public void serverGetFormattedElementFromInvocationContext() {
    Map<String, Object> data = new HashMap<>();
    String traceIdTest = "traceIdTest";
    when(invocation.getContext(Const.TRACE_ID_NAME)).thenReturn(traceIdTest);
    when(routingContext.data()).thenReturn(data);
    data.put(RestConst.REST_INVOCATION_CONTEXT, invocation);

    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    MatcherAssert.assertThat(strBuilder.toString(), is(traceIdTest));
  }

  @Test
  public void clientGetFormattedElementFromInvocationContext() {
    String traceIdTest = "traceIdTest";
    clientContext.put(Const.TRACE_ID_NAME, traceIdTest);
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getContext()).thenReturn(clientContext);

    ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    MatcherAssert.assertThat(strBuilder.toString(), is(traceIdTest));
  }

  @Test
  public void serverGetFormattedElementFromRequestHeader() {
    Map<String, Object> data = new HashMap<>();
    String traceIdTest = "traceIdTest";
    when(invocation.getContext(Const.TRACE_ID_NAME)).thenReturn(null);
    when(routingContext.data()).thenReturn(data);
    data.put(RestConst.REST_INVOCATION_CONTEXT, invocation);

    when(serverRequest.getHeader(Const.TRACE_ID_NAME)).thenReturn(traceIdTest);
    when(routingContext.request()).thenReturn(serverRequest);
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    MatcherAssert.assertThat(strBuilder.toString(), is(traceIdTest));
  }

  @Test
  public void serverGetFormattedElementOnTraceIdNotFound() {
    Map<String, Object> data = new HashMap<>();
    when(invocation.getContext(Const.TRACE_ID_NAME)).thenReturn("");
    when(routingContext.data()).thenReturn(data);
    data.put(RestConst.REST_INVOCATION_CONTEXT, invocation);

    when(serverRequest.getHeader(Const.TRACE_ID_NAME)).thenReturn(null);
    when(routingContext.request()).thenReturn(serverRequest);
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    MatcherAssert.assertThat(strBuilder.toString(), is("-"));

    strBuilder = new StringBuilder();
    when(invocation.getContext(Const.TRACE_ID_NAME)).thenReturn(null);
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    MatcherAssert.assertThat(strBuilder.toString(), is("-"));
  }

  @Test
  public void clientGetFormattedElementOnTraceIdNotFound() {
    clientContext.put(Const.TRACE_ID_NAME, null);
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getContext()).thenReturn(clientContext);

    ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    MatcherAssert.assertThat(strBuilder.toString(), is("-"));
  }

  @Test
  public void serverGetFormattedElementOnInvocationContextIsNull() {
    when(routingContext.data()).thenReturn(null);
    when(routingContext.request()).thenReturn(serverRequest);
    when(serverRequest.getHeader(Const.TRACE_ID_NAME)).thenReturn(null);
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    MatcherAssert.assertThat(strBuilder.toString(), is("-"));
  }

  @Test
  public void clientGetFormattedElementOnInvocationContextIsNull() {
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getContext()).thenReturn(null);

    ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    MatcherAssert.assertThat(strBuilder.toString(), is("-"));
  }

  @Test
  public void serverGetFormattedElementOnDataIsNull() {
    when(serverRequest.getHeader(Const.TRACE_ID_NAME)).thenReturn(null);
    when(routingContext.request()).thenReturn(serverRequest);
    when(routingContext.data()).thenReturn(null);
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    MatcherAssert.assertThat(strBuilder.toString(), is("-"));
  }
}
