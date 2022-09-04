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

package org.apache.servicecomb.edge.core;

import static org.apache.servicecomb.edge.core.DefaultEdgeDispatcher.MICROSERVICE_NAME;
import static org.apache.servicecomb.edge.core.DefaultEdgeDispatcher.VERSION;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RequestBody;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class TestDefaultEdgeDispatcher {
  @BeforeEach
  public void setUp() {

  }

  @AfterEach
  public void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testOnRequest() {
    DefaultEdgeDispatcher dispatcher = new DefaultEdgeDispatcher() {
      @Override
      protected boolean isFilterChainEnabled() {
        return false;
      }
    };
    Router router = Mockito.mock(Router.class);
    Route route = Mockito.mock(Route.class);
    RoutingContext context = Mockito.mock(RoutingContext.class);
    EdgeInvocation invocation = Mockito.mock(EdgeInvocation.class);
    dispatcher = Mockito.spy(dispatcher);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    Mockito.when(router.routeWithRegex(dispatcher.generateRouteRegex("api", true))).thenReturn(route);
    Mockito.when(route.handler(Mockito.any())).thenReturn(route);
    Mockito.when(route.failureHandler(Mockito.any())).thenReturn(route);
    RequestBody body = Mockito.mock(RequestBody.class);
    Mockito.when(context.body()).thenReturn(body);
    Mockito.when(body.buffer()).thenReturn(Mockito.mock(Buffer.class));
    Mockito.when(context.pathParam(MICROSERVICE_NAME)).thenReturn("testService");
    Mockito.when(context.pathParam(VERSION)).thenReturn("v1");
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.path()).thenReturn("/api/testService/v1/hello");
    Mockito.when(dispatcher.createEdgeInvocation()).thenReturn(invocation);
    invocation.setVersionRule("2.0.0+");
    invocation.init("serviceName", context, "/c/d/e", dispatcher.getHttpServerFilters());
    Mockito.doNothing().when(invocation).edgeInvoke();
    dispatcher.init(router);
    Assertions.assertFalse(dispatcher.enabled());
    Assertions.assertEquals(Utils.findActualPath("/api/test", 1), "/test");
    Assertions.assertEquals(dispatcher.getOrder(), 20000);

    try (MockedStatic<Vertx> vertxMockedStatic = Mockito.mockStatic(Vertx.class)) {
      vertxMockedStatic.when(Vertx::currentContext).thenReturn(Mockito.mock(Context.class));
      dispatcher.onRequest(context);
      // assert done in expectations.
    }
  }
}
