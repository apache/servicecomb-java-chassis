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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.config.ConfigurationChangedEvent;
import org.apache.servicecomb.transport.rest.vertx.RestBodyHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;

import io.vertx.ext.web.RoutingContext;

public class TestURLMappedEdgeDispatcher {
  ConfigurableEnvironment environment = Mockito.mock(ConfigurableEnvironment.class);

  @BeforeEach
  public void setUp() throws Exception {
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testConfigurations() {
    EnumerablePropertySource<?> propertySource = Mockito.mock(EnumerablePropertySource.class);
    MutablePropertySources mutablePropertySources = new MutablePropertySources();
    mutablePropertySources.addLast(propertySource);
    Mockito.when(environment.getPropertySources()).thenReturn(mutablePropertySources);
    Mockito.when(propertySource.getPropertyNames()).thenReturn(new String[] {
    });
    Mockito.when(environment.getProperty("servicecomb.http.dispatcher.edge.url.enabled", boolean.class, false))
        .thenReturn(true);
    URLMappedEdgeDispatcher dispatcher = new URLMappedEdgeDispatcher();
    dispatcher.setEnvironment(environment);
    Map<String, URLMappedConfigurationItem> items = dispatcher.getConfigurations();
    Assertions.assertEquals(items.size(), 0);

    RoutingContext context = Mockito.mock(RoutingContext.class);
    Mockito.when(context.get(RestBodyHandler.BYPASS_BODY_HANDLER)).thenReturn(Boolean.TRUE);
    dispatcher.onRequest(context);

    Mockito.when(propertySource.getPropertyNames()).thenReturn(new String[] {
        "servicecomb.http.dispatcher.edge.url.mappings.service1.path",
        "servicecomb.http.dispatcher.edge.url.mappings.service1.microserviceName",
        "servicecomb.http.dispatcher.edge.url.mappings.service1.prefixSegmentCount",
        "servicecomb.http.dispatcher.edge.url.mappings.service1.versionRule"
    });
    Mockito.when(environment.getProperty("servicecomb.http.dispatcher.edge.url.mappings.service1.path"))
        .thenReturn("/a/b/c/.*");
    Mockito.when(environment.getProperty("servicecomb.http.dispatcher.edge.url.mappings.service1.microserviceName"))
        .thenReturn("serviceName");
    Mockito.when(environment.getProperty("servicecomb.http.dispatcher.edge.url.mappings.service1.prefixSegmentCount",
            int.class, 0))
        .thenReturn(2);
    Mockito.when(environment.getProperty("servicecomb.http.dispatcher.edge.url.mappings.service1.versionRule",
            "0.0.0+"))
        .thenReturn("2.0.0+");
    Map<String, Object> latest = new HashMap<>();
    latest.put("servicecomb.http.dispatcher.edge.url.mappings.service1.path", "/a/b/c/.*");
    dispatcher.onConfigurationChangedEvent(ConfigurationChangedEvent.createIncremental(latest, new HashMap<>()));

    items = dispatcher.getConfigurations();
    Assertions.assertEquals(items.size(), 1);
    URLMappedConfigurationItem item = items.get("service1");
    Assertions.assertEquals(item.getMicroserviceName(), "serviceName");
    Assertions.assertEquals(item.getPrefixSegmentCount(), 2);
    Assertions.assertEquals(item.getStringPattern(), "/a/b/c/.*");
    Assertions.assertEquals(item.getVersionRule(), "2.0.0+");

    Mockito.when(propertySource.getPropertyNames()).thenReturn(new String[] {
        "servicecomb.http.dispatcher.edge.url.mappings.service1.path",
        "servicecomb.http.dispatcher.edge.url.mappings.service1.microserviceName",
        "servicecomb.http.dispatcher.edge.url.mappings.service1.prefixSegmentCount",
        "servicecomb.http.dispatcher.edge.url.mappings.service1.versionRule",
        "servicecomb.http.dispatcher.edge.url.mappings.service2.versionRule",
        "servicecomb.http.dispatcher.edge.url.mappings.service3.path"
    });
    Mockito.when(environment.getProperty("servicecomb.http.dispatcher.edge.url.mappings.service2.versionRule"))
        .thenReturn("2.0.0+");
    Mockito.when(environment.getProperty("servicecomb.http.dispatcher.edge.url.mappings.service3.path"))
        .thenReturn("/b/c/d/.*");

    latest = new HashMap<>();
    latest.put("servicecomb.http.dispatcher.edge.url.mappings.service3.path", "/a/b/c/.*");
    dispatcher.onConfigurationChangedEvent(ConfigurationChangedEvent.createIncremental(latest, new HashMap<>()));

    items = dispatcher.getConfigurations();
    Assertions.assertEquals(items.size(), 1);
    item = items.get("service1");
    Assertions.assertEquals(item.getMicroserviceName(), "serviceName");
    Assertions.assertEquals(item.getPrefixSegmentCount(), 2);
    Assertions.assertEquals(item.getStringPattern(), "/a/b/c/.*");
    Assertions.assertEquals(item.getVersionRule(), "2.0.0+");
  }
}
