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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.api.registry.DataCenterInfo;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManager;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTreeNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 *
 */
public class TestLoadBalanceHandler2 {

  @After
  public void teardown() {
    CseContext.getInstance().setTransportManager(null);
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testZoneAwareAndIsolationFilterWorks() {
    Invocation invocation = Mockito.mock(Invocation.class);
    InstanceCacheManager instanceCacheManager = Mockito.mock(InstanceCacheManager.class);
    ServiceRegistry serviceRegistry = Mockito.mock(ServiceRegistry.class);
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    Transport transport = Mockito.mock(Transport.class);
    ArchaiusUtils.setProperty("servicecomb.loadbalance.filter.operation.enabled", "false");

    // set up data
    MicroserviceInstance myself = new MicroserviceInstance();
    DataCenterInfo info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region");
    info.setAvailableZone("test-zone");
    myself.setDataCenterInfo(info);

    MicroserviceInstance allmatchInstance = new MicroserviceInstance();
    info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region");
    info.setAvailableZone("test-zone");
    List<String> allMatchEndpoint = new ArrayList<>();
    allMatchEndpoint.add("rest://localhost:9090");
    allmatchInstance.setEndpoints(allMatchEndpoint);
    allmatchInstance.setDataCenterInfo(info);
    allmatchInstance.setInstanceId("allmatchInstance");

    MicroserviceInstance regionMatchInstance = new MicroserviceInstance();
    info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region");
    info.setAvailableZone("test-zone2");
    List<String> regionMatchEndpoint = new ArrayList<>();
    regionMatchEndpoint.add("rest://localhost:9091");
    regionMatchInstance.setEndpoints(regionMatchEndpoint);
    regionMatchInstance.setDataCenterInfo(info);
    regionMatchInstance.setInstanceId("regionMatchInstance");

    MicroserviceInstance noneMatchInstance = new MicroserviceInstance();
    info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region2");
    info.setAvailableZone("test-zone2");
    List<String> noMatchEndpoint = new ArrayList<>();
    noMatchEndpoint.add("rest://localhost:9092");
    noneMatchInstance.setEndpoints(noMatchEndpoint);
    noneMatchInstance.setDataCenterInfo(info);
    noneMatchInstance.setInstanceId("noneMatchInstance");

    Map<String, MicroserviceInstance> data = new HashMap<>();
    DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent").data(data);
    CseContext.getInstance().setTransportManager(transportManager);

    RegistryUtils.setServiceRegistry(serviceRegistry);

    Mockito.when(invocation.getAppId()).thenReturn("testApp");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("testMicroserviceName");
    Mockito.when(invocation.getMicroserviceVersionRule()).thenReturn("0.0.0+");
    Mockito.when(invocation.getConfigTransportName()).thenReturn("rest");
    Mockito.when(serviceRegistry.getMicroserviceInstance()).thenReturn(myself);
    Mockito.when(serviceRegistry.getInstanceCacheManager()).thenReturn(instanceCacheManager);
    Mockito.when(instanceCacheManager.getOrCreateVersionedCache("testApp", "testMicroserviceName", "0.0.0+"))
        .thenReturn(parent);
    Mockito.when(transportManager.findTransport("rest")).thenReturn(transport);

    LoadbalanceHandler handler = null;
    LoadBalancer loadBalancer = null;
    ServiceCombServer server = null;

    handler = new LoadbalanceHandler();
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer();
    Assert.assertEquals(server, null);

    data.put("noneMatchInstance", noneMatchInstance);
    parent.cacheVersion(1);
    handler = new LoadbalanceHandler();
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer();
    Assert.assertEquals(server.getEndpoint().getEndpoint(), "rest://localhost:9092");

    data.put("regionMatchInstance", regionMatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer();
    Assert.assertEquals(server.getEndpoint().getEndpoint(), "rest://localhost:9091");

    data.put("allmatchInstance", allmatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer();
    Assert.assertEquals(server.getEndpoint().getEndpoint(), "rest://localhost:9090");

    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server);
    ServiceCombServer server2 = server;
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer();
    Assert.assertEquals(server.getEndpoint().getEndpoint(), "rest://localhost:9091");
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server2);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer();
    Assert.assertEquals(server.getEndpoint().getEndpoint(), "rest://localhost:9090");
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server2);
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server2);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer();
    Assert.assertEquals(server.getEndpoint().getEndpoint(), "rest://localhost:9091");
  }
}
