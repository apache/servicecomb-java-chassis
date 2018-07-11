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

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.Assert;
import org.junit.Test;

import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.WeightedResponseTimeRule;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

public class TestLoadBalanceCreator {
  @Test
  public void testLoadBalanceWithRoundRobinRuleAndFilter(@Injectable Invocation invocation) {
    // Robin components implementations require getReachableServers & getServerList have the same size, we add a test case for this.
    RoundRobinRule rule = new RoundRobinRule();
    List<Server> servers = new ArrayList<>();
    Server server = new Server("host1", 80);
    server.setAlive(true);
    Server server2 = new Server("host2", 80);
    server2.setAlive(true);
    servers.add(server);
    servers.add(server2);
    LoadBalancerCreator lbCreator = new LoadBalancerCreator(rule, "test");

    List<ServerListFilterExt> filters = new ArrayList<>();

    filters.add(new ServerListFilterExt() {
      @Override
      public List<Server> getFilteredListOfServers(List<Server> serverList, Invocation invocation) {
        List<Server> filteredServers = new ArrayList<>();
        for (Server server : servers) {
          if (server.getHost().equals("host1")) {
            continue;
          }
          filteredServers.add(server);
        }
        return filteredServers;
      }
    });
    lbCreator.setFilters(filters);

    LoadBalancer lb = lbCreator.createLoadBalancer(invocation);
    Server s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
    s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
    s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
  }

  @Test
  public void testLoadBalanceWithRandomRuleAndFilter(@Injectable Invocation invocation) {
    // Robin components implementations require getReachableServers & getServerList have the same size, we add a test case for this.
    RandomRule rule = new RandomRule();
    LoadBalancerCreator lbCreator = new LoadBalancerCreator(rule, "service");

    List<Server> servers = new ArrayList<>();
    Server server = new Server("host1", 80);
    server.setAlive(true);
    Server server2 = new Server("host2", 80);
    server2.setAlive(true);
    servers.add(server);
    servers.add(server2);
    lbCreator.setServerList(servers);
    List<ServerListFilterExt> filters = new ArrayList<>();
    filters.add(new ServerListFilterExt() {
      @Override
      public List<Server> getFilteredListOfServers(List<Server> serverList, Invocation invocation) {
        List<Server> filteredServers = new ArrayList<>();
        for (Server server : servers) {
          if (server.getHost().equals("host1")) {
            continue;
          }
          filteredServers.add(server);
        }
        return filteredServers;
      }
    });
    lbCreator.setFilters(filters);
    LoadBalancer lb = lbCreator.createLoadBalancer(invocation);
    Server s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
    s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
    s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
  }

  @Test
  public void testLoadBalanceWithWeightedResponseTimeRuleAndFilter(@Mocked ServiceCombServer server,
      @Mocked ServiceCombServer server2, @Injectable Invocation invocation) {
    // Robin components implementations require getReachableServers & getServerList have the same size, we add a test case for this.
    WeightedResponseTimeRule rule = new WeightedResponseTimeRule();
    LoadBalancerCreator lbCreator = new LoadBalancerCreator(rule, "service");
    List<Server> servers = new ArrayList<>();

    new Expectations() {
      {
        server.getHost();
        result = "host1";

        server2.isReadyToServe();
        result = true;
        server2.isAlive();
        result = true;
        server2.getHost();
        result = "host2";
      }
    };

    servers.add(server);
    servers.add(server2);
    lbCreator.setServerList(servers);
    List<ServerListFilterExt> filters = new ArrayList<>();
    filters.add(new ServerListFilterExt() {
      @Override
      public List<Server> getFilteredListOfServers(List<Server> serverList, Invocation invocation) {
        List<Server> filteredServers = new ArrayList<>();
        for (Server server : servers) {
          if (server.getHost().equals("host1")) {
            continue;
          }
          filteredServers.add(server);
        }
        return filteredServers;
      }
    });
    lbCreator.setFilters(filters);
    LoadBalancer lb = lbCreator.createLoadBalancer(invocation);
    Server s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
    s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
    s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
  }

  @Test
  public void testLoadBalanceWithSessionSticknessRule(@Injectable Invocation invocation) {
    SessionStickinessRule rule = new SessionStickinessRule();
    LoadBalancerCreator lbCreator = new LoadBalancerCreator(rule, "service");

    List<Server> servers = new ArrayList<>();
    Server server = new Server("host1", 80);
    server.setAlive(true);
    Server server2 = new Server("host2", 80);
    server2.setAlive(true);
    servers.add(server);
    servers.add(server2);
    lbCreator.setServerList(servers);
    lbCreator.setFilters(new ArrayList<>());
    LoadBalancer lb = lbCreator.createLoadBalancer(invocation);
    Server s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
    s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);

    long time = Deencapsulation.getField(rule, "lastAccessedTime");
    Deencapsulation.setField(rule, "lastAccessedTime", time - 1000 * 300);
    ArchaiusUtils.setProperty("cse.loadbalance.service.SessionStickinessRule.sessionTimeoutInSeconds", 9);
    s = lb.chooseServer("test");
    Assert.assertEquals(server, s);

    ArchaiusUtils.setProperty("cse.loadbalance.service.SessionStickinessRule.successiveFailedTimes", 5);
    lb.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(s);
    lb.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(s);
    lb.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(s);
    lb.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(s);
    lb.getLoadBalancerStats().incrementSuccessiveConnectionFailureCount(s);
    s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
  }
}
