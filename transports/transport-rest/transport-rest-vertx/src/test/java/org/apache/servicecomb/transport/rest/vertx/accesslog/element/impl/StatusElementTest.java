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
import io.vertx.ext.web.RoutingContext;

public class StatusElementTest {

  public static final StatusElement STATUS_ELEMENT = new StatusElement();

  @Test
  public void getFormattedElement() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerResponse response = Mockito.mock(HttpServerResponse.class);
    int statusCode = 200;

    param.setRoutingContext(context);
    Mockito.when(context.response()).thenReturn(response);
    Mockito.when(response.getStatusCode()).thenReturn(statusCode);

    String result = STATUS_ELEMENT.getFormattedElement(param);

    assertEquals("200", result);
  }


  @Test
  public void getFormattedElementOnResponseIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);

    param.setRoutingContext(context);
    Mockito.when(context.response()).thenReturn(null);

    String result = STATUS_ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }
}
