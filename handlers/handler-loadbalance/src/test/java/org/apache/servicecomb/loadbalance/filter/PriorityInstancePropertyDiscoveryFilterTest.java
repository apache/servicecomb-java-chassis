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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.MicroserviceProperties;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryTreeNode;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Sets;

import mockit.Deencapsulation;
import mockit.Injectable;

/**
 * Test for PriorityInstancePropertyDiscoveryFilter
 */
public class PriorityInstancePropertyDiscoveryFilterTest {

  public static final String PROPERTY_KEY = "environment";

  private PriorityInstancePropertyDiscoveryFilter filter;

  private List<StatefulDiscoveryInstance> instances;

  private MicroserviceProperties self;

  @Injectable
  RegistrationManager registrationManager;

  RegistrationManager original;

  StatefulDiscoveryInstance instance1;

  @Before
  public void setUp() {
    filter = new PriorityInstancePropertyDiscoveryFilter();
    instances = new ArrayList<>();
    self = new MicroserviceProperties();
//    self.setInstanceId("self");
    DiscoveryInstance discoveryInstance1 = Mockito.mock(DiscoveryInstance.class);
    instance1 = new StatefulDiscoveryInstance(discoveryInstance1);
    Mockito.when(discoveryInstance1.getInstanceId()).thenReturn("instance.empty");

    DiscoveryInstance discoveryInstance2 = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance2 = new StatefulDiscoveryInstance(discoveryInstance2);
    Mockito.when(discoveryInstance2.getInstanceId()).thenReturn("instance.local");
    Map<String, String> properties = new HashMap<>();
    properties.put(PROPERTY_KEY, "local");
    Mockito.when(discoveryInstance2.getProperties()).thenReturn(properties);

    DiscoveryInstance discoveryInstance3 = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance3 = new StatefulDiscoveryInstance(discoveryInstance3);
    Mockito.when(discoveryInstance3.getInstanceId()).thenReturn("instance.local.feature1");
    Map<String, String> properties3 = new HashMap<>();
    properties3.put(PROPERTY_KEY, "local.feature1");
    Mockito.when(discoveryInstance3.getProperties()).thenReturn(properties3);

    DiscoveryInstance discoveryInstance4 = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance4 = new StatefulDiscoveryInstance(discoveryInstance4);
    Mockito.when(discoveryInstance4.getInstanceId()).thenReturn("instance.local.feature1.sprint1");
    Map<String, String> properties4 = new HashMap<>();
    properties4.put(PROPERTY_KEY, "local.feature1.sprint1");
    Mockito.when(discoveryInstance4.getProperties()).thenReturn(properties4);

    instances.add(instance1);
    instances.add(instance2);
    instances.add(instance3);
    instances.add(instance4);

    original = Deencapsulation.getField(RegistrationManager.class, "INSTANCE");
    Deencapsulation.setField(RegistrationManager.class, "INSTANCE", registrationManager);
  }

  @After
  public void cleanup() {
    Deencapsulation.setField(RegistrationManager.class, "INSTANCE", original);
  }

  @Test
  public void testGetFilteredListOfServers() {

    //complete match
    executeTest("", Sets.newHashSet("instance.empty"));
    executeTest("local", Sets.newHashSet("instance.local"));
    executeTest("local.feature1", Sets.newHashSet("instance.local.feature1"));
    executeTest("local.feature1.sprint1", Sets.newHashSet("instance.local.feature1.sprint1"));

    //priority match
    executeTest("test", Sets.newHashSet("instance.empty"));
    executeTest("local.feature2", Sets.newHashSet("instance.local"));
    executeTest("local.feature1.sprint2", Sets.newHashSet("instance.local.feature1"));
    executeTest("local.feature2.sprint1", Sets.newHashSet("instance.local"));
    executeTest("local.feature1.sprint2.temp", Sets.newHashSet("instance.local.feature1"));

    //none match
    instances.remove(instance1);
    executeTest("", Collections.emptySet());
    executeTest("foo", Collections.emptySet());
    instances.add(instance1);
  }


  private void executeTest(String selfProperty, Set<String> expectedMatchedKeys) {
    Invocation invocation = new Invocation();
    DiscoveryContext discoveryContext = new DiscoveryContext();
    discoveryContext.setInputParameters(invocation);
    self.getProperties().put(PROPERTY_KEY, selfProperty);

    DiscoveryTreeNode parent = new DiscoveryTreeNode();
    parent.name("parent");
    parent.data(instances);

    DiscoveryTreeNode node = filter.discovery(discoveryContext, parent);
    List<StatefulDiscoveryInstance> filterInstance = node.data();
    assertThat(filterInstance.stream().map(instance -> instance.getDiscoveryInstance().getInstanceId()).collect(
        Collectors.toList())).containsAnyElementsOf(expectedMatchedKeys);
  }
}
