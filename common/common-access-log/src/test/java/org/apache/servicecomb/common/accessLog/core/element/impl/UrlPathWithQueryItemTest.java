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

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class UrlPathWithQueryItemTest {

  public static final UrlPathWithQueryAccessItem ELEMENT = new UrlPathWithQueryAccessItem();

  private StringBuilder strBuilder;

  private InvocationFinishEvent finishEvent;

  private ServerAccessLogEvent accessLogEvent;

  private RoutingContext routingContext;

  private Invocation invocation;

  private HttpServerRequest serverRequest;

  @BeforeEach
  public void initStrBuilder() {
    accessLogEvent = new ServerAccessLogEvent();
    routingContext = Mockito.mock(RoutingContext.class);
    finishEvent = Mockito.mock(InvocationFinishEvent.class);
    invocation = Mockito.mock(Invocation.class);
    serverRequest = Mockito.mock(HttpServerRequest.class);

    accessLogEvent.setRoutingContext(routingContext);
    strBuilder = new StringBuilder();
  }

  @Test
  public void serverFormattedElement() {
    String uri = "uriTest";
    when(routingContext.request()).thenReturn(serverRequest);
    when(serverRequest.uri()).thenReturn(uri);
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals(uri, strBuilder.toString());
  }

  @Test
  public void clientFormattedElement() {
    String uri = "uriTest";
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getLocalContext(RestConst.REST_CLIENT_REQUEST_PATH)).thenReturn(uri);
    ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals(uri, strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnRequestIsNull() {
    when(routingContext.request()).thenReturn(null);
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnRequestIsNull() {
    when(finishEvent.getInvocation()).thenReturn(null);
    ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnUriIsNull() {
    when(routingContext.request()).thenReturn(serverRequest);
    when(serverRequest.uri()).thenReturn(null);
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnUriIsNull() {
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getLocalContext(RestConst.REST_CLIENT_REQUEST_PATH)).thenReturn(null);
    ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnUriIsEmpty() {
    when(routingContext.request()).thenReturn(serverRequest);
    when(serverRequest.uri()).thenReturn("");
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnUriIsEmpty() {
    when(finishEvent.getInvocation()).thenReturn(invocation);
    when(invocation.getLocalContext(RestConst.REST_CLIENT_REQUEST_PATH)).thenReturn("");
    ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    Assertions.assertEquals("-", strBuilder.toString());
  }
}
