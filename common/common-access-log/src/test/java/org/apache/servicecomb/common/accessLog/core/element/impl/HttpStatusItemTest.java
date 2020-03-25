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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class HttpStatusItemTest {

  private static final HttpStatusAccessItem STATUS_ELEMENT = new HttpStatusAccessItem();

  private StringBuilder strBuilder;

  private InvocationFinishEvent finishEvent;

  private ServerAccessLogEvent accessLogEvent;

  private RoutingContext routingContext;

  private Response response;

  private HttpServerResponse serverResponse;

  @Before
  public void initStrBuilder() {
    routingContext = Mockito.mock(RoutingContext.class);
    finishEvent = Mockito.mock(InvocationFinishEvent.class);
    response = Mockito.mock(Response.class);
    serverResponse = Mockito.mock(HttpServerResponse.class);

    accessLogEvent = new ServerAccessLogEvent();
    accessLogEvent.setRoutingContext(routingContext);
    strBuilder = new StringBuilder();
  }

  @Test
  public void serverFormattedElement() {
    int statusCode = 200;
    when(routingContext.response()).thenReturn(serverResponse);
    when(serverResponse.getStatusCode()).thenReturn(statusCode);

    STATUS_ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    assertEquals("200", strBuilder.toString());
  }

  @Test
  public void clientFormattedElement() {
    int statusCode = 200;
    when(finishEvent.getResponse()).thenReturn(response);
    when(response.getStatusCode()).thenReturn(statusCode);

    STATUS_ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    assertEquals("200", strBuilder.toString());
  }

  @Test
  public void serverFormattedElementOnResponseIsNull() {
    Mockito.when(routingContext.response()).thenReturn(null);
    STATUS_ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    assertEquals("-", strBuilder.toString());

    Mockito.when(routingContext.response()).thenReturn(serverResponse);
    Mockito.when(serverResponse.closed()).thenReturn(true);
    Mockito.when(serverResponse.ended()).thenReturn(false);

    strBuilder = new StringBuilder();
    STATUS_ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    assertEquals("-", strBuilder.toString());
  }

  @Test
  public void clientFormattedElementOnResponseIsNull() {
    when(finishEvent.getResponse()).thenReturn(null);
    STATUS_ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    assertEquals("-", strBuilder.toString());
  }
}
