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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.loadbalance.Configuration;
import org.apache.servicecomb.loadbalance.ServiceCombLoadBalancerStats;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.loadbalance.ServiceCombServerStats;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.CacheEndpoint;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryContext;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTreeNode;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import mockit.Deencapsulation;
import mockit.Mocked;

public class IsolationDiscoveryFilterTest {

  private DiscoveryContext discoveryContext;

  private DiscoveryTreeNode discoveryTreeNode;

  private Map<String, MicroserviceInstance> data;

  private IsolationDiscoveryFilter filter;

  @Mocked
  private Transport transport = Mockito.mock(Transport.class);

  private Invocation invocation = new Invocation() {
    @Override
    public String getMicroserviceName() {
      return "testMicroserviceName";
    }
  };

  @Before
  public void before() {
    discoveryContext = new DiscoveryContext();
    discoveryContext.setInputParameters(invocation);
    discoveryTreeNode = new DiscoveryTreeNode();
    Mockito.doAnswer(a -> a.getArguments()[0]).when(transport).parseAddress(Mockito.anyString());
    data = new HashMap<>();
    for (int i = 0; i < 3; ++i) {
      MicroserviceInstance instance = new MicroserviceInstance();
      instance.setInstanceId("i" + i);
      String endpoint = "rest://127.0.0.1:" + i;
      instance.setEndpoints(Collections.singletonList(endpoint));
      data.put(instance.getInstanceId(), instance);
      ServiceCombServer serviceCombServer = new ServiceCombServer(transport, new CacheEndpoint(endpoint, instance));
      ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer);
    }
    discoveryTreeNode.data(data);

    filter = new IsolationDiscoveryFilter();
    ServiceCombServerStats.releaseTryingChance();
  }

  @After
  public void after() {
    Deencapsulation.invoke(ServiceCombLoadBalancerStats.INSTANCE, "init");
    ServiceCombServerStats.releaseTryingChance();
  }

  @Test
  public void discovery_no_instance_reach_error_threshold() {
    DiscoveryTreeNode childNode = filter.discovery(discoveryContext, discoveryTreeNode);

    Map<String, MicroserviceInstance> childNodeData = childNode.data();
    Assert.assertThat(childNodeData.keySet(), Matchers.containsInAnyOrder("i0", "i1", "i2"));
    Assert.assertEquals(data.get("i0"), childNodeData.get("i0"));
    Assert.assertEquals(data.get("i1"), childNodeData.get("i1"));
    Assert.assertEquals(data.get("i2"), childNodeData.get("i2"));
  }

  @Test
  public void discovery_isolate_error_instance() {
    ServiceCombServer server0 = ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServer(data.get("i0"));
    for (int i = 0; i < 4; ++i) {
      ServiceCombLoadBalancerStats.INSTANCE.markFailure(server0);
    }
    DiscoveryTreeNode childNode = filter.discovery(discoveryContext, discoveryTreeNode);
    Map<String, MicroserviceInstance> childNodeData = childNode.data();
    Assert.assertThat(childNodeData.keySet(), Matchers.containsInAnyOrder("i0", "i1", "i2"));
    Assert.assertEquals(data.get("i0"), childNodeData.get("i0"));
    Assert.assertEquals(data.get("i1"), childNodeData.get("i1"));
    Assert.assertEquals(data.get("i2"), childNodeData.get("i2"));

    // by default 5 times continuous failure will cause isolation
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server0);
    Assert.assertFalse(ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(server0).isIsolated());

    childNode = filter.discovery(discoveryContext, discoveryTreeNode);
    childNodeData = childNode.data();
    Assert.assertThat(childNodeData.keySet(), Matchers.containsInAnyOrder("i1", "i2"));
    Assert.assertEquals(data.get("i1"), childNodeData.get("i1"));
    Assert.assertEquals(data.get("i2"), childNodeData.get("i2"));
    Assert.assertTrue(ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(server0).isIsolated());
  }

  @Test
  public void discovery_try_isolated_instance_after_singleTestTime() {
    ServiceCombServer server0 = ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServer(data.get("i0"));
    ServiceCombServerStats serviceCombServerStats = ServiceCombLoadBalancerStats.INSTANCE
        .getServiceCombServerStats(server0);
    for (int i = 0; i < 5; ++i) {
      serviceCombServerStats.markFailure();
    }
    letIsolatedInstancePassSingleTestTime(serviceCombServerStats);
    ServiceCombLoadBalancerStats.INSTANCE.markIsolated(server0, true);

    Assert.assertTrue(ServiceCombServerStats.isolatedServerCanTry());
    Assert.assertFalse(
        Boolean.TRUE.equals(invocation.getLocalContext(IsolationDiscoveryFilter.TRYING_INSTANCES_EXISTING)));
    DiscoveryTreeNode childNode = filter.discovery(discoveryContext, discoveryTreeNode);
    Map<String, MicroserviceInstance> childNodeData = childNode.data();
    Assert.assertThat(childNodeData.keySet(), Matchers.containsInAnyOrder("i0", "i1", "i2"));
    Assert.assertEquals(data.get("i0"), childNodeData.get("i0"));
    Assert.assertEquals(data.get("i1"), childNodeData.get("i1"));
    Assert.assertEquals(data.get("i2"), childNodeData.get("i2"));
    Assert.assertTrue(serviceCombServerStats.isIsolated());
    Assert.assertFalse(ServiceCombServerStats.isolatedServerCanTry());
    Assert.assertTrue(
        Boolean.TRUE.equals(invocation.getLocalContext(IsolationDiscoveryFilter.TRYING_INSTANCES_EXISTING)));
  }

  @Test
  public void discovery_not_try_isolated_instance_concurrently() {
    ServiceCombServer server0 = ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServer(data.get("i0"));
    ServiceCombServerStats serviceCombServerStats = ServiceCombLoadBalancerStats.INSTANCE
        .getServiceCombServerStats(server0);
    for (int i = 0; i < 5; ++i) {
      serviceCombServerStats.markFailure();
    }
    ServiceCombLoadBalancerStats.INSTANCE.markIsolated(server0, true);
    letIsolatedInstancePassSingleTestTime(serviceCombServerStats);

    Assert.assertTrue(ServiceCombServerStats.isolatedServerCanTry());

    // The first invocation can occupy the trying chance
    DiscoveryTreeNode childNode = filter.discovery(discoveryContext, discoveryTreeNode);
    Map<String, MicroserviceInstance> childNodeData = childNode.data();
    Assert.assertThat(childNodeData.keySet(), Matchers.containsInAnyOrder("i0", "i1", "i2"));
    Assert.assertEquals(data.get("i0"), childNodeData.get("i0"));
    Assert.assertEquals(data.get("i1"), childNodeData.get("i1"));
    Assert.assertEquals(data.get("i2"), childNodeData.get("i2"));
    Assert.assertFalse(ServiceCombServerStats.isolatedServerCanTry());

    // Other invocation cannot get trying chance concurrently
    childNode = filter.discovery(discoveryContext, discoveryTreeNode);
    childNodeData = childNode.data();
    Assert.assertThat(childNodeData.keySet(), Matchers.containsInAnyOrder("i1", "i2"));
    Assert.assertEquals(data.get("i1"), childNodeData.get("i1"));
    Assert.assertEquals(data.get("i2"), childNodeData.get("i2"));

    ServiceCombServerStats.releaseTryingChance(); // after the first invocation releases the trying chance

    // Other invocation can get the trying chance
    childNode = filter.discovery(discoveryContext, discoveryTreeNode);
    childNodeData = childNode.data();
    Assert.assertThat(childNodeData.keySet(), Matchers.containsInAnyOrder("i0", "i1", "i2"));
    Assert.assertEquals(data.get("i0"), childNodeData.get("i0"));
    Assert.assertEquals(data.get("i1"), childNodeData.get("i1"));
    Assert.assertEquals(data.get("i2"), childNodeData.get("i2"));
    Assert.assertFalse(ServiceCombServerStats.isolatedServerCanTry());
  }

  private ServiceCombServerStats letIsolatedInstancePassSingleTestTime(ServiceCombServerStats serviceCombServerStats) {
    Deencapsulation.setField(serviceCombServerStats, "lastActiveTime",
        System.currentTimeMillis() - 1 - Configuration.INSTANCE.getSingleTestTime(invocation.getMicroserviceName()));
    Deencapsulation.setField(serviceCombServerStats, "lastVisitTime",
        System.currentTimeMillis() - 1 - Configuration.INSTANCE.getSingleTestTime(invocation.getMicroserviceName()));
    return serviceCombServerStats;
  }

  @Test
  public void discovery_keep_minIsolationTime() {
    ServiceCombServer server0 = ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServer(data.get("i0"));
    ServiceCombLoadBalancerStats.INSTANCE.markIsolated(server0, true);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server0);

    DiscoveryTreeNode childNode = filter.discovery(discoveryContext, discoveryTreeNode);
    Map<String, MicroserviceInstance> childNodeData = childNode.data();
    Assert.assertThat(childNodeData.keySet(), Matchers.containsInAnyOrder("i1", "i2"));
    Assert.assertEquals(data.get("i1"), childNodeData.get("i1"));
    Assert.assertEquals(data.get("i2"), childNodeData.get("i2"));

    ServiceCombServerStats serviceCombServerStats = ServiceCombLoadBalancerStats.INSTANCE
        .getServiceCombServerStats(server0);
    Deencapsulation.setField(serviceCombServerStats, "lastVisitTime",
        System.currentTimeMillis() - Configuration.INSTANCE.getMinIsolationTime(invocation.getMicroserviceName()) - 1);
    childNode = filter.discovery(discoveryContext, discoveryTreeNode);
    childNodeData = childNode.data();
    Assert.assertThat(childNodeData.keySet(), Matchers.containsInAnyOrder("i0", "i1", "i2"));
    Assert.assertEquals(data.get("i0"), childNodeData.get("i0"));
    Assert.assertEquals(data.get("i1"), childNodeData.get("i1"));
    Assert.assertEquals(data.get("i2"), childNodeData.get("i2"));
  }

  @Test
  public void discovery_recover_instance() {
    ServiceCombServer server0 = ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServer(data.get("i0"));
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server0);
    ServiceCombServerStats serviceCombServerStats = ServiceCombLoadBalancerStats.INSTANCE
        .getServiceCombServerStats(server0);
    Deencapsulation.setField(serviceCombServerStats, "lastVisitTime",
        System.currentTimeMillis() - Configuration.INSTANCE.getMinIsolationTime(invocation.getMicroserviceName()) - 1);

    ServiceCombLoadBalancerStats.INSTANCE.markIsolated(server0, true);
    DiscoveryTreeNode childNode = filter.discovery(discoveryContext, discoveryTreeNode);
    Map<String, MicroserviceInstance> childNodeData = childNode.data();
    Assert.assertThat(childNodeData.keySet(), Matchers.containsInAnyOrder("i0", "i1", "i2"));
    Assert.assertEquals(data.get("i0"), childNodeData.get("i0"));
    Assert.assertEquals(data.get("i1"), childNodeData.get("i1"));
    Assert.assertEquals(data.get("i2"), childNodeData.get("i2"));
    Assert.assertFalse(ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(server0).isIsolated());
  }
}