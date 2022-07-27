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
import java.util.Collections;
import java.util.List;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.loadbalance.Configuration;
import org.apache.servicecomb.loadbalance.ServiceCombLoadBalancerStats;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.loadbalance.ServiceCombServerStats;
import org.apache.servicecomb.loadbalance.TestServiceCombServerStats;
import org.apache.servicecomb.loadbalance.filterext.IsolationDiscoveryFilter;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.cache.CacheEndpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import mockit.Deencapsulation;
import mockit.Mocked;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IsolationDiscoveryFilterTest {

  private IsolationDiscoveryFilter filter;

  private List<ServiceCombServer> servers;

  @Mocked
  private Transport transport = Mockito.mock(Transport.class);

  private Invocation invocation = new Invocation() {
    @Override
    public String getMicroserviceName() {
      return "testMicroserviceName";
    }
  };

  @BeforeEach
  public void before() {
    Mockito.doAnswer(a -> a.getArguments()[0]).when(transport).parseAddress(Mockito.anyString());
    servers = new ArrayList<>();
    for (int i = 0; i < 3; ++i) {
      MicroserviceInstance instance = new MicroserviceInstance();
      instance.setInstanceId("i" + i);
      String endpoint = "rest://127.0.0.1:" + i;
      instance.setEndpoints(Collections.singletonList(endpoint));
      ServiceCombServer serviceCombServer = new ServiceCombServer(invocation.getMicroserviceName(), transport,
          new CacheEndpoint(endpoint, instance));
      servers.add(serviceCombServer);
      ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer);
    }

    filter = new IsolationDiscoveryFilter();
    TestServiceCombServerStats.releaseTryingChance();
  }

  @AfterEach
  public void after() {
    ServiceCombLoadBalancerStats.INSTANCE.init();
    TestServiceCombServerStats.releaseTryingChance();
  }

  @Test
  public void discoveryNoInstanceReachErrorThreshold() {
    List<ServiceCombServer> filteredServers = filter.getFilteredListOfServers(servers, invocation);

    Assertions.assertEquals(filteredServers.size(), 3);
    Assertions.assertEquals(servers.get(0), filteredServers.get(0));
    Assertions.assertEquals(servers.get(1), filteredServers.get(1));
    Assertions.assertEquals(servers.get(2), filteredServers.get(2));
  }

  @Test
  public void discoveryIsolateErrorInstance() {
    ServiceCombServer server0 = servers.get(0);
    for (int i = 0; i < 4; ++i) {
      ServiceCombLoadBalancerStats.INSTANCE.markFailure(server0);
    }
    List<ServiceCombServer> filteredServers = filter.getFilteredListOfServers(servers, invocation);
    Assertions.assertEquals(filteredServers.size(), 3);
    Assertions.assertEquals(servers.get(0), filteredServers.get(0));
    Assertions.assertEquals(servers.get(1), filteredServers.get(1));
    Assertions.assertEquals(servers.get(2), filteredServers.get(2));

    // by default 5 times continuous failure will cause isolation
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server0);
    Assertions.assertFalse(ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(server0).isIsolated());

    filteredServers = filter.getFilteredListOfServers(servers, invocation);
    Assertions.assertEquals(filteredServers.size(), 2);
    Assertions.assertEquals(servers.get(1), filteredServers.get(0));
    Assertions.assertEquals(servers.get(2), filteredServers.get(1));
    Assertions.assertTrue(ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(server0).isIsolated());
  }

  @Test
  public void discoveryTryIsolatedInstanceAfterSingleTestTime() {
    ServiceCombServer server0 = servers.get(0);
    ServiceCombServerStats serviceCombServerStats = ServiceCombLoadBalancerStats.INSTANCE
        .getServiceCombServerStats(server0);
    for (int i = 0; i < 5; ++i) {
      serviceCombServerStats.markFailure();
    }
    letIsolatedInstancePassSingleTestTime(serviceCombServerStats);
    ServiceCombLoadBalancerStats.INSTANCE.markIsolated(server0, true);

    Assertions.assertTrue(ServiceCombServerStats.isolatedServerCanTry());
    Assertions.assertNull(TestServiceCombServerStats.getTryingIsolatedServerInvocation());
    List<ServiceCombServer> filteredServers = filter.getFilteredListOfServers(servers, invocation);
    Assertions.assertEquals(filteredServers.size(), 3);
    Assertions.assertEquals(servers.get(0), filteredServers.get(0));
    Assertions.assertEquals(servers.get(1), filteredServers.get(1));
    Assertions.assertEquals(servers.get(2), filteredServers.get(2));
    Assertions.assertTrue(serviceCombServerStats.isIsolated());
    Assertions.assertFalse(ServiceCombServerStats.isolatedServerCanTry());
    Assertions.assertSame(invocation, TestServiceCombServerStats.getTryingIsolatedServerInvocation());
  }

  @Test
  public void discoveryNotTryIsolatedInstanceConcurrently() {
    ServiceCombServer server0 = servers.get(0);
    ServiceCombServerStats serviceCombServerStats = ServiceCombLoadBalancerStats.INSTANCE
        .getServiceCombServerStats(server0);
    for (int i = 0; i < 5; ++i) {
      serviceCombServerStats.markFailure();
    }
    ServiceCombLoadBalancerStats.INSTANCE.markIsolated(server0, true);
    letIsolatedInstancePassSingleTestTime(serviceCombServerStats);

    Assertions.assertTrue(ServiceCombServerStats.isolatedServerCanTry());

    // The first invocation can occupy the trying chance
    List<ServiceCombServer> filteredServers = filter.getFilteredListOfServers(servers, invocation);
    Assertions.assertEquals(filteredServers.size(), 3);
    Assertions.assertEquals(servers.get(0), filteredServers.get(0));
    Assertions.assertEquals(servers.get(1), filteredServers.get(1));
    Assertions.assertEquals(servers.get(2), filteredServers.get(2));
    Assertions.assertFalse(ServiceCombServerStats.isolatedServerCanTry());

    // Other invocation cannot get trying chance concurrently
    filteredServers = filter.getFilteredListOfServers(servers, invocation);
    Assertions.assertEquals(filteredServers.size(), 2);
    Assertions.assertEquals(servers.get(1), filteredServers.get(0));
    Assertions.assertEquals(servers.get(2), filteredServers.get(1));

    ServiceCombServerStats
        .checkAndReleaseTryingChance(invocation); // after the first invocation releases the trying chance

    // Other invocation can get the trying chance
    filteredServers = filter.getFilteredListOfServers(servers, invocation);
    Assertions.assertEquals(filteredServers.size(), 3);
    Assertions.assertEquals(servers.get(0), filteredServers.get(0));
    Assertions.assertEquals(servers.get(1), filteredServers.get(1));
    Assertions.assertEquals(servers.get(2), filteredServers.get(2));
    Assertions.assertFalse(ServiceCombServerStats.isolatedServerCanTry());
  }

  private ServiceCombServerStats letIsolatedInstancePassSingleTestTime(ServiceCombServerStats serviceCombServerStats) {
    Deencapsulation.setField(serviceCombServerStats, "lastActiveTime",
        System.currentTimeMillis() - 1 - Configuration.INSTANCE.getSingleTestTime(invocation.getMicroserviceName()));
    Deencapsulation.setField(serviceCombServerStats, "lastVisitTime",
        System.currentTimeMillis() - 1 - Configuration.INSTANCE.getSingleTestTime(invocation.getMicroserviceName()));
    return serviceCombServerStats;
  }

  @Test
  public void discoveryKeepMinIsolationTime() {
    ServiceCombServer server0 = servers.get(0);
    ServiceCombLoadBalancerStats.INSTANCE.markIsolated(server0, true);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server0);

    List<ServiceCombServer> filteredServers = filter.getFilteredListOfServers(servers, invocation);
    Assertions.assertEquals(filteredServers.size(), 2);
    Assertions.assertEquals(servers.get(1), filteredServers.get(0));
    Assertions.assertEquals(servers.get(2), filteredServers.get(1));

    ServiceCombServerStats serviceCombServerStats = ServiceCombLoadBalancerStats.INSTANCE
        .getServiceCombServerStats(server0);
    Deencapsulation.setField(serviceCombServerStats, "isolatedTime",
        System.currentTimeMillis() - Configuration.INSTANCE.getMinIsolationTime(invocation.getMicroserviceName()) - 1);
    filteredServers = filter.getFilteredListOfServers(servers, invocation);
    Assertions.assertEquals(filteredServers.size(), 3);
    Assertions.assertEquals(servers.get(0), filteredServers.get(0));
    Assertions.assertEquals(servers.get(1), filteredServers.get(1));
    Assertions.assertEquals(servers.get(2), filteredServers.get(2));
  }

  @Test
  public void discoveryRecoverInstance() {
    ServiceCombServer server0 = servers.get(0);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server0);
    ServiceCombServerStats serviceCombServerStats = ServiceCombLoadBalancerStats.INSTANCE
        .getServiceCombServerStats(server0);

    ServiceCombLoadBalancerStats.INSTANCE.markIsolated(server0, true);
    Deencapsulation.setField(serviceCombServerStats, "isolatedTime",
        System.currentTimeMillis() - Configuration.INSTANCE.getMinIsolationTime(invocation.getMicroserviceName()) - 1);

    List<ServiceCombServer> filteredServers = filter.getFilteredListOfServers(servers, invocation);
    Assertions.assertEquals(filteredServers.size(), 3);
    Assertions.assertEquals(servers.get(0), filteredServers.get(0));
    Assertions.assertEquals(servers.get(1), filteredServers.get(1));
    Assertions.assertEquals(servers.get(2), filteredServers.get(2));
    Assertions.assertFalse(ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(server0).isIsolated());
  }
}
