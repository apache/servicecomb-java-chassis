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

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.headers.VertxHttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class ResponseHeaderItemTest {

  private static final String VAR_NAME = "varName";

  private static final ResponseHeaderItem ELEMENT = new ResponseHeaderItem(VAR_NAME);

  @Test
  public void getFormattedElement() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HttpServerResponse mockResponse = Mockito.mock(HttpServerResponse.class);
    VertxHttpHeaders headers = new VertxHttpHeaders();
    String headerValue = "headerValue";

    param.setContextData(mockContext);
    headers.add(VAR_NAME, headerValue);

    Mockito.when(mockContext.response()).thenReturn(mockResponse);
    Mockito.when(mockResponse.headers()).thenReturn(headers);

    String result = ELEMENT.getFormattedItem(param);

    assertEquals(headerValue, result);
    assertEquals(ELEMENT.getVarName(), VAR_NAME);
  }

  @Test
  public void getFormattedElementOnHeadersIsNull() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HttpServerResponse mockResponse = Mockito.mock(HttpServerResponse.class);

    param.setContextData(mockContext);

    Mockito.when(mockContext.response()).thenReturn(mockResponse);

    String result = ELEMENT.getFormattedItem(param);

    assertEquals("-", result);
  }

  @Test
  public void getFormattedElementOnResponseIsNull() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);

    param.setContextData(mockContext);

    Mockito.when(mockContext.response()).thenReturn(null);

    String result = ELEMENT.getFormattedItem(param);

    assertEquals("-", result);
  }

  @Test
  public void getFormattedElementOnNotFound() {
    AccessLogParam<RoutingContext> param = new AccessLogParam<>();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HttpServerResponse mockResponse = Mockito.mock(HttpServerResponse.class);
    VertxHttpHeaders headers = new VertxHttpHeaders();
    String headerValue = "headerValue";

    param.setContextData(mockContext);
    headers.add("anotherHeader", headerValue);

    Mockito.when(mockContext.response()).thenReturn(mockResponse);
    Mockito.when(mockResponse.headers()).thenReturn(headers);

    String result = ELEMENT.getFormattedItem(param);

    assertEquals("-", result);
  }
}
