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

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.ext.web.RoutingContext;

public class InvocationContextItemTest {

  public static final String INVOCATION_CONTEXT_KEY = "testKey";

  public static final String INVOCATION_CONTEXT_VALUE = "testValue";

  private static InvocationContextAccessItem ITEM = new InvocationContextAccessItem(INVOCATION_CONTEXT_KEY);

  private StringBuilder strBuilder;

  private InvocationFinishEvent finishEvent;

  private ServerAccessLogEvent accessLogEvent;

  private RoutingContext routingContext;

  private Invocation invocation;

  @Before
  public void initStrBuilder() {
    accessLogEvent = new ServerAccessLogEvent();
    routingContext = Mockito.mock(RoutingContext.class);
    finishEvent = Mockito.mock(InvocationFinishEvent.class);
    invocation = Mockito.mock(Invocation.class);

    accessLogEvent.setRoutingContext(routingContext);
    strBuilder = new StringBuilder();
  }

  @Test
  public void serverGetFormattedItem() {
    Map<String, Object> routingContextData = new HashMap<>();
    when(routingContext.data()).thenReturn(routingContextData);
    routingContextData.put(RestConst.REST_INVOCATION_CONTEXT, invocation);
    when(invocation.getContext(INVOCATION_CONTEXT_KEY)).thenReturn(INVOCATION_CONTEXT_VALUE);

    ITEM.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assert.assertThat(strBuilder.toString(), Matchers.is(INVOCATION_CONTEXT_VALUE));
  }

  @Test
  public void clientGetFormattedItem() {
    Map<String, String> context = new HashMap<>();
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getContext()).thenReturn(context);
    context.put(INVOCATION_CONTEXT_KEY, INVOCATION_CONTEXT_VALUE);

    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assert.assertThat(strBuilder.toString(), Matchers.is(INVOCATION_CONTEXT_VALUE));
  }

  @Test
  public void serverGetFormattedItemOnInvocationContextValueNotFound() {
    Map<String, Object> routingContextData = new HashMap<>();
    Invocation invocation = Mockito.mock(Invocation.class);
    when(routingContext.data()).thenReturn(routingContextData);
    routingContextData.put(RestConst.REST_INVOCATION_CONTEXT, invocation);
    when(invocation.getContext(INVOCATION_CONTEXT_KEY)).thenReturn(null);

    ITEM.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assert.assertThat(strBuilder.toString(), Matchers.is(InvocationContextAccessItem.NOT_FOUND));
  }

  @Test
  public void clientGetFormattedItemOnInvocationContextValueNotFound() {
    Map<String, String> context = new HashMap<>();
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getContext()).thenReturn(context);
    context.put(INVOCATION_CONTEXT_KEY, null);

    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assert.assertThat(strBuilder.toString(), Matchers.is(InvocationContextAccessItem.NOT_FOUND));
  }

  @Test
  public void serverGetFormattedItemOnInvocationNotFound() {
    Map<String, Object> routingContextData = new HashMap<>();
    when(routingContext.data()).thenReturn(routingContextData);

    ITEM.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assert.assertThat(strBuilder.toString(), Matchers.is(InvocationContextAccessItem.NOT_FOUND));
  }

  @Test
  public void clientGetFormattedItemOnInvocationContextNotFound() {
    Map<String, String> context = new HashMap<>();
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getContext()).thenReturn(context);

    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assert.assertThat(strBuilder.toString(), Matchers.is(InvocationContextAccessItem.NOT_FOUND));
  }

  @Test
  public void testGetFormattedItemOnRoutingContextDataNotFound() {
    when(routingContext.data()).thenReturn(null);
    ITEM.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assert.assertThat(strBuilder.toString(), Matchers.is(InvocationContextAccessItem.NOT_FOUND));
  }

  @Test
  public void clientGetFormattedItemOnRoutingContextDataNotFound() {
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getContext()).thenReturn(null);
    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assert.assertThat(strBuilder.toString(), Matchers.is(InvocationContextAccessItem.NOT_FOUND));
  }

  @Test
  public void clientGetFormattedItemOnInvocationNotFound() {
    when(finishEvent.getInvocation()).thenReturn(null);
    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assert.assertThat(strBuilder.toString(), Matchers.is(InvocationContextAccessItem.NOT_FOUND));
  }
}