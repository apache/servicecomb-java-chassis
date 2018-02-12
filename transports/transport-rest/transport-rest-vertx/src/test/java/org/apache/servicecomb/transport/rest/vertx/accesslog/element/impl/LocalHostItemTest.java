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

public class LocalHostItemTest {

  public static final LocalHostItem ELEMENT = new LocalHostItem();

  @Test
  public void getFormattedElement() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    SocketAddress localAddress = Mockito.mock(SocketAddress.class);
    String localHost = "testHost";

    param.setContextData(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.localAddress()).thenReturn(localAddress);
    Mockito.when(localAddress.host()).thenReturn(localHost);

    String result = ELEMENT.getFormattedItem(param);

    assertEquals(localHost, result);
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
  public void getFormattedElementOnLocalAddressIsNull() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);

    param.setContextData(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.localAddress()).thenReturn(null);

    String result = ELEMENT.getFormattedItem(param);

    assertEquals("-", result);
  }

  @Test
  public void getFormattedElementOnHostIsNull() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    SocketAddress localAddress = Mockito.mock(SocketAddress.class);

    param.setContextData(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.localAddress()).thenReturn(localAddress);
    Mockito.when(localAddress.host()).thenReturn(null);

    String result = ELEMENT.getFormattedItem(param);

    assertEquals("-", result);
  }

  @Test
  public void getFormattedElementIsEmpty() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    SocketAddress localAddress = Mockito.mock(SocketAddress.class);
    String localHost = "";

    param.setContextData(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.localAddress()).thenReturn(localAddress);
    Mockito.when(localAddress.host()).thenReturn(localHost);

    String result = ELEMENT.getFormattedItem(param);

    assertEquals("-", result);
  }
}
