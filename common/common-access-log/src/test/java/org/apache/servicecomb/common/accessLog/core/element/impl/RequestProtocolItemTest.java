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
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.RoutingContext;

public class RequestProtocolItemTest {
  private static final RequestProtocolAccessItem ITEM = new RequestProtocolAccessItem();

  private StringBuilder strBuilder;

  private InvocationFinishEvent finishEvent;

  private ServerAccessLogEvent accessLogEvent;

  private RoutingContext routingContext;

  private Invocation invocation;

  private HttpServerRequest serverRequest;

  private Endpoint endpoint;

  private URIEndpointObject urlEndpoint;

  @BeforeEach
  public void initStrBuilder() {
    routingContext = Mockito.mock(RoutingContext.class);
    finishEvent = Mockito.mock(InvocationFinishEvent.class);
    invocation = Mockito.mock(Invocation.class);
    serverRequest = Mockito.mock(HttpServerRequest.class);
    urlEndpoint = Mockito.mock(URIEndpointObject.class);
    endpoint = Mockito.mock(Endpoint.class);

    accessLogEvent = new ServerAccessLogEvent();
    accessLogEvent.setRoutingContext(routingContext);
    strBuilder = new StringBuilder();
  }

  @Test
  public void serverFormattedElement() {
    when(routingContext.request()).thenReturn(serverRequest);
    when(serverRequest.version()).thenReturn(HttpVersion.HTTP_1_1);

    ITEM.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("HTTP/1.1", strBuilder.toString());

    strBuilder = new StringBuilder();
    when(serverRequest.version()).thenReturn(HttpVersion.HTTP_1_0);
    ITEM.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("HTTP/1.0", strBuilder.toString());

    strBuilder = new StringBuilder();
    when(serverRequest.version()).thenReturn(HttpVersion.HTTP_2);
    ITEM.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("HTTP/2.0", strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnRequestIsNull() {
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getEndpoint()).thenReturn(endpoint);
    when(endpoint.getAddress()).thenReturn(urlEndpoint);
    when(urlEndpoint.isHttp2Enabled()).thenReturn(true);
    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("HTTP/2.0", strBuilder.toString());

    strBuilder = new StringBuilder();
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getEndpoint()).thenReturn(endpoint);
    when(endpoint.getAddress()).thenReturn(urlEndpoint);
    when(urlEndpoint.isHttp2Enabled()).thenReturn(false);
    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("HTTP/1.1", strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnVersionIsNull() {
    when(routingContext.request()).thenReturn(serverRequest);
    when(serverRequest.version()).thenReturn(null);

    ITEM.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnVersionIsNull() {
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getEndpoint()).thenReturn(endpoint);
    when(endpoint.getAddress()).thenReturn(null);

    ITEM.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("HTTP/1.1", strBuilder.toString());
  }
}
