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

import static org.junit.Assert.assertEquals;

import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;

public class RemoteHostItemTest {

  public static final RemoteHostItem ELEMENT = new RemoteHostItem();

  @Test
  public void getFormattedElement() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    SocketAddress address = Mockito.mock(SocketAddress.class);
    String remoteHost = "remoteHost";

    param.setContextData(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.remoteAddress()).thenReturn(address);
    Mockito.when(address.host()).thenReturn(remoteHost);

    String result = ELEMENT.getFormattedItem(param);

    assertEquals(remoteHost, result);
  }

  @Test
  public void getFormattedElementOnRequestIsNull() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext context = Mockito.mock(RoutingContext.class);

    param.setContextData(context);
    Mockito.when(context.request()).thenReturn(null);

    String result = ELEMENT.getFormattedItem(param);

    assertEquals("-", result);
  }


  @Test
  public void getFormattedElementOnRemoteAddressIsNull() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);

    param.setContextData(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.remoteAddress()).thenReturn(null);

    String result = ELEMENT.getFormattedItem(param);

    assertEquals("-", result);
  }


  @Test
  public void getFormattedElementOnHostIsNull() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    SocketAddress address = Mockito.mock(SocketAddress.class);

    param.setContextData(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.remoteAddress()).thenReturn(address);
    Mockito.when(address.host()).thenReturn(null);

    String result = ELEMENT.getFormattedItem(param);

    assertEquals("-", result);
  }


  @Test
  public void getFormattedElementOnHostIsEmpty() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    SocketAddress address = Mockito.mock(SocketAddress.class);
    String remoteHost = "";

    param.setContextData(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.remoteAddress()).thenReturn(address);
    Mockito.when(address.host()).thenReturn(remoteHost);

    String result = ELEMENT.getFormattedItem(param);

    assertEquals("-", result);
  }
}
