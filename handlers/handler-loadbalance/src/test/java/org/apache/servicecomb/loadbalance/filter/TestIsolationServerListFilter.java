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
import java.util.List;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.loadbalance.CseServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.Server;

import mockit.Deencapsulation;

public class TestIsolationServerListFilter {

  IsolationServerListFilter IsolationServerListFilter = null;

  LoadBalancerStats loadBalancerStats = null;

  @BeforeClass
  public static void initConfig() throws Exception {
    ConfigUtil.installDynamicConfig();
  }

  @AfterClass
  public static void classTeardown() {
    Deencapsulation.setField(ConfigurationManager.class, "instance", null);
    Deencapsulation.setField(ConfigurationManager.class, "customConfigurationInstalled", false);
    Deencapsulation.setField(DynamicPropertyFactory.class, "config", null);
  }

  @Before
  public void setUp() throws Exception {
    IsolationServerListFilter = new IsolationServerListFilter();
    loadBalancerStats = new LoadBalancerStats("loadBalancer");

    AbstractConfiguration configuration =
        (AbstractConfiguration) DynamicPropertyFactory.getBackingConfigurationSource();
    configuration.clearProperty("cse.loadbalance.isolation.enabled");
    configuration.addProperty("cse.loadbalance.isolation.enabled",
        "true");
    configuration.clearProperty("cse.loadbalance.isolation.enableRequestThreshold");
    configuration.addProperty("cse.loadbalance.isolation.enableRequestThreshold",
        "3");
  }

  @After
  public void tearDown() throws Exception {
    IsolationServerListFilter = null;
    loadBalancerStats = null;

    AbstractConfiguration configuration =
        (AbstractConfiguration) DynamicPropertyFactory.getBackingConfigurationSource();
    configuration.clearProperty("cse.loadbalance.isolation.continuousFailureThreshold");
  }

  @Test
  public void testSetLoadBalancerStats() {
    IsolationServerListFilter.setLoadBalancerStats(loadBalancerStats);
    Assert.assertNotNull(IsolationServerListFilter.getLoadBalancerStats());
    Assert.assertEquals(loadBalancerStats, IsolationServerListFilter.getLoadBalancerStats());
  }

  @Test
  public void testSetMicroserviceName() {
    IsolationServerListFilter.setMicroserviceName("microserviceName");
    Assert.assertNotNull(IsolationServerListFilter.getMicroserviceName());
    Assert.assertEquals("microserviceName", IsolationServerListFilter.getMicroserviceName());
  }

  @Test
  public void testGetFilteredListOfServers() {
    Invocation invocation = Mockito.mock(Invocation.class);
    CseServer testServer = Mockito.mock(CseServer.class);
    Mockito.when(invocation.getMicroserviceName()).thenReturn("microserviceName");
    Mockito.when(testServer.getLastVisitTime()).thenReturn(System.currentTimeMillis());

    List<Server> serverList = new ArrayList<>();
    serverList.add(testServer);
    IsolationServerListFilter.setLoadBalancerStats(loadBalancerStats);
    IsolationServerListFilter.setInvocation(invocation);
    List<Server> returnedServerList = IsolationServerListFilter.getFilteredListOfServers(serverList);
    Assert.assertEquals(returnedServerList.size(), 1);

    loadBalancerStats.incrementNumRequests(testServer);
    loadBalancerStats.incrementNumRequests(testServer);
    loadBalancerStats.incrementNumRequests(testServer);
    loadBalancerStats.incrementSuccessiveConnectionFailureCount(testServer);
    returnedServerList = IsolationServerListFilter.getFilteredListOfServers(serverList);
    Assert.assertEquals(returnedServerList.size(), 0);
  }

  @Test
  public void testGetFilteredListOfServersOnContinuousFailureReachesThreshold() {
    ((AbstractConfiguration) DynamicPropertyFactory.getBackingConfigurationSource())
        .addProperty("cse.loadbalance.isolation.continuousFailureThreshold",
            "3");
    Invocation invocation = Mockito.mock(Invocation.class);
    CseServer testServer = Mockito.mock(CseServer.class);
    Mockito.when(invocation.getMicroserviceName()).thenReturn("microserviceName");
    Mockito.when(testServer.getCountinuousFailureCount()).thenReturn(3);
    Mockito.when(testServer.getLastVisitTime()).thenReturn(System.currentTimeMillis());

    for (int i = 0; i < 3; ++i) {
      loadBalancerStats.incrementNumRequests(testServer);
    }

    List<Server> serverList = new ArrayList<>();
    serverList.add(testServer);
    IsolationServerListFilter.setLoadBalancerStats(loadBalancerStats);
    IsolationServerListFilter.setInvocation(invocation);
    List<Server> returnedServerList = IsolationServerListFilter.getFilteredListOfServers(serverList);
    Assert.assertEquals(0, returnedServerList.size());
  }

  @Test
  public void testGetFilteredListOfServersOnContinuousFailureIsBelowThreshold() {
    ((AbstractConfiguration) DynamicPropertyFactory.getBackingConfigurationSource())
        .addProperty("cse.loadbalance.isolation.continuousFailureThreshold",
            "3");
    Invocation invocation = Mockito.mock(Invocation.class);
    CseServer testServer = Mockito.mock(CseServer.class);
    Mockito.when(invocation.getMicroserviceName()).thenReturn("microserviceName");
    Mockito.when(testServer.getCountinuousFailureCount()).thenReturn(2);
    Mockito.when(testServer.getLastVisitTime()).thenReturn(System.currentTimeMillis());

    for (int i = 0; i < 3; ++i) {
      loadBalancerStats.incrementNumRequests(testServer);
    }

    List<Server> serverList = new ArrayList<>();
    serverList.add(testServer);
    IsolationServerListFilter.setLoadBalancerStats(loadBalancerStats);
    IsolationServerListFilter.setInvocation(invocation);
    List<Server> returnedServerList = IsolationServerListFilter.getFilteredListOfServers(serverList);
    Assert.assertEquals(1, returnedServerList.size());
  }
}
