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

import org.apache.servicecomb.edge.core.URLMappedEdgeDispatcher.ConfigurationItem;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestURLMappedEdgeDispatcher {
  @Before
  public void setUp() throws Exception {
    Log4jUtils.init();
  }

  @After
  public void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testConfigurations(@Mocked RoutingContext context
      , @Mocked HttpServerRequest requst
      , @Mocked EdgeInvocation invocation) {
    ArchaiusUtils.setProperty("servicecomb.http.dispatcher.edge.url.enabled", true);

    URLMappedEdgeDispatcher dispatcher = new URLMappedEdgeDispatcher();
    Map<String, ConfigurationItem> items = Deencapsulation
        .getField(dispatcher, "configurations");
    Assert.assertEquals(items.size(), 0);

    new Expectations() {
      {
        context.next();
      }
    };
    dispatcher.onRequest(context);

    ArchaiusUtils.setProperty("servicecomb.http.dispatcher.edge.url.mappings.service1.path", "/a/b/c/.*");
    ArchaiusUtils.setProperty("servicecomb.http.dispatcher.edge.url.mappings.service1.microserviceName", "serviceName");
    ArchaiusUtils.setProperty("servicecomb.http.dispatcher.edge.url.mappings.service1.prefixSegmentCount", 2);
    ArchaiusUtils.setProperty("servicecomb.http.dispatcher.edge.url.mappings.service1.versionRule", "2.0.0+");
    items = Deencapsulation.getField(dispatcher, "configurations");
    Assert.assertEquals(items.size(), 1);
    ConfigurationItem item = items.get("service1");
    Assert.assertEquals(item.microserviceName, "serviceName");
    Assert.assertEquals(item.prefixSegmentCount, 2);
    Assert.assertEquals(item.stringPattern, "/a/b/c/.*");
    Assert.assertEquals(item.versionRule, "2.0.0+");

    ArchaiusUtils.setProperty("servicecomb.http.dispatcher.edge.url.mappings.service2.versionRule", "2.0.0+");
    ArchaiusUtils.setProperty("servicecomb.http.dispatcher.edge.url.mappings.service3.path", "/b/c/d/.*");
    items = Deencapsulation.getField(dispatcher, "configurations");
    Assert.assertEquals(items.size(), 1);
    item = items.get("service1");
    Assert.assertEquals(item.microserviceName, "serviceName");
    Assert.assertEquals(item.prefixSegmentCount, 2);
    Assert.assertEquals(item.stringPattern, "/a/b/c/.*");
    Assert.assertEquals(item.versionRule, "2.0.0+");

    new Expectations() {
      {
        context.request();
        result = requst;
        requst.path();
        result = "/a/b/c/d/e";
        invocation.setVersionRule("2.0.0+");
        invocation.init("serviceName", context, "/c/d/e",
            Deencapsulation.getField(dispatcher, "httpServerFilters"));
        invocation.edgeInvoke();
      }
    };
    dispatcher.onRequest(context);
  }
}
