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

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.NonSwaggerInvocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.definition.InvocationRuntimeType;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.loadbalance.event.IsolationServerEvent;
import org.apache.servicecomb.loadbalance.filter.ServerDiscoveryFilter;
import org.apache.servicecomb.localregistry.LocalRegistryStore;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.registry.DataCenterInfo;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.cache.InstanceCacheManager;
import org.apache.servicecomb.registry.discovery.DiscoveryTree;
import org.apache.servicecomb.registry.discovery.DiscoveryTreeNode;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import com.google.common.eventbus.Subscribe;

import mockit.Mock;
import mockit.MockUp;

import static org.mockito.Mockito.when;

public class TestLoadBalanceHandler2 {
  private Holder<Long> mockTimeMillis;

  private static SCBEngine scbEngine;

  @BeforeClass
  public static void beforeClass() {
    ConfigUtil.installDynamicConfig();
    ArchaiusUtils.setProperty("servicecomb.loadbalance.userDefinedEndpoint.enabled", "true");
    scbEngine = SCBBootstrap.createSCBEngineForTest().run();
  }

  @AfterClass
  public static void afterClass() {
    scbEngine.destroy();
    ArchaiusUtils.resetConfig();
  }

  @Before
  public void setUp() {

    // avoid mock
    ServiceCombLoadBalancerStats.INSTANCE.init();
    TestServiceCombServerStats.releaseTryingChance();

    mockTimeMillis = new Holder<>(1L);
    new MockUp<System>() {
      @Mock
      long currentTimeMillis() {
        return mockTimeMillis.value;
      }
    };
  }

  @After
  public void teardown() {
    TestServiceCombServerStats.releaseTryingChance();
  }

  @Test
  public void testZoneAwareAndIsolationFilterWorks() throws Exception {
    ReferenceConfig referenceConfig = Mockito.mock(ReferenceConfig.class);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    InvocationRuntimeType invocationRuntimeType = Mockito.mock(InvocationRuntimeType.class);
    SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);
    when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    MicroserviceMeta microserviceMeta = Mockito.mock(MicroserviceMeta.class);
    when(schemaMeta.getMicroserviceMeta()).thenReturn(microserviceMeta);
    when(schemaMeta.getMicroserviceName()).thenReturn("testMicroserviceName");
    when(microserviceMeta.getAppId()).thenReturn("testApp");
    when(referenceConfig.getVersionRule()).thenReturn("0.0.0+");
    when(referenceConfig.getTransport()).thenReturn("rest");
    Invocation invocation = new Invocation(referenceConfig, operationMeta, invocationRuntimeType, new HashMap<>());

    InstanceCacheManager instanceCacheManager = Mockito.mock(InstanceCacheManager.class);
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
    scbEngine.setTransportManager(transportManager);
    LocalRegistryStore.INSTANCE.initSelfWithMocked(null, myself);
    mockUpInstanceCacheManager(instanceCacheManager);
    when(instanceCacheManager.getOrCreateVersionedCache("testApp", "testMicroserviceName", "0.0.0+"))
        .thenReturn(parent);
    when(transportManager.findTransport("rest")).thenReturn(transport);

    LoadbalanceHandler handler = null;
    LoadBalancer loadBalancer = null;
    ServiceCombServer server = null;

    handler = new LoadbalanceHandler();
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertNull(server);

    data.put("noneMatchInstance", noneMatchInstance);
    parent.cacheVersion(1);
    handler = new LoadbalanceHandler();
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9092", server.getEndpoint().getEndpoint());

    data.put("regionMatchInstance", regionMatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9091", server.getEndpoint().getEndpoint());

    data.put("allmatchInstance", allmatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9090", server.getEndpoint().getEndpoint());

    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server);

    //if errorThresholdPercentage is 0,that means errorThresholdPercentage is not active.
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.errorThresholdPercentage", "0");
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9090", server.getEndpoint().getEndpoint());

    //if errorThresholdPercentage greater than 0, it will activate.
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.errorThresholdPercentage", "20");
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.minIsolationTime", "10");
    ServiceCombServer server2 = server;
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9091", server.getEndpoint().getEndpoint());
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server2);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9091", server.getEndpoint().getEndpoint());
    mockDelayMillis(20);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9090", server.getEndpoint().getEndpoint());
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server2);
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server2);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9091", server.getEndpoint().getEndpoint());
  }

  public static class IsolationEndpointListener {
    Holder<Integer> count;

    public IsolationEndpointListener(Holder<Integer> count) {
      this.count = count;
    }

    @Subscribe
    public void listener(IsolationServerEvent event) {
      count.value++;
      Assertions.assertSame("Isolation Endpoint", "rest://localhost:9090", event.getEndpoint().getEndpoint());
    }
  }

  @Test
  public void testIsolationEventWithEndpoint() throws Exception {
    ReferenceConfig referenceConfig = Mockito.mock(ReferenceConfig.class);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    InvocationRuntimeType invocationRuntimeType = Mockito.mock(InvocationRuntimeType.class);
    SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);
    when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    MicroserviceMeta microserviceMeta = Mockito.mock(MicroserviceMeta.class);
    when(schemaMeta.getMicroserviceMeta()).thenReturn(microserviceMeta);
    when(schemaMeta.getMicroserviceName()).thenReturn("testMicroserviceName");
    when(microserviceMeta.getAppId()).thenReturn("testApp");
    when(referenceConfig.getVersionRule()).thenReturn("0.0.0+");
    when(referenceConfig.getTransport()).thenReturn("rest");
    Invocation invocation = new Invocation(referenceConfig, operationMeta, invocationRuntimeType, new HashMap<>());

    InstanceCacheManager instanceCacheManager = Mockito.mock(InstanceCacheManager.class);
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    Transport transport = Mockito.mock(Transport.class);
    ArchaiusUtils.setProperty("servicecomb.loadbalance.filter.operation.enabled", "false");

    // set up data
    MicroserviceInstance myself = new MicroserviceInstance();
    DataCenterInfo info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test");
    info.setAvailableZone("test");
    myself.setDataCenterInfo(info);

    MicroserviceInstance instance = new MicroserviceInstance();
    info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test");
    info.setAvailableZone("test");
    List<String> allMatchEndpoint = new ArrayList<>();
    allMatchEndpoint.add("rest://localhost:9090");
    instance.setEndpoints(allMatchEndpoint);
    instance.setDataCenterInfo(info);
    instance.setInstanceId("instance");

    Map<String, MicroserviceInstance> data = new HashMap<>();
    DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent").data(data);
    scbEngine.setTransportManager(transportManager);

    LocalRegistryStore.INSTANCE.initSelfWithMocked(null, myself);
    mockUpInstanceCacheManager(instanceCacheManager);
    when(instanceCacheManager.getOrCreateVersionedCache("testApp", "testMicroserviceName", "0.0.0+"))
        .thenReturn(parent);
    when(transportManager.findTransport("rest")).thenReturn(transport);

    LoadbalanceHandler handler = null;
    LoadBalancer loadBalancer = null;
    ServiceCombServer server = null;

    handler = new LoadbalanceHandler();
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertNull(server);

    data.put("instance", instance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9090", server.getEndpoint().getEndpoint());

    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server);

    //if errorThresholdPercentage greater than 0, it will activate.
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.errorThresholdPercentage", "10");
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.minIsolationTime", "10");

    Holder<Integer> count = new Holder<>(0);
    IsolationEndpointListener isolationEndpointListener = new IsolationEndpointListener(count);
    EventManager.getEventBus().register(isolationEndpointListener);
    Assertions.assertEquals(0, count.value.intValue());
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    // no server is available
    Assertions.assertNull(server);
    Assertions.assertEquals(1, count.value.intValue());
    EventManager.unregister(isolationEndpointListener);
  }

  @Test
  public void testZoneAwareAndIsolationFilterWorksEmptyInstanceProtectionEnabled() throws Exception {
    ArchaiusUtils.setProperty("servicecomb.loadbalance.filter.isolation.emptyInstanceProtectionEnabled", "true");
    ReferenceConfig referenceConfig = Mockito.mock(ReferenceConfig.class);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    InvocationRuntimeType invocationRuntimeType = Mockito.mock(InvocationRuntimeType.class);
    SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);
    when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    MicroserviceMeta microserviceMeta = Mockito.mock(MicroserviceMeta.class);
    when(schemaMeta.getMicroserviceMeta()).thenReturn(microserviceMeta);
    when(schemaMeta.getMicroserviceName()).thenReturn("testMicroserviceName");
    when(microserviceMeta.getAppId()).thenReturn("testApp");
    when(referenceConfig.getVersionRule()).thenReturn("0.0.0+");
    when(referenceConfig.getTransport()).thenReturn("rest");
    Invocation invocation = new Invocation(referenceConfig, operationMeta, invocationRuntimeType, new HashMap<>());

    InstanceCacheManager instanceCacheManager = Mockito.mock(InstanceCacheManager.class);
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
    scbEngine.setTransportManager(transportManager);

    LocalRegistryStore.INSTANCE.initSelfWithMocked(null, myself);
    mockUpInstanceCacheManager(instanceCacheManager);
    when(instanceCacheManager.getOrCreateVersionedCache("testApp", "testMicroserviceName", "0.0.0+"))
        .thenReturn(parent);
    when(transportManager.findTransport("rest")).thenReturn(transport);

    LoadbalanceHandler handler = null;
    LoadBalancer loadBalancer = null;
    ServiceCombServer server = null;

    handler = new LoadbalanceHandler();
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertNull(server);

    data.put("noneMatchInstance", noneMatchInstance);
    parent.cacheVersion(1);
    handler = new LoadbalanceHandler();
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9092", server.getEndpoint().getEndpoint());

    data.put("regionMatchInstance", regionMatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9091", server.getEndpoint().getEndpoint());

    data.put("allmatchInstance", allmatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9090", server.getEndpoint().getEndpoint());

    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server);

    //if errorThresholdPercentage is 0,that means errorThresholdPercentage is not active.
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.errorThresholdPercentage", "0");
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9090", server.getEndpoint().getEndpoint());

    //if errorThresholdPercentage greater than 0, it will activate.
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.errorThresholdPercentage", "20");
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.minIsolationTime", "30");
    ServiceCombServer server2 = server;
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9091", server.getEndpoint().getEndpoint());
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server2);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9091", server.getEndpoint().getEndpoint());
    mockDelayMillis(31);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9090", server.getEndpoint().getEndpoint());
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server2);
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server2);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9091", server.getEndpoint().getEndpoint());
  }

  @Test
  public void testZoneAwareAndIsolationFilterUsingMockedInvocationWorks() throws Exception {
    Invocation invocation = new NonSwaggerInvocation("testApp", "testMicroserviceName", "0.0.0+", (inv, aysnc) -> aysnc.success("OK"));

    InstanceCacheManager instanceCacheManager = Mockito.mock(InstanceCacheManager.class);
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
    allMatchEndpoint.add("rest://localhost:7090");
    allmatchInstance.setEndpoints(allMatchEndpoint);
    allmatchInstance.setDataCenterInfo(info);
    allmatchInstance.setInstanceId("allmatchInstance");

    MicroserviceInstance regionMatchInstance = new MicroserviceInstance();
    info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region");
    info.setAvailableZone("test-zone2");
    List<String> regionMatchEndpoint = new ArrayList<>();
    regionMatchEndpoint.add("rest://localhost:7091");
    regionMatchInstance.setEndpoints(regionMatchEndpoint);
    regionMatchInstance.setDataCenterInfo(info);
    regionMatchInstance.setInstanceId("regionMatchInstance");

    MicroserviceInstance noneMatchInstance = new MicroserviceInstance();
    info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region2");
    info.setAvailableZone("test-zone2");
    List<String> noMatchEndpoint = new ArrayList<>();
    noMatchEndpoint.add("rest://localhost:7092");
    noneMatchInstance.setEndpoints(noMatchEndpoint);
    noneMatchInstance.setDataCenterInfo(info);
    noneMatchInstance.setInstanceId("noneMatchInstance");

    Map<String, MicroserviceInstance> data = new HashMap<>();
    DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent").data(data);
    scbEngine.setTransportManager(transportManager);

    LocalRegistryStore.INSTANCE.initSelfWithMocked(null, myself);
    mockUpInstanceCacheManager(instanceCacheManager);
    when(instanceCacheManager.getOrCreateVersionedCache("testApp", "testMicroserviceName", "0.0.0+"))
        .thenReturn(parent);
    when(transportManager.findTransport("rest")).thenReturn(transport);

    LoadbalanceHandler handler = null;
    LoadBalancer loadBalancer = null;
    ServiceCombServer server = null;

    DiscoveryTree discoveryTree = new DiscoveryTree();
    discoveryTree.addFilter(new ServerDiscoveryFilter());
    discoveryTree.sort();
    handler = new LoadbalanceHandler(discoveryTree);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertNull(server);

    data.put("noneMatchInstance", noneMatchInstance);
    parent.cacheVersion(1);
    handler = new LoadbalanceHandler();
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7092", server.getEndpoint().getEndpoint());
    handler.handle(invocation, (response) -> Assertions.assertEquals("OK", response.getResult()));

    data.put("regionMatchInstance", regionMatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7091", server.getEndpoint().getEndpoint());
    handler.handle(invocation, (response) -> Assertions.assertEquals("OK", response.getResult()));

    data.put("allmatchInstance", allmatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7090", server.getEndpoint().getEndpoint());

    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server);

    //if errorThresholdPercentage is 0,that means errorThresholdPercentage is not active.
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.errorThresholdPercentage", "0");
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7090", server.getEndpoint().getEndpoint());

    //if errorThresholdPercentage greater than 0, it will activate.
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.errorThresholdPercentage", "20");
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.minIsolationTime", "30");
    ServiceCombServer server2 = server;
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7091", server.getEndpoint().getEndpoint());
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server2);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7091", server.getEndpoint().getEndpoint());
    mockDelayMillis(31);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7090", server.getEndpoint().getEndpoint());
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server2);
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server2);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7091", server.getEndpoint().getEndpoint());
    handler.handle(invocation, (response) -> Assertions.assertEquals("OK", response.getResult()));
  }

  @Test
  public void testStatusFilterUsingMockedInvocationWorks() throws Exception {
    ArchaiusUtils.setProperty("servicecomb.loadbalance.filter.status.enabled", "false");

    Invocation invocation = new NonSwaggerInvocation("testApp", "testMicroserviceName", "0.0.0+", (inv, aysnc) -> aysnc.success("OK"));

    InstanceCacheManager instanceCacheManager = Mockito.mock(InstanceCacheManager.class);
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
    allMatchEndpoint.add("rest://localhost:7090");
    allmatchInstance.setEndpoints(allMatchEndpoint);
    allmatchInstance.setDataCenterInfo(info);
    allmatchInstance.setInstanceId("allmatchInstance");
    allmatchInstance.setStatus(MicroserviceInstanceStatus.TESTING);

    MicroserviceInstance regionMatchInstance = new MicroserviceInstance();
    info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region");
    info.setAvailableZone("test-zone2");
    List<String> regionMatchEndpoint = new ArrayList<>();
    regionMatchEndpoint.add("rest://localhost:7091");
    regionMatchInstance.setEndpoints(regionMatchEndpoint);
    regionMatchInstance.setDataCenterInfo(info);
    regionMatchInstance.setInstanceId("regionMatchInstance");

    MicroserviceInstance noneMatchInstance = new MicroserviceInstance();
    info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region2");
    info.setAvailableZone("test-zone2");
    List<String> noMatchEndpoint = new ArrayList<>();
    noMatchEndpoint.add("rest://localhost:7092");
    noneMatchInstance.setEndpoints(noMatchEndpoint);
    noneMatchInstance.setDataCenterInfo(info);
    noneMatchInstance.setInstanceId("noneMatchInstance");

    Map<String, MicroserviceInstance> data = new HashMap<>();
    DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent").data(data);
    scbEngine.setTransportManager(transportManager);

    LocalRegistryStore.INSTANCE.initSelfWithMocked(null, myself);
    mockUpInstanceCacheManager(instanceCacheManager);
    when(instanceCacheManager.getOrCreateVersionedCache("testApp", "testMicroserviceName", "0.0.0+"))
        .thenReturn(parent);
    when(transportManager.findTransport("rest")).thenReturn(transport);

    LoadbalanceHandler handler = null;
    LoadBalancer loadBalancer = null;
    ServiceCombServer server = null;

    DiscoveryTree discoveryTree = new DiscoveryTree();
    discoveryTree.addFilter(new ServerDiscoveryFilter());
    discoveryTree.sort();
    handler = new LoadbalanceHandler(discoveryTree);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertNull(server);

    data.put("noneMatchInstance", noneMatchInstance);
    parent.cacheVersion(1);
    handler = new LoadbalanceHandler();
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7092", server.getEndpoint().getEndpoint());
    handler.handle(invocation, (response) -> Assertions.assertEquals("OK", response.getResult()));

    data.put("regionMatchInstance", regionMatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7091", server.getEndpoint().getEndpoint());
    handler.handle(invocation, (response) -> Assertions.assertEquals("OK", response.getResult()));

    data.put("allmatchInstance", allmatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7090", server.getEndpoint().getEndpoint());

    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server);
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server);

    //if errorThresholdPercentage is 0,that means errorThresholdPercentage is not active.
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.errorThresholdPercentage", "0");
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7090", server.getEndpoint().getEndpoint());

    //if errorThresholdPercentage greater than 0, it will activate.
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.errorThresholdPercentage", "20");
    ArchaiusUtils.setProperty("servicecomb.loadbalance.isolation.minIsolationTime", "10");
    ServiceCombServer server2 = server;
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7091", server.getEndpoint().getEndpoint());
    ServiceCombLoadBalancerStats.INSTANCE.markSuccess(server2);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7091", server.getEndpoint().getEndpoint());
    mockDelayMillis(20);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7090", server.getEndpoint().getEndpoint());
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server2);
    ServiceCombLoadBalancerStats.INSTANCE.markFailure(server2);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7091", server.getEndpoint().getEndpoint());
    handler.handle(invocation, (response) -> Assertions.assertEquals("OK", response.getResult()));
  }

  @Test
  public void testConfigEndpoint() {
    ReferenceConfig referenceConfig = Mockito.mock(ReferenceConfig.class);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    InvocationRuntimeType invocationRuntimeType = Mockito.mock(InvocationRuntimeType.class);
    SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);
    when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    MicroserviceMeta microserviceMeta = Mockito.mock(MicroserviceMeta.class);
    when(schemaMeta.getMicroserviceMeta()).thenReturn(microserviceMeta);
    when(schemaMeta.getMicroserviceName()).thenReturn("testMicroserviceName");
    when(microserviceMeta.getAppId()).thenReturn("testApp");
    when(referenceConfig.getVersionRule()).thenReturn("0.0.0+");
    when(referenceConfig.getTransport()).thenReturn("rest");
    Invocation invocation = new Invocation(referenceConfig, operationMeta, invocationRuntimeType, new HashMap<>());
    AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);

    InstanceCacheManager instanceCacheManager = Mockito.mock(InstanceCacheManager.class);
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
    scbEngine.setTransportManager(transportManager);
    SCBEngine.getInstance().setTransportManager(transportManager);

    LocalRegistryStore.INSTANCE.initSelfWithMocked(null, myself);
    mockUpInstanceCacheManager(instanceCacheManager);
    when(instanceCacheManager.getOrCreateVersionedCache("testApp", "testMicroserviceName", "0.0.0+"))
        .thenReturn(parent);
    when(transportManager.findTransport("rest")).thenReturn(transport);

    data.put("findInstance", findInstance);
    parent.cacheVersion(1);
    LoadbalanceHandler handler = new LoadbalanceHandler();
    try {
      handler.handle(invocation, asyncResp);
    } catch (Exception e) {

    }
    Assertions.assertEquals("rest://localhost:9092", invocation.getEndpoint().getEndpoint());

    // reset
    invocation.setEndpoint(null);

    //success
    invocation.addLocalContext("scb-endpoint", "rest://127.0.0.1:8080?sslEnabled=true&protocol=http2");
    try {
      handler.handle(invocation, asyncResp);
    } catch (Exception e) {

    }
    Assertions.assertEquals("rest://127.0.0.1:8080?sslEnabled=true&protocol=http2", invocation.getEndpoint().getEndpoint());

    // reset
    invocation.setEndpoint(null);

    //endpoint format is not correct
    invocation.addLocalContext("scb-endpoint", "127.0.0.1:8080");
    try {
      handler.handle(invocation, asyncResp);
      Assertions.assertEquals("endpoint's format is not correct, throw exception", " but not throw exception");
    } catch (Exception e) {
      Assertions.assertTrue(e.getMessage()
          .contains("Illegal character in scheme name"));
    }

    // reset
    invocation.setEndpoint(null);

    //transport is not find
    invocation.addLocalContext("scb-endpoint", "my://127.0.0.1:8080?sslEnabled=true&protocol=http2");
    try {
      handler.handle(invocation, asyncResp);
      Assertions.assertEquals("endpoint's transport not found, throw exception", "but not throw exception");
    } catch (Exception e) {
      Assertions.assertTrue(e.getMessage().contains("the endpoint's transport is not found."));
    }
  }

  @Test
  public void trying_chance_should_be_released() {
    List<ServiceCombServer> servers = new ArrayList<>();
    ServiceCombServer serviceCombServer = createMockedServer("instanceId", "rest://127.0.0.1:8080");
    servers.add(serviceCombServer);

    DiscoveryTree discoveryTree = createMockedDiscoveryTree(servers);
    LoadbalanceHandler handler = new LoadbalanceHandler(discoveryTree);

    // mock the process of the isolated server selected and changed to TRYING status
    ServiceCombServerStats serviceCombServerStats =
        mockServiceCombServerStats(serviceCombServer, 5, true);

    Invocation invocation = new NonSwaggerInvocation("testApp", "testMicroserviceName", "0.0.0+",
        (inv, aysnc) -> {
          Assertions.assertEquals("rest://127.0.0.1:8080", inv.getEndpoint().getEndpoint());
          Assertions.assertTrue(serviceCombServerStats.isIsolated());
          Assertions.assertEquals(5, serviceCombServerStats.getContinuousFailureCount());
          Assertions.assertFalse(ServiceCombServerStats.isolatedServerCanTry());
          aysnc.success("OK");
        });

    Assertions.assertTrue(ServiceCombServerStats.applyForTryingChance(invocation));
    ArchaiusUtils.setProperty("servicecomb.loadbalance.filter.isolation.enabled", "false");
    try {
      handler.handle(invocation, (response) -> Assertions.assertEquals("OK", response.getResult()));
    } catch (Exception e) {
      Assertions.fail("unexpected exception " + e.getMessage());
    }
    ArchaiusUtils.setProperty("servicecomb.loadbalance.filter.isolation.enabled", "true");
    Assertions.assertEquals("rest://127.0.0.1:8080", invocation.getEndpoint().getEndpoint());
    Assertions.assertTrue(serviceCombServerStats.isIsolated());
    Assertions.assertEquals(0, serviceCombServerStats.getContinuousFailureCount());
    Assertions.assertTrue(ServiceCombServerStats.isolatedServerCanTry());
  }

  /**
   * Mock the statistics of the specified {@code serviceCombServer}, set the failureCount and status.
   * @return the ServiceCombServerStats object corresponding to the param {@code serviceCombServer}
   */
  private ServiceCombServerStats mockServiceCombServerStats(ServiceCombServer serviceCombServer, int failureCount,
      boolean isIsolatedStatus) {
    ServiceCombServerStats serviceCombServerStats =
        ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(serviceCombServer);
    for (int i = 0; i < failureCount; ++i) {
      serviceCombServerStats.markFailure();
    }
    serviceCombServerStats.markIsolated(isIsolatedStatus);
    return serviceCombServerStats;
  }

  /**
   * Create a mocked ServiceCombServer with specified microserviceInstanceId and endpoint.
   */
  private ServiceCombServer createMockedServer(String microserviceInstanceId, String endpoint) {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setInstanceId(microserviceInstanceId);
    return new ServiceCombServer(null,
        new Endpoint(Mockito.mock(Transport.class), endpoint),
        microserviceInstance);
  }

  /**
   * Create a mocked DiscoveryTree that always returns {@code servers} as the versionedCache result.
   */
  private DiscoveryTree createMockedDiscoveryTree(List<ServiceCombServer> servers) {
    DiscoveryTree discoveryTree = Mockito.mock(DiscoveryTree.class);
    DiscoveryTreeNode versionedCache = new DiscoveryTreeNode()
        .name("testVersionedCacheName")
        .data(servers);
    Mockito.when(discoveryTree.discovery(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(versionedCache);
    return discoveryTree;
  }

  private void mockDelayMillis(long delay) {
    mockTimeMillis.value += delay;
  }

  private void mockUpInstanceCacheManager(InstanceCacheManager instanceCacheManager) {
    new MockUp<DiscoveryManager>() {
      @Mock
      public InstanceCacheManager getInstanceCacheManager() {
        return instanceCacheManager;
      }
    };
  }
}
