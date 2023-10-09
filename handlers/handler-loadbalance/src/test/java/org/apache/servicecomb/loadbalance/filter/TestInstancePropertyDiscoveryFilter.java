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

package org.apache.servicecomb.loadbalance.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryTreeNode;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;

public class TestInstancePropertyDiscoveryFilter {

  private InstancePropertyDiscoveryFilter filter;

  StatefulDiscoveryInstance instance;

  ConfigurableEnvironment environment;

  EnumerablePropertySource<?> propertySource;

  @BeforeEach
  public void setUp() {
    environment = Mockito.mock(ConfigurableEnvironment.class);
    LegacyPropertyFactory.setEnvironment(environment);
    propertySource = Mockito.mock(EnumerablePropertySource.class);
    MutablePropertySources mutablePropertySources = new MutablePropertySources();
    mutablePropertySources.addLast(propertySource);
    Mockito.when(environment.getPropertySources()).thenReturn(mutablePropertySources);
    Mockito.when(propertySource.getPropertyNames()).thenReturn(new String[] {
        "servicecomb.loadbalance.test.flowsplitFilter.policy",
        "servicecomb.loadbalance.test.flowsplitFilter.options.tag0"
    });
    Mockito.when(environment.getProperty("servicecomb.loadbalance.test.flowsplitFilter.policy"))
        .thenReturn("org.apache.servicecomb.loadbalance.filter.SimpleFlowsplitFilter");
    Mockito.when(environment.getProperty("servicecomb.loadbalance.test.flowsplitFilter.options.tag0"))
        .thenReturn("value0");

    filter = new InstancePropertyDiscoveryFilter();
    Map<String, String> properties = new HashMap<>();
    properties.put("tag0", "value0");
    properties.put("tag1", "value1");
    DiscoveryInstance discoveryInstance = Mockito.mock(DiscoveryInstance.class);
    instance = new StatefulDiscoveryInstance(discoveryInstance);
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn("instance111");
    Mockito.when(discoveryInstance.getProperties()).thenReturn(properties);
  }

  @Test
  public void testAllowVisit() {
    Map<String, String> filterOptions = new HashMap<>();
    Assertions.assertTrue(filter.allowVisit(instance, filterOptions));

    filterOptions.put("tag0", "value0");
    Assertions.assertTrue(filter.allowVisit(instance, filterOptions));

    filterOptions.put("tag2", "value2");
    Assertions.assertFalse(filter.allowVisit(instance, filterOptions));

    filterOptions.clear();
    filterOptions.put("tag0", "value1");
    Assertions.assertFalse(filter.allowVisit(instance, filterOptions));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGetFilteredListOfServers() {
    DiscoveryContext context = new DiscoveryContext();
    DiscoveryTreeNode parent = new DiscoveryTreeNode();

    Invocation invocation = Mockito.mock(Invocation.class);
    context.setInputParameters(invocation);

    List<StatefulDiscoveryInstance> instances = new ArrayList<>();
    instances.add(instance);
    parent.data(instances);
    parent.name("parent");

    DiscoveryTreeNode node = filter.discovery(context, parent);
    Assertions.assertEquals(1, ((List<StatefulDiscoveryInstance>) node.data()).size());
  }
}
