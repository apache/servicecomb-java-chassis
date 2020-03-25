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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class ResponseSizeItemTest {
  private static final ResponseSizeAccessItem ELEMENT = new ResponseSizeAccessItem("0");

  private StringBuilder strBuilder;

  private InvocationFinishEvent finishEvent;

  private ServerAccessLogEvent accessLogEvent;

  private RoutingContext routingContext;

  private HttpServerResponse serverResponse;

  @Before
  public void initStrBuilder() {
    routingContext = Mockito.mock(RoutingContext.class);
    finishEvent = Mockito.mock(InvocationFinishEvent.class);
    serverResponse = Mockito.mock(HttpServerResponse.class);

    accessLogEvent = new ServerAccessLogEvent();
    accessLogEvent.setRoutingContext(routingContext);
    strBuilder = new StringBuilder();
  }

  @Test
  public void serverFormattedElement() {
    long bytesWritten = 16L;
    when(routingContext.response()).thenReturn(serverResponse);
    when(serverResponse.bytesWritten()).thenReturn(bytesWritten);

    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    assertEquals(String.valueOf(bytesWritten), strBuilder.toString());
  }

  @Test
  public void clientFormattedElement() {
    ELEMENT.appendClientFormattedItem(finishEvent, strBuilder);
    assertEquals("0", strBuilder.toString());
  }

  @Test
  public void getFormattedElementOnResponseIsNull() {
    when(routingContext.response()).thenReturn(null);
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    assertEquals("0", strBuilder.toString());
  }

  @Test
  public void getFormattedElementOnBytesWrittenIsZero() {
    long bytesWritten = 0L;
    when(routingContext.response()).thenReturn(serverResponse);
    when(serverResponse.bytesWritten()).thenReturn(bytesWritten);
    ELEMENT.appendServerFormattedItem(accessLogEvent, strBuilder);
    assertEquals("0", strBuilder.toString());
  }
}
