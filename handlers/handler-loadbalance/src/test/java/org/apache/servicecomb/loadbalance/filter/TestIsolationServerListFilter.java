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
import org.apache.servicecomb.foundation.common.event.AlarmEvent;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.loadbalance.CseServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.eventbus.Subscribe;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.Server;

public class TestIsolationServerListFilter {

  IsolationServerListFilter isolationServerListFilter = null;

  LoadBalancerStats loadBalancerStats = null;

  private List<AlarmEvent> taskList = null;

  Object receiveEvent = null;

  @BeforeClass
  public static void initConfig() throws Exception {
    ConfigUtil.installDynamicConfig();
  }

  @AfterClass
  public static void classTeardown() {
    ArchaiusUtils.resetConfig();
  }

  @Before
  public void setUp() throws Exception {
    isolationServerListFilter = new IsolationServerListFilter();
    loadBalancerStats = new LoadBalancerStats("loadBalancer");
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.enabled",
        "true");
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.enableRequestThreshold",
        "3");

    taskList = new ArrayList<>();
    receiveEvent = new Object() {
      @Subscribe
      public void onEvent(AlarmEvent isolationServerEvent) {
        taskList.add(isolationServerEvent);
      }
    };
    isolationServerListFilter.eventBus.register(receiveEvent);
  }

  @After
  public void tearDown() throws Exception {
    isolationServerListFilter.eventBus.unregister(receiveEvent);
    isolationServerListFilter = null;
    loadBalancerStats = null;

    AbstractConfiguration configuration =
        (AbstractConfiguration) DynamicPropertyFactory.getBackingConfigurationSource();
    configuration.clearProperty("servicecomb.loadbalance.isolation.continuousFailureThreshold");
  }

  @Test
  public void testSetLoadBalancerStats() {
    isolationServerListFilter.setLoadBalancerStats(loadBalancerStats);
    Assert.assertNotNull(isolationServerListFilter.getLoadBalancerStats());
    Assert.assertEquals(loadBalancerStats, isolationServerListFilter.getLoadBalancerStats());
  }

  @Test
  public void testSetMicroserviceName() {
    isolationServerListFilter.setMicroserviceName("microserviceName");
    Assert.assertNotNull(isolationServerListFilter.getMicroserviceName());
    Assert.assertEquals("microserviceName", isolationServerListFilter.getMicroserviceName());
  }

  @Test
  public void testGetFilteredListOfServers() {
    Invocation invocation = Mockito.mock(Invocation.class);
    CseServer testServer = Mockito.mock(CseServer.class);
    Mockito.when(invocation.getMicroserviceName()).thenReturn("microserviceName");
    Mockito.when(testServer.getLastVisitTime()).thenReturn(System.currentTimeMillis());

    List<Server> serverList = new ArrayList<>();
    serverList.add(testServer);
    isolationServerListFilter.setLoadBalancerStats(loadBalancerStats);
    isolationServerListFilter.setInvocation(invocation);
    List<Server> returnedServerList = isolationServerListFilter.getFilteredListOfServers(serverList);
    Assert.assertEquals(returnedServerList.size(), 1);

    loadBalancerStats.incrementNumRequests(testServer);
    loadBalancerStats.incrementNumRequests(testServer);
    loadBalancerStats.incrementNumRequests(testServer);
    loadBalancerStats.incrementSuccessiveConnectionFailureCount(testServer);
    returnedServerList = isolationServerListFilter.getFilteredListOfServers(serverList);
    Assert.assertEquals(returnedServerList.size(), 0);
  }

  @Test
  public void testGetFilteredListOfServersOnContinuousFailureReachesThreshold() {
    ((AbstractConfiguration) DynamicPropertyFactory.getBackingConfigurationSource())
        .addProperty("servicecomb.loadbalance.isolation.continuousFailureThreshold",
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
    isolationServerListFilter.setLoadBalancerStats(loadBalancerStats);
    isolationServerListFilter.setInvocation(invocation);
    List<Server> returnedServerList = isolationServerListFilter.getFilteredListOfServers(serverList);
    Assert.assertEquals(0, returnedServerList.size());
    Assert.assertEquals(1, taskList.size());
  }

  @Test
  public void testGetFilteredListOfServersOnContinuousFailureIsBelowThreshold() {
    ((AbstractConfiguration) DynamicPropertyFactory.getBackingConfigurationSource())
        .addProperty("servicecomb.loadbalance.isolation.continuousFailureThreshold",
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
    isolationServerListFilter.setLoadBalancerStats(loadBalancerStats);
    isolationServerListFilter.setInvocation(invocation);
    List<Server> returnedServerList = isolationServerListFilter.getFilteredListOfServers(serverList);
    Assert.assertEquals(1, returnedServerList.size());
  }
}
