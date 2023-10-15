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

package org.apache.servicecomb.loadbalance;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import com.netflix.loadbalancer.Server;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;

public class TestLoadBalanceCreator {
  @Test
  public void testLoadBalanceWithRoundRobinRuleAndFilter(@Injectable Invocation invocation,
      @Injectable Transport transport) {
    // Robin components implementations require getReachableServers & getServerList have the same size, we add a test case for this.
    RoundRobinRuleExt rule = new RoundRobinRuleExt();
    List<ServiceCombServer> servers = new ArrayList<>();

    DiscoveryInstance discoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance1 = new StatefulDiscoveryInstance(discoveryInstance);
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn("instance1");
    Endpoint host1 = new Endpoint(transport, "host1", instance1);
    ServiceCombServer server = new ServiceCombServer(null, host1);

    DiscoveryInstance discoveryInstance2 = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance2 = new StatefulDiscoveryInstance(discoveryInstance2);
    Mockito.when(discoveryInstance2.getInstanceId()).thenReturn("instance2");
    Endpoint host2 = new Endpoint(transport, "host2", instance2);
    ServiceCombServer server2 = new ServiceCombServer(null, host2);

    servers.add(server);
    servers.add(server2);
    LoadBalancer lb = new LoadBalancer(rule, "test");

    List<ServerListFilterExt> filters = new ArrayList<>();

    filters.add((serverList, invocation1) -> {
      List<ServiceCombServer> filteredServers = new ArrayList<>();
      for (ServiceCombServer server1 : servers) {
        if (server1.getHost().equals("host1")) {
          continue;
        }
        filteredServers.add(server1);
      }
      return filteredServers;
    });
    lb.setFilters(filters);

    new Expectations() {
      {
        invocation.getLocalContext(LoadBalanceFilter.CONTEXT_KEY_SERVER_LIST);
        result = servers;
      }
    };
    Server s = lb.chooseServer(invocation);
    Assertions.assertEquals(server2, s);
    s = lb.chooseServer(invocation);
    Assertions.assertEquals(server2, s);
    s = lb.chooseServer(invocation);
    Assertions.assertEquals(server2, s);
  }

  @Test
  public void testLoadBalanceWithRandomRuleAndFilter(@Injectable Invocation invocation,
      @Injectable Transport transport) {
    // Robin components implementations require getReachableServers & getServerList have the same size, we add a test case for this.
    RandomRuleExt rule = new RandomRuleExt();
    LoadBalancer lb = new LoadBalancer(rule, "service");

    List<ServiceCombServer> servers = new ArrayList<>();

    DiscoveryInstance discoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance1 = new StatefulDiscoveryInstance(discoveryInstance);
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn("instance1");
    Endpoint host1 = new Endpoint(transport, "host1", instance1);
    ServiceCombServer server = new ServiceCombServer(null, host1);

    DiscoveryInstance discoveryInstance2 = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance2 = new StatefulDiscoveryInstance(discoveryInstance2);
    Mockito.when(discoveryInstance2.getInstanceId()).thenReturn("instance2");
    Endpoint host2 = new Endpoint(transport, "host2", instance2);
    ServiceCombServer server2 = new ServiceCombServer(null, host2);

    servers.add(server);
    servers.add(server2);

    List<ServerListFilterExt> filters = new ArrayList<>();
    filters.add((serverList, invocation1) -> {
      List<ServiceCombServer> filteredServers = new ArrayList<>();
      for (ServiceCombServer server1 : servers) {
        if (server1.getHost().equals("host1")) {
          continue;
        }
        filteredServers.add(server1);
      }
      return filteredServers;
    });
    lb.setFilters(filters);
    new Expectations() {
      {
        invocation.getLocalContext(LoadBalanceFilter.CONTEXT_KEY_SERVER_LIST);
        result = servers;
      }
    };
    Server s = lb.chooseServer(invocation);
    Assertions.assertEquals(server2, s);
    s = lb.chooseServer(invocation);
    Assertions.assertEquals(server2, s);
    s = lb.chooseServer(invocation);
    Assertions.assertEquals(server2, s);
  }

  @Test
  public void testLoadBalanceWithWeightedResponseTimeRuleAndFilter(@Injectable Endpoint endpoint1,
      @Injectable Endpoint endpoint2, @Injectable Invocation invocation) {
    // Robin components implementations require getReachableServers & getServerList have the same size, we add a test case for this.
    WeightedResponseTimeRuleExt rule = new WeightedResponseTimeRuleExt();
    LoadBalancer lb = new LoadBalancer(rule, "service");

    List<ServiceCombServer> servers = new ArrayList<>();

    DiscoveryInstance discoveryInstance1 = Mockito.mock(DiscoveryInstance.class);
//    StatefulDiscoveryInstance instance1 = new StatefulDiscoveryInstance(discoveryInstance1);
    Mockito.when(discoveryInstance1.getInstanceId()).thenReturn("ii01");

    DiscoveryInstance discoveryInstance2 = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance2 = new StatefulDiscoveryInstance(discoveryInstance2);
    Mockito.when(discoveryInstance2.getInstanceId()).thenReturn("ii02");

    new Expectations() {
      {
        endpoint1.getEndpoint();
        result = "host1";
//        endpoint1.getMicroserviceInstance();
//        result = instance1;
        endpoint2.getEndpoint();
        result = "host2";
        endpoint2.getMicroserviceInstance();
        result = instance2;
      }
    };

    ServiceCombServer server = new ServiceCombServer(null, endpoint1);
    ServiceCombServer server2 = new ServiceCombServer(null, endpoint2);

    servers.add(server);
    servers.add(server2);
    List<ServerListFilterExt> filters = new ArrayList<>();
    filters.add((serverList, invocation1) -> {
      List<ServiceCombServer> filteredServers = new ArrayList<>();
      for (ServiceCombServer server1 : servers) {
        if (server1.getHost().equals("host1")) {
          continue;
        }
        filteredServers.add(server1);
      }
      return filteredServers;
    });
    lb.setFilters(filters);
    new Expectations() {
      {
        invocation.getLocalContext(LoadBalanceFilter.CONTEXT_KEY_SERVER_LIST);
        result = servers;
      }
    };
    Server s = lb.chooseServer(invocation);
    Assertions.assertEquals(server2, s);
    s = lb.chooseServer(invocation);
    Assertions.assertEquals(server2, s);
    s = lb.chooseServer(invocation);
    Assertions.assertEquals(server2, s);
  }

  @Test
  public void testLoadBalanceWithSessionSticknessRule(@Injectable Invocation invocation,
      @Injectable Transport transport) {
    SessionStickinessRule rule = new SessionStickinessRule();
    LoadBalancer lb = new LoadBalancer(rule, "service");

    List<ServiceCombServer> servers = new ArrayList<>();

    DiscoveryInstance discoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance1 = new StatefulDiscoveryInstance(discoveryInstance);
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn("instance1");
    Endpoint host1 = new Endpoint(transport, "host1", instance1);
    ServiceCombServer server = new ServiceCombServer(null, host1);

    DiscoveryInstance discoveryInstance2 = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance2 = new StatefulDiscoveryInstance(discoveryInstance2);
    Mockito.when(discoveryInstance2.getInstanceId()).thenReturn("instance2");
    Endpoint host2 = new Endpoint(transport, "host2", instance2);
    ServiceCombServer server2 = new ServiceCombServer(null, host2);

    servers.add(server);
    servers.add(server2);

    lb.setFilters(new ArrayList<>());
    new Expectations() {
      {
        invocation.getLocalContext(LoadBalanceFilter.CONTEXT_KEY_SERVER_LIST);
        result = servers;
      }
    };

    Server s = lb.chooseServer(invocation);
    Assertions.assertEquals(server, s);
    s = lb.chooseServer(invocation);
    Assertions.assertEquals(server, s);

    long time = Deencapsulation.getField(rule, "lastAccessedTime");
    Deencapsulation.setField(rule, "lastAccessedTime", time - 1000 * 300);
    s = lb.chooseServer(invocation);
    Assertions.assertEquals(server2, s);

    lb.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(s);
    lb.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(s);
    lb.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(s);
    lb.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(s);
    lb.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(s);
    s = lb.chooseServer(invocation);
    Assertions.assertEquals(server, s);
  }
}
