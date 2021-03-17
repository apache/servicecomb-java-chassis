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

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestDefaultEdgeDispatcher {
  @Before
  public void setUp() {

  }

  @After
  public void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testOnRequest(@Mocked Router router, @Mocked Route route
      , @Mocked RoutingContext context
      , @Mocked HttpServerRequest requst
      , @Mocked EdgeInvocation invocation) {
    DefaultEdgeDispatcher dispatcher = new DefaultEdgeDispatcher() {
      @Override
      protected boolean isFilterChainEnabled() {
        return false;
      }
    };

    new Expectations(SCBEngine.class) {
      {
        router.routeWithRegex(dispatcher.generateRouteRegex("api", true));
        result = route;
        route.handler((Handler<RoutingContext>) any);
        result = route;
        route.failureHandler((Handler<RoutingContext>) any);
        result = route;
        context.pathParam(MICROSERVICE_NAME);
        result = "testService";
        context.pathParam(VERSION);
        result = "v1";
        context.request();
        result = requst;
        requst.path();
        result = "/api/testService/v1/hello";
        invocation.setVersionRule("1.0.0.0-2.0.0.0");
        invocation.init("testService", context, "/testService/v1/hello",
            Deencapsulation.getField(dispatcher, "httpServerFilters"));
        invocation.edgeInvoke();
      }
    };
    dispatcher.init(router);
    Assert.assertEquals(dispatcher.enabled(), false);
    Assert.assertEquals(Utils.findActualPath("/api/test", 1), "/test");
    Assert.assertEquals(dispatcher.getOrder(), 20000);

    dispatcher.onRequest(context);
    // assert done in expectations.
  }
}
