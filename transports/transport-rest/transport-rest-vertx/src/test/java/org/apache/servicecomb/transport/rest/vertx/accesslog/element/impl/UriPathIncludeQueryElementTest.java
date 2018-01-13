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
import io.vertx.ext.web.RoutingContext;

public class UriPathIncludeQueryElementTest {

  public static final UriPathIncludeQueryElement ELEMENT = new UriPathIncludeQueryElement();

  @Test
  public void getFormattedElement() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    String uri = "uriTest";

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.uri()).thenReturn(uri);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals(uri, result);
  }


  @Test
  public void getFormattedElementOnRequestIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(null);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }

  @Test
  public void getFormattedElementOnUriIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.uri()).thenReturn(null);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }


  @Test
  public void getFormattedElementOnUriIsEmpty() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    String uri = "";

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.uri()).thenReturn(uri);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }
}
