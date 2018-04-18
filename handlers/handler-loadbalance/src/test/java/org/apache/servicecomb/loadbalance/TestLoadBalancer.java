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

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.loadbalance.filter.IsolationServerListFilter;
import org.apache.servicecomb.loadbalance.filter.SimpleTransactionControlFilter;
import org.apache.servicecomb.loadbalance.filter.TransactionControlFilter;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.loadbalancer.AbstractLoadBalancer.ServerGroup;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.WeightedResponseTimeRule;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestLoadBalancer {
  private IRule rule = Mockito.mock(IRule.class);

  private LoadBalancer loadBalancer = new LoadBalancer("loadBalancerName", rule, "test");

  @Test
  public void name() {
    Assert.assertEquals("loadBalancerName", loadBalancer.getName());
  }

  @Test
  public void testLoadBalancerFullOperationWithoutException() {
    List<Server> newServers = new ArrayList<>();
    Server server = Mockito.mock(Server.class);
    newServers.add(server);

    loadBalancer.setServerList(newServers);
    loadBalancer.chooseServer();

    Object key = Mockito.mock(Object.class);

    loadBalancer.chooseServer(key);
    loadBalancer.getAllServers();
    loadBalancer.getServerList(true);
    loadBalancer.getServerList(false);
    loadBalancer.getLoadBalancerStats();
    loadBalancer.getReachableServers();

    assertNotNull(loadBalancer.getAllServers());
  }

  @Test
  public void testAddServerException() {
    boolean status = true;
    List<Server> newServers = new ArrayList<>();
    Server server = Mockito.mock(Server.class);

    newServers.add(server);

    try {

      loadBalancer.addServers(newServers);
    } catch (Exception e) {

      status = false;

      Assert.assertEquals("Not implemented.", e.getMessage());
    }

    Assert.assertFalse(status);
  }

  @Test
  public void testServerListException() {
    boolean status = true;
    List<Server> newServers = new ArrayList<>();
    Server server = Mockito.mock(Server.class);

    newServers.add(server);

    try {

      loadBalancer.getServerList(ServerGroup.ALL);
    } catch (Exception e) {

      status = false;

      Assert.assertEquals("Not implemented.", e.getMessage());
    }

    Assert.assertFalse(status);
  }

  @Test
  public void testMarkServerDownException() {
    boolean status = true;
    List<Server> newServers = new ArrayList<>();
    Server server = Mockito.mock(Server.class);

    newServers.add(server);

    try {

      loadBalancer.markServerDown(server);
    } catch (Exception e) {

      status = false;

      Assert.assertEquals("Not implemented.", e.getMessage());
    }

    Assert.assertFalse(status);
  }

  @Test
  public void testFilter() {
    Assert.assertEquals(0, loadBalancer.getFilterSize());

    TransactionControlFilter filter = new SimpleTransactionControlFilter();
    loadBalancer.putFilter("test", filter);
    Assert.assertEquals(1, loadBalancer.getFilterSize());
  }

  @Test
  public void testGetAllServers() {
    List<Server> servers = new ArrayList<>();
    Server server = Mockito.mock(Server.class);
    servers.add(server);
    loadBalancer.setServerList(servers);

    TransactionControlFilter filter = Mockito.mock(TransactionControlFilter.class);
    Mockito.when(filter.getFilteredListOfServers(servers)).thenReturn(servers);
    Assert.assertEquals(servers, loadBalancer.getAllServers());
  }

  @Test
  public void testLoadBalanceWithRoundRobinRuleAndFilter() {
    // Robin components implementations require getReachableServers & getServerList have the same size, we add a test case for this.
    RoundRobinRule rule = new RoundRobinRule();
    LoadBalancer lb = new LoadBalancer("lb1", rule, "service");
    List<Server> servers = new ArrayList<>();
    Server server = new Server("host1", 80);
    server.setAlive(true);
    Server server2 = new Server("host2", 80);
    server2.setAlive(true);
    servers.add(server);
    servers.add(server2);
    lb.setServerList(servers);
    lb.putFilter("testFiler", new ServerListFilterExt() {
      @Override
      public List<Server> getFilteredListOfServers(List<Server> servers) {
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
    Server s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
    s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
    s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
  }

  @Test
  public void testLoadBalanceWithRandomRuleAndFilter() {
    // Robin components implementations require getReachableServers & getServerList have the same size, we add a test case for this.
    RandomRule rule = new RandomRule();
    LoadBalancer lb = new LoadBalancer("lb1", rule, "service");
    List<Server> servers = new ArrayList<>();
    Server server = new Server("host1", 80);
    server.setAlive(true);
    Server server2 = new Server("host2", 80);
    server2.setAlive(true);
    servers.add(server);
    servers.add(server2);
    lb.setServerList(servers);
    lb.putFilter("testFiler", new ServerListFilterExt() {
      @Override
      public List<Server> getFilteredListOfServers(List<Server> servers) {
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
    Server s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
    s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
    s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
  }

  @Test
  public void testLoadBalanceWithWeightedResponseTimeRuleAndFilter(@Mocked CseServer server,
      @Mocked CseServer server2) {
    // Robin components implementations require getReachableServers & getServerList have the same size, we add a test case for this.
    WeightedResponseTimeRule rule = new WeightedResponseTimeRule();
    LoadBalancer lb = new LoadBalancer("lb1", rule, "service");
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
    lb.setServerList(servers);
    SimpleTransactionControlFilter simpleFilter = new SimpleTransactionControlFilter();
    simpleFilter.setMicroserviceName("service");
    IsolationServerListFilter isolationFilter = new IsolationServerListFilter();
    isolationFilter.setMicroserviceName("service");
    lb.putFilter("simpleFilter", simpleFilter);
    lb.putFilter("isolationFilter", isolationFilter);
    lb.putFilter("testFiler", new ServerListFilterExt() {
      @Override
      public List<Server> getFilteredListOfServers(List<Server> servers) {
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
    Server s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
    s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
    s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
  }

  @Test
  public void testLoadBalanceWithSessionSticknessRule() {
    SessionStickinessRule rule = new SessionStickinessRule();
    LoadBalancer lb = new LoadBalancer("lb1", rule, "service");
    Assert.assertEquals(lb.getMicroServiceName(), "service");
    Assert.assertEquals("service", Deencapsulation.getField(rule, "microserviceName"));

    List<Server> servers = new ArrayList<>();
    Server server = new Server("host1", 80);
    server.setAlive(true);
    Server server2 = new Server("host2", 80);
    server2.setAlive(true);
    servers.add(server);
    servers.add(server2);
    lb.setServerList(servers);

    Server s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);
    s = lb.chooseServer("test");
    Assert.assertEquals(server2, s);

    long time = Deencapsulation.getField(rule, "lastAccessedTime");
    Deencapsulation.setField(rule, "lastAccessedTime", time - 1000 * 10);
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
