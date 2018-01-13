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

import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class MethodElementTest {

  @Test
  public void getFormattedElement() {
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    Mockito.when(routingContext.request()).thenReturn(request);
    Mockito.when(request.method()).thenReturn(HttpMethod.DELETE);
    AccessLogParam param = new AccessLogParam().setRoutingContext(routingContext);

    Assert.assertEquals("DELETE", new MethodElement().getFormattedElement(param));
  }

  @Test
  public void getFormattedElementOnRequestIsNull() {
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);
    AccessLogParam param = new AccessLogParam().setRoutingContext(routingContext);

    Mockito.when(routingContext.request()).thenReturn(null);

    Assert.assertEquals("-", new MethodElement().getFormattedElement(param));
  }

  @Test
  public void getFormattedElementOnMethodIsNull() {
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    AccessLogParam param = new AccessLogParam().setRoutingContext(routingContext);

    Mockito.when(routingContext.request()).thenReturn(request);
    Mockito.when(request.method()).thenReturn(null);

    Assert.assertEquals("-", new MethodElement().getFormattedElement(param));
  }
}
