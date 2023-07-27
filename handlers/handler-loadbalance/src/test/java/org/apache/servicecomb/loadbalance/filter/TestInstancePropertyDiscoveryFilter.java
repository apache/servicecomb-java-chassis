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

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryTreeNode;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import mockit.Expectations;
import mockit.Injectable;

public class TestInstancePropertyDiscoveryFilter {

  private InstancePropertyDiscoveryFilter filter;

  StatefulDiscoveryInstance instance;

  @BeforeClass
  public static void beforeCls() {
    AbstractConfiguration configuration = new BaseConfiguration();
    configuration.addProperty("servicecomb.loadbalance.test.flowsplitFilter.policy",
        "org.apache.servicecomb.loadbalance.filter.SimpleFlowsplitFilter");
    configuration.addProperty("servicecomb.loadbalance.test.flowsplitFilter.options.tag0", "value0");
  }

  @Before
  public void setUp() {
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
  public void testGetFilteredListOfServers(@Injectable DiscoveryContext context, @Injectable DiscoveryTreeNode parent,
      @Injectable Invocation invocation) {
    List<StatefulDiscoveryInstance> instances = new ArrayList<>();
    instances.add(instance);
    new Expectations() {
      {
        context.getInputParameters();
        result = invocation;
        parent.data();
        result = instances;
        parent.name();
        result = "parent";
      }
    };

    DiscoveryTreeNode node = filter.discovery(context, parent);
    Assertions.assertEquals(1, ((List<StatefulDiscoveryInstance>) node.data()).size());
  }
}
