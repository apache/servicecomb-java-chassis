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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.ext.web.RoutingContext;

public class InvocationContextItemTest {

  public static final String INVOCATION_CONTEXT_KEY = "testKey";

  private static InvocationContextItem ITEM = new InvocationContextItem(INVOCATION_CONTEXT_KEY);

  @Test
  public void testGetFormattedItem() {
    AccessLogParam<RoutingContext> accessLogParam = new AccessLogParam<>();
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);
    Map<String, Object> routingContextData = new HashMap<>();
    Invocation invocation = Mockito.mock(Invocation.class);
    String testValue = "testValue";

    accessLogParam.setContextData(routingContext);
    Mockito.when(routingContext.data()).thenReturn(routingContextData);
    routingContextData.put(RestConst.REST_INVOCATION_CONTEXT, invocation);
    Mockito.when(invocation.getContext(INVOCATION_CONTEXT_KEY)).thenReturn(testValue);

    String result = ITEM.getFormattedItem(accessLogParam);

    Assert.assertThat(result, Matchers.is(testValue));
  }

  @Test
  public void testGetFormattedItemOnInvocationContextValueNotFound() {
    AccessLogParam<RoutingContext> accessLogParam = new AccessLogParam<>();
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);
    Map<String, Object> routingContextData = new HashMap<>();
    Invocation invocation = Mockito.mock(Invocation.class);

    accessLogParam.setContextData(routingContext);
    Mockito.when(routingContext.data()).thenReturn(routingContextData);
    routingContextData.put(RestConst.REST_INVOCATION_CONTEXT, invocation);
    Mockito.when(invocation.getContext(INVOCATION_CONTEXT_KEY)).thenReturn(null);

    String result = ITEM.getFormattedItem(accessLogParam);

    Assert.assertThat(result, Matchers.is(InvocationContextItem.NOT_FOUND));
  }

  @Test
  public void testGetFormattedItemOnInvocationNotFound() {
    AccessLogParam<RoutingContext> accessLogParam = new AccessLogParam<>();
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);
    Map<String, Object> routingContextData = new HashMap<>();

    accessLogParam.setContextData(routingContext);
    Mockito.when(routingContext.data()).thenReturn(routingContextData);

    String result = ITEM.getFormattedItem(accessLogParam);

    Assert.assertThat(result, Matchers.is(InvocationContextItem.NOT_FOUND));
  }

  @Test
  public void testGetFormattedItemOnRoutingContextDataNotFound() {
    AccessLogParam<RoutingContext> accessLogParam = new AccessLogParam<>();
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);

    accessLogParam.setContextData(routingContext);
    Mockito.when(routingContext.data()).thenReturn(null);

    String result = ITEM.getFormattedItem(accessLogParam);

    Assert.assertThat(result, Matchers.is(InvocationContextItem.NOT_FOUND));
  }
}