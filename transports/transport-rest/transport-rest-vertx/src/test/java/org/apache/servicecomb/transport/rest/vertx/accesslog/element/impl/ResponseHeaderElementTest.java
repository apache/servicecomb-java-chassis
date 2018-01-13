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

public class ResponseHeaderElementTest {

  private static final String IDENTIFIER = "identifier";

  private static final ResponseHeaderElement ELEMENT = new ResponseHeaderElement(IDENTIFIER);

  @Test
  public void getFormattedElement() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HttpServerResponse mockResponse = Mockito.mock(HttpServerResponse.class);
    VertxHttpHeaders headers = new VertxHttpHeaders();
    String headerValue = "headerValue";

    param.setRoutingContext(mockContext);
    headers.add(IDENTIFIER, headerValue);

    Mockito.when(mockContext.response()).thenReturn(mockResponse);
    Mockito.when(mockResponse.headers()).thenReturn(headers);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals(headerValue, result);
    assertEquals(ELEMENT.getIdentifier(), IDENTIFIER);
  }

  @Test
  public void getFormattedElementOnHeadersIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HttpServerResponse mockResponse = Mockito.mock(HttpServerResponse.class);

    param.setRoutingContext(mockContext);

    Mockito.when(mockContext.response()).thenReturn(mockResponse);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }

  @Test
  public void getFormattedElementOnResponseIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);

    param.setRoutingContext(mockContext);

    Mockito.when(mockContext.response()).thenReturn(null);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }

  @Test
  public void getFormattedElementOnNotFound() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HttpServerResponse mockResponse = Mockito.mock(HttpServerResponse.class);
    VertxHttpHeaders headers = new VertxHttpHeaders();
    String headerValue = "headerValue";

    param.setRoutingContext(mockContext);
    headers.add("anotherHeader", headerValue);

    Mockito.when(mockContext.response()).thenReturn(mockResponse);
    Mockito.when(mockResponse.headers()).thenReturn(headers);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }
}
