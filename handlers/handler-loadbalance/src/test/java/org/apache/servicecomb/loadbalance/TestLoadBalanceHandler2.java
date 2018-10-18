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

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.api.registry.DataCenterInfo;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManager;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTreeNode;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 *
 */
public class TestLoadBalanceHandler2 {
  @BeforeClass
  public static void beforeClass() {
    //prepare for defineEndpointAndHandle
    ArchaiusUtils.setProperty("servicecomb.loadbalance.userDefinedEndpoint.enabled", "true");
    // avoid mock
    ServiceCombLoadBalancerStats.INSTANCE.init();
  }

  @After
  public void teardown() {
    CseContext.getInstance().setTransportManager(null);
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testZoneAwareAndIsolationFilterWorks() throws Exception {
    ReferenceConfig referenceConfig = Mockito.mock(ReferenceConfig.class);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);
    when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    MicroserviceMeta microserviceMeta = Mockito.mock(MicroserviceMeta.class);
    when(schemaMeta.getMicroserviceMeta()).thenReturn(microserviceMeta);
    when(schemaMeta.getMicroserviceName()).thenReturn("testMicroserviceName");
    when(microserviceMeta.getAppId()).thenReturn("testApp");
    when(referenceConfig.getVersionRule()).thenReturn("0.0.0+");
    when(referenceConfig.getTransport()).thenReturn("rest");
    Invocation invocation = new Invocation(referenceConfig, operationMeta, new Object[0]);

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

    when(serviceRegistry.getMicroserviceInstance()).thenReturn(myself);
    when(serviceRegistry.getInstanceCacheManager()).thenReturn(instanceCacheManager);
    when(instanceCacheManager.getOrCreateVersionedCache("testApp", "testMicroserviceName", "0.0.0+"))
        .thenReturn(parent);
    when(transportManager.findTransport("rest")).thenReturn(transport);

    LoadbalanceHandler handler = null;
    LoadBalancer loadBalancer = null;
    ServiceCombServer server = null;

    handler = new LoadbalanceHandler();
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer(invocation);
    Assert.assertEquals(server, null);

    data.put("noneMatchInstance", noneMatchInstance);
    parent.cacheVersion(1);
    handler = new LoadbalanceHandler();
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer(invocation);
    Assert.assertEquals(server.getEndpoint().getEndpoint(), "rest://localhost:9092");

    data.put("regionMatchInstance", regionMatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer(invocation);
    Assert.assertEquals(server.getEndpoint().getEndpoint(), "rest://localhost:9091");

    data.put("allmatchInstance", allmatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer(invocation);
    Assert.assertEquals(server.getEndpoint().getEndpoint(), "rest://localhost:9090");

    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server);

    //if errorThresholdPercentage is 0,that means errorThresholdPercentage is not active.
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.errorThresholdPercentage", "0");
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer(invocation);
    Assert.assertEquals(server.getEndpoint().getEndpoint(), "rest://localhost:9090");

    //if errorThresholdPercentage greater than 0, it will activate.
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.errorThresholdPercentage", "20");
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.minIsolationTime", "10");
    ServiceCombServer server2 = server;
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer(invocation);
    Assert.assertEquals(server.getEndpoint().getEndpoint(), "rest://localhost:9091");
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server2);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer(invocation);
    Assert.assertEquals(server.getEndpoint().getEndpoint(), "rest://localhost:9091");
    TimeUnit.MILLISECONDS.sleep(20);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer(invocation);
    Assert.assertEquals(server.getEndpoint().getEndpoint(), "rest://localhost:9090");
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server2);
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server2);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = (ServiceCombServer) loadBalancer.chooseServer(invocation);
    Assert.assertEquals(server.getEndpoint().getEndpoint(), "rest://localhost:9091");
  }

  @Test
  public void testConfigEndpoint() {
    ReferenceConfig referenceConfig = Mockito.mock(ReferenceConfig.class);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);
    when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    MicroserviceMeta microserviceMeta = Mockito.mock(MicroserviceMeta.class);
    when(schemaMeta.getMicroserviceMeta()).thenReturn(microserviceMeta);
    when(schemaMeta.getMicroserviceName()).thenReturn("testMicroserviceName");
    when(microserviceMeta.getAppId()).thenReturn("testApp");
    when(referenceConfig.getVersionRule()).thenReturn("0.0.0+");
    when(referenceConfig.getTransport()).thenReturn("rest");
    Invocation invocation = new Invocation(referenceConfig, operationMeta, new Object[0]);
    AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

    InstanceCacheManager instanceCacheManager = Mockito.mock(InstanceCacheManager.class);
    ServiceRegistry serviceRegistry = Mockito.mock(ServiceRegistry.class);
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    Transport transport = Mockito.mock(Transport.class);
    ArchaiusUtils.setProperty("servicecomb.loadbalance.filter.operation.enabled", "false");

    // set up data
    MicroserviceInstance myself = new MicroserviceInstance();

    MicroserviceInstance findInstance = new MicroserviceInstance();
    List<String> findEndpoint = new ArrayList<>();
    findEndpoint.add("rest://localhost:9092");
    findInstance.setEndpoints(findEndpoint);
    findInstance.setInstanceId("findInstance");

    Map<String, MicroserviceInstance> data = new HashMap<>();
    DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent").data(data);
    CseContext.getInstance().setTransportManager(transportManager);
    SCBEngine.getInstance().setTransportManager(transportManager);

    RegistryUtils.setServiceRegistry(serviceRegistry);

    when(serviceRegistry.getMicroserviceInstance()).thenReturn(myself);
    when(serviceRegistry.getInstanceCacheManager()).thenReturn(instanceCacheManager);
    when(instanceCacheManager.getOrCreateVersionedCache("testApp", "testMicroserviceName", "0.0.0+"))
        .thenReturn(parent);
    when(transportManager.findTransport("rest")).thenReturn(transport);

    LoadbalanceHandler handler = null;

    handler = new LoadbalanceHandler();
    data.put("findInstance", findInstance);
    parent.cacheVersion(1);
    handler = new LoadbalanceHandler();
    try {
      handler.handle(invocation, asyncResp);
    } catch (Exception e) {

    }
    Assert.assertEquals("rest://localhost:9092", invocation.getEndpoint().getEndpoint());

    //success
    invocation.addLocalContext("scb-endpoint", "rest://127.0.0.1:8080?sslEnabled=true&protocol=http2");
    try {
      handler.handle(invocation, asyncResp);
    } catch (Exception e) {

    }
    Assert.assertEquals("rest://127.0.0.1:8080?sslEnabled=true&protocol=http2", invocation.getEndpoint().getEndpoint());

    //endpoint format is not correct
    invocation.addLocalContext("scb-endpoint", "127.0.0.1:8080");
    try {
      handler.handle(invocation, asyncResp);
      Assert.assertEquals("endpoint's format is not correct, throw exception", " but not throw exception");
    } catch (Exception e) {
      Assert.assertTrue(e.getMessage()
          .contains("Illegal character in scheme name"));
    }

    //transport is not find
    invocation.addLocalContext("scb-endpoint", "my://127.0.0.1:8080?sslEnabled=true&protocol=http2");
    try {
      handler.handle(invocation, asyncResp);
      Assert.assertEquals("endpoint's transport not found, throw exception", "but not throw exception");
    } catch (Exception e) {
      Assert.assertTrue(e.getMessage().contains("the endpoint's transport is not found."));
    }
  }
}
