/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.loadbalance;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.loadbalancer.AbstractLoadBalancer.ServerGroup;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;

import io.servicecomb.loadbalance.filter.SimpleTransactionControlFilter;
import io.servicecomb.loadbalance.filter.TransactionControlFilter;

public class TestLoadBalancer {

  private CseServerList serverList = Mockito.mock(CseServerList.class);

  private IRule rule = Mockito.mock(IRule.class);

  private LoadBalancer loadBalancer = new LoadBalancer(serverList, rule);

  @Test
  public void testLoadBalancerFullOperationWithoutException() {

    List<Server> newServers = new ArrayList<Server>();
    Server server = Mockito.mock(Server.class);
    newServers.add(server);

    loadBalancer.chooseServer();

    Object key = Mockito.mock(Object.class);

    loadBalancer.chooseServer(key);
    loadBalancer.getAllServers();
    loadBalancer.getLoadBalancerStats();
    loadBalancer.getReachableServers();

    assertNotNull(loadBalancer.getAllServers());
  }

  @Test
  public void testAddServerException() {
    boolean status = true;
    List<Server> newServers = new ArrayList<Server>();
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
    List<Server> newServers = new ArrayList<Server>();
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
    List<Server> newServers = new ArrayList<Server>();
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
    List<Server> servers = new ArrayList<Server>();
    Server server = Mockito.mock(Server.class);
    servers.add(server);
    Mockito.when(serverList.getInitialListOfServers()).thenReturn(servers);

    TransactionControlFilter filter = Mockito.mock(TransactionControlFilter.class);
    Mockito.when(filter.getFilteredListOfServers(servers)).thenReturn(servers);
    Assert.assertEquals(servers, loadBalancer.getAllServers());
  }
}
