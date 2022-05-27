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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryTreeNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;

/**
 * Test for PriorityInstancePropertyDiscoveryFilter
 */
public class PriorityInstancePropertyDiscoveryFilterTest {

  public static final String PROPERTY_KEY = "environment";

  private PriorityInstancePropertyDiscoveryFilter filter;

  private Map<String, MicroserviceInstance> instances;

  private MicroserviceInstance self;

  @Injectable
  RegistrationManager registrationManager;

  RegistrationManager original;

  @Before
  public void setUp() {
    filter = new PriorityInstancePropertyDiscoveryFilter();
    instances = new HashMap<>();
    self = new MicroserviceInstance();
    self.setInstanceId("self");
    MicroserviceInstance instance1 = new MicroserviceInstance();
    instance1.setInstanceId("instance.empty");
    MicroserviceInstance instance2 = new MicroserviceInstance();
    instance2.getProperties().put(PROPERTY_KEY, "local");
    instance2.setInstanceId("instance.local");
    MicroserviceInstance instance3 = new MicroserviceInstance();
    instance3.getProperties().put(PROPERTY_KEY, "local.feature1");
    instance3.setInstanceId("instance.local.feature1");
    MicroserviceInstance instance4 = new MicroserviceInstance();
    instance4.getProperties().put(PROPERTY_KEY, "local.feature1.sprint1");
    instance4.setInstanceId("instance.local.feature1.sprint1");

    instances.put(instance1.getInstanceId(), instance1);
    instances.put(instance2.getInstanceId(), instance2);
    instances.put(instance3.getInstanceId(), instance3);
    instances.put(instance4.getInstanceId(), instance4);

    original = Deencapsulation.getField(RegistrationManager.class, "INSTANCE");
    Deencapsulation.setField(RegistrationManager.class, "INSTANCE", registrationManager);

    new Expectations() {
      {
        registrationManager.getMicroserviceInstance();
        result = self;
      }
    };
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
    MicroserviceInstance instance1 = instances.remove("instance.empty");
    executeTest("", Collections.emptySet());
    executeTest("foo", Collections.emptySet());
    instances.put("instance.empty", instance1);
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
    Map<String, MicroserviceInstance> filterInstance = node.data();
    assertThat(filterInstance.keySet()).containsAnyElementsOf(expectedMatchedKeys);
  }
}
