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

import static org.hamcrest.core.Is.is;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.RestProducerInvocation;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import mockit.Deencapsulation;

public class TraceIdItemTest {
  private static final TraceIdItem ELEMENT = new TraceIdItem();

  @Test
  public void testGetFormattedElementFromInvocationContext() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);
    Map<String, Object> data = new HashMap<>();
    RestProducerInvocation restProducerInvocation = new RestProducerInvocation();
    Invocation invocation = Mockito.mock(Invocation.class);
    String traceIdTest = "traceIdTest";

    Mockito.when(invocation.getContext(Const.TRACE_ID_NAME)).thenReturn(traceIdTest);
    Deencapsulation.setField(restProducerInvocation, "invocation", invocation);
    Mockito.when(routingContext.data()).thenReturn(data);
    data.put("servicecomb-rest-producer-invocation", restProducerInvocation);

    param.setContextData(routingContext);

    String result = ELEMENT.getFormattedItem(param);
    Assert.assertThat(result, is(traceIdTest));
  }

  @Test
  public void testGetFormattedElementFromRequestHeader() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);
    Map<String, Object> data = new HashMap<>();
    RestProducerInvocation restProducerInvocation = new RestProducerInvocation();
    Invocation invocation = Mockito.mock(Invocation.class);
    String traceIdTest = "traceIdTest";

    Mockito.when(invocation.getContext(Const.TRACE_ID_NAME)).thenReturn(null);
    Deencapsulation.setField(restProducerInvocation, "invocation", invocation);
    Mockito.when(routingContext.data()).thenReturn(data);
    data.put("servicecomb-rest-producer-invocation", restProducerInvocation);

    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    Mockito.when(request.getHeader(Const.TRACE_ID_NAME)).thenReturn(traceIdTest);
    Mockito.when(routingContext.request()).thenReturn(request);

    param.setContextData(routingContext);

    String result = ELEMENT.getFormattedItem(param);
    Assert.assertThat(result, is(traceIdTest));
  }

  @Test
  public void testGetFormattedElementOnTraceIdNotFound() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);
    Map<String, Object> data = new HashMap<>();
    RestProducerInvocation restProducerInvocation = new RestProducerInvocation();
    Invocation invocation = Mockito.mock(Invocation.class);

    Mockito.when(invocation.getContext(Const.TRACE_ID_NAME)).thenReturn("");
    Deencapsulation.setField(restProducerInvocation, "invocation", invocation);
    Mockito.when(routingContext.data()).thenReturn(data);
    data.put("servicecomb-rest-producer-invocation", restProducerInvocation);

    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    Mockito.when(request.getHeader(Const.TRACE_ID_NAME)).thenReturn(null);
    Mockito.when(routingContext.request()).thenReturn(request);

    param.setContextData(routingContext);

    String result = ELEMENT.getFormattedItem(param);
    Assert.assertThat(result, is("-"));

    Mockito.when(invocation.getContext(Const.TRACE_ID_NAME)).thenReturn(null);
    result = ELEMENT.getFormattedItem(param);
    Assert.assertThat(result, is("-"));
  }

  @Test
  public void testGetFormattedElementOnInvocationContextIsNull() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);
    Map<String, Object> data = new HashMap<>();

    Mockito.when(routingContext.data()).thenReturn(data);

    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    Mockito.when(request.getHeader(Const.TRACE_ID_NAME)).thenReturn(null);
    Mockito.when(routingContext.request()).thenReturn(request);

    param.setContextData(routingContext);

    String result = ELEMENT.getFormattedItem(param);
    Assert.assertThat(result, is("-"));
  }

  @Test
  public void testGetFormattedElementOnDataIsNull() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);

    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    Mockito.when(request.getHeader(Const.TRACE_ID_NAME)).thenReturn(null);
    Mockito.when(routingContext.request()).thenReturn(request);

    param.setContextData(routingContext);
    Mockito.when(routingContext.data()).thenReturn(null);

    String result = ELEMENT.getFormattedItem(param);
    Assert.assertThat(result, is("-"));
  }
}
