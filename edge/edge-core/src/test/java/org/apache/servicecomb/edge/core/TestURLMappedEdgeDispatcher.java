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

import java.util.Map;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RequestBody;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.transport.rest.vertx.RestBodyHandler;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class TestURLMappedEdgeDispatcher {
  @BeforeEach
  public void setUp() throws Exception {
  }

  @AfterEach
  public void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testConfigurations() {
    ArchaiusUtils.setProperty("servicecomb.http.dispatcher.edge.url.enabled", true);

    URLMappedEdgeDispatcher dispatcher = new URLMappedEdgeDispatcher();
    Map<String, URLMappedConfigurationItem> items = dispatcher.getConfigurations();
    Assertions.assertEquals(items.size(), 0);

    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    EdgeInvocation invocation = Mockito.mock(EdgeInvocation.class);
    Mockito.when(context.get(RestBodyHandler.BYPASS_BODY_HANDLER)).thenReturn(Boolean.TRUE);
    dispatcher.onRequest(context);

    ArchaiusUtils.setProperty("servicecomb.http.dispatcher.edge.url.mappings.service1.path", "/a/b/c/.*");
    ArchaiusUtils.setProperty("servicecomb.http.dispatcher.edge.url.mappings.service1.microserviceName", "serviceName");
    ArchaiusUtils.setProperty("servicecomb.http.dispatcher.edge.url.mappings.service1.prefixSegmentCount", 2);
    ArchaiusUtils.setProperty("servicecomb.http.dispatcher.edge.url.mappings.service1.versionRule", "2.0.0+");
    items = dispatcher.getConfigurations();
    Assertions.assertEquals(items.size(), 1);
    URLMappedConfigurationItem item = items.get("service1");
    Assertions.assertEquals(item.getMicroserviceName(), "serviceName");
    Assertions.assertEquals(item.getPrefixSegmentCount(), 2);
    Assertions.assertEquals(item.getStringPattern(), "/a/b/c/.*");
    Assertions.assertEquals(item.getVersionRule(), "2.0.0+");

    ArchaiusUtils.setProperty("servicecomb.http.dispatcher.edge.url.mappings.service2.versionRule", "2.0.0+");
    ArchaiusUtils.setProperty("servicecomb.http.dispatcher.edge.url.mappings.service3.path", "/b/c/d/.*");
    items = dispatcher.getConfigurations();
    Assertions.assertEquals(items.size(), 1);
    item = items.get("service1");
    Assertions.assertEquals(item.getMicroserviceName(), "serviceName");
    Assertions.assertEquals(item.getPrefixSegmentCount(), 2);
    Assertions.assertEquals(item.getStringPattern(), "/a/b/c/.*");
    Assertions.assertEquals(item.getVersionRule(), "2.0.0+");

    URLMappedConfigurationItem finalItem = item;
    try (MockedStatic<Vertx> vertxMockedStatic = Mockito.mockStatic(Vertx.class)) {
      vertxMockedStatic.when(Vertx::currentContext).thenReturn(Mockito.mock(Context.class));
      dispatcher = Mockito.spy(dispatcher);
      RequestBody body = Mockito.mock(RequestBody.class);
      Mockito.when(context.body()).thenReturn(body);
      Mockito.when(body.buffer()).thenReturn(Mockito.mock(Buffer.class));
      Mockito.when(context.get(RestBodyHandler.BYPASS_BODY_HANDLER)).thenReturn(Boolean.FALSE);
      Mockito.when(context.get(URLMappedEdgeDispatcher.CONFIGURATION_ITEM)).thenReturn(finalItem);
      Mockito.when(context.request()).thenReturn(request);
      Mockito.when(request.path()).thenReturn("/a/b/c/d/e");
      Mockito.when(dispatcher.createEdgeInvocation()).thenReturn(invocation);
      invocation.setVersionRule("2.0.0+");
      invocation.init("serviceName", context, "/c/d/e", dispatcher.getHttpServerFilters());
      Mockito.doNothing().when(invocation).edgeInvoke();
      dispatcher.onRequest(context);
    }
  }
}
