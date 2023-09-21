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

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.transport.rest.vertx.RestBodyHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.netflix.config.DynamicPropertyFactory;

import io.vertx.ext.web.RoutingContext;

public class TestURLMappedEdgeDispatcher {
  Environment environment = Mockito.mock(Environment.class);

  @BeforeEach
  public void setUp() throws Exception {
    DynamicPropertyFactory.getInstance();
  }

  @AfterEach
  public void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testConfigurations() {
    Mockito.when(environment.getProperty("servicecomb.http.dispatcher.edge.url.enabled", boolean.class, false))
        .thenReturn(true);
    URLMappedEdgeDispatcher dispatcher = new URLMappedEdgeDispatcher();
    dispatcher.setEnvironment(environment);
    Map<String, URLMappedConfigurationItem> items = dispatcher.getConfigurations();
    Assertions.assertEquals(items.size(), 0);

    RoutingContext context = Mockito.mock(RoutingContext.class);
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
  }
}
