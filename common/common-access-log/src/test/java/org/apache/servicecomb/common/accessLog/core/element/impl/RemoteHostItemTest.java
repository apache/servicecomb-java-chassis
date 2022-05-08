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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RemoteHostItemTest {
  public static final RemoteHostAccessItem ELEMENT = new RemoteHostAccessItem();

  private StringBuilder strBuilder;

  private InvocationFinishEvent finishEvent;

  private ServerAccessLogEvent accessLogEvent;

  private RoutingContext routingContext;

  private Invocation invocation;

  private HttpServerRequest serverRequest;

  private Endpoint endpoint;

  private URIEndpointObject uriEndpointObject;

  private SocketAddress socketAddress;

  @BeforeEach
  public void initStrBuilder() {
    routingContext = mock(RoutingContext.class);
    finishEvent = mock(InvocationFinishEvent.class);
    invocation = mock(Invocation.class);
    serverRequest = mock(HttpServerRequest.class);
    endpoint = mock(Endpoint.class);
    uriEndpointObject = mock(URIEndpointObject.class);
    socketAddress = mock(SocketAddress.class);
    accessLogEvent = new ServerAccessLogEvent();
    accessLogEvent.setRoutingContext(routingContext);
    strBuilder = new StringBuilder();
  }

  @Test
  public void serverFormattedElement() {
    String remoteHost = "remoteHost";
    when(routingContext.request()).thenReturn(serverRequest);
    when(serverRequest.remoteAddress()).thenReturn(socketAddress);
    when(socketAddress.host()).thenReturn(remoteHost);

    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals(remoteHost, strBuilder.toString());
  }

  @Test
  public void clientFormattedElement() {
    String remoteHost = "remoteHost";
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getEndpoint()).thenReturn(endpoint);
    when(endpoint.getAddress()).thenReturn(uriEndpointObject);
    when(uriEndpointObject.getHostOrIp()).thenReturn(remoteHost);
    ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals(remoteHost, strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnRequestIsNull() {
    when(routingContext.request()).thenReturn(null);
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnRequestIsNull() {
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getEndpoint()).thenReturn(null);
    ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnRemoteAddressIsNull() {
    when(routingContext.request()).thenReturn(serverRequest);
    when(serverRequest.remoteAddress()).thenReturn(null);
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnRemoteAddressIsNull() {
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getEndpoint()).thenReturn(endpoint);
    when(endpoint.getAddress()).thenReturn(null);
    ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnHostIsNull() {
    when(routingContext.request()).thenReturn(serverRequest);
    when(serverRequest.remoteAddress()).thenReturn(socketAddress);
    when(socketAddress.host()).thenReturn(null);
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnHostIsNull() {
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getEndpoint()).thenReturn(endpoint);
    when(endpoint.getAddress()).thenReturn(uriEndpointObject);
    when(uriEndpointObject.getHostOrIp()).thenReturn(null);
    ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnHostIsEmpty() {
    when(routingContext.request()).thenReturn(serverRequest);
    when(serverRequest.remoteAddress()).thenReturn(socketAddress);
    when(socketAddress.host()).thenReturn("");
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnHostIsEmpty() {
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getEndpoint()).thenReturn(endpoint);
    when(endpoint.getAddress()).thenReturn(uriEndpointObject);
    when(uriEndpointObject.getHostOrIp()).thenReturn("");
    ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }
}
