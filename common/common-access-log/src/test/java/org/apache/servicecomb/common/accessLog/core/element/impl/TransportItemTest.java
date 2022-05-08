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

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TransportItemTest {
  private static final TransportAccessItem ITEM = new TransportAccessItem();

  private StringBuilder strBuilder;

  private InvocationFinishEvent finishEvent;

  private ServerAccessLogEvent accessLogEvent;

  private Invocation invocation;

  private Endpoint endpoint;

  @BeforeEach
  public void initStrBuilder() {
    finishEvent = Mockito.mock(InvocationFinishEvent.class);
    invocation = Mockito.mock(Invocation.class);
    endpoint = Mockito.mock(Endpoint.class);
    accessLogEvent = new ServerAccessLogEvent();
    strBuilder = new StringBuilder();
  }

  @Test
  public void clientFormattedElement() {
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getConfigTransportName()).thenReturn("rest");
    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("rest", strBuilder.toString());

    strBuilder = new StringBuilder();
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getEndpoint()).thenReturn(endpoint);
    when(endpoint.getEndpoint()).thenReturn("rest:xxx:30100");
    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("rest", strBuilder.toString());
  }

  @Test
  public void serverFormattedElement() {
    ITEM.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("rest", strBuilder.toString());
  }

  @Test
  public void clientConfigTransportNameIsNull() {
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getConfigTransportName()).thenReturn(null);

    strBuilder = new StringBuilder();
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getEndpoint()).thenReturn(endpoint);
    when(endpoint.getEndpoint()).thenReturn("rest:xxx:30100");
    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("rest", strBuilder.toString());
  }

  @Test
  public void clientConfigTransportNameIsEmpty() {
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getConfigTransportName()).thenReturn("");
    strBuilder = new StringBuilder();
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getEndpoint()).thenReturn(endpoint);
    when(endpoint.getEndpoint()).thenReturn("rest:xxx:30100");
    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("rest", strBuilder.toString());
  }

  @Test
  public void clientALLIsEmpty() {
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getConfigTransportName()).thenReturn(null);
    strBuilder = new StringBuilder();
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getEndpoint()).thenReturn(null);
    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }
}
