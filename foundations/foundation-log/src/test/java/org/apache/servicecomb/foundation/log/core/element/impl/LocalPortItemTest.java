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

package org.apache.servicecomb.foundation.log.core.element.impl;

import static org.junit.Assert.assertEquals;

import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;

public class LocalPortItemTest {
  public static final LocalPortItem ELEMENT = new LocalPortItem();

  private StringBuilder strBuilder;

  private InvocationFinishEvent finishEvent;

  private ServerAccessLogEvent accessLogEvent;

  private RoutingContext routingContext;

  private HttpServerRequest serverRequest;

  private SocketAddress socketAddress;

  @Before
  public void initStrBuilder() {
    accessLogEvent = new ServerAccessLogEvent();
    routingContext = Mockito.mock(RoutingContext.class);
    finishEvent = Mockito.mock(InvocationFinishEvent.class);
    serverRequest = Mockito.mock(HttpServerRequest.class);
    socketAddress = Mockito.mock(SocketAddress.class);

    accessLogEvent.setRoutingContext(routingContext);
    strBuilder = new StringBuilder();
  }

  @Test
  public void serverFormattedElement() {
    Mockito.when(routingContext.request()).thenReturn(serverRequest);
    Mockito.when(serverRequest.localAddress()).thenReturn(socketAddress);
    Mockito.when(socketAddress.port()).thenReturn(8080);

    ELEMENT.appendFormattedItem(accessLogEvent, strBuilder);
    assertEquals("8080", strBuilder.toString());
  }

  @Test
  public void clientFormattedElement() {
    ELEMENT.appendFormattedItem(finishEvent, strBuilder);
    assertEquals("-", strBuilder.toString());
  }

  @Test
  public void getFormattedElementOnRequestIsNull() {
    Mockito.when(routingContext.request()).thenReturn(null);
    ELEMENT.appendFormattedItem(accessLogEvent, strBuilder);
    assertEquals("-", strBuilder.toString());
  }

  @Test
  public void getFormattedElementOnLocalAddressIsNull() {
    Mockito.when(routingContext.request()).thenReturn(serverRequest);
    Mockito.when(serverRequest.localAddress()).thenReturn(null);

    ELEMENT.appendFormattedItem(accessLogEvent, strBuilder);
    assertEquals("-", strBuilder.toString());
  }
}
