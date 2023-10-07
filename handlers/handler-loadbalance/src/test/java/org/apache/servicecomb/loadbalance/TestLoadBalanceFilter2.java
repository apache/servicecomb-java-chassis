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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.servicecomb.config.DataCenterProperties;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.NonSwaggerInvocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.definition.InvocationRuntimeType;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.loadbalance.filter.ServerDiscoveryFilter;
import org.apache.servicecomb.loadbalance.filter.ZoneAwareDiscoveryFilter;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.discovery.DiscoveryTree;
import org.apache.servicecomb.registry.discovery.DiscoveryTreeNode;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class TestLoadBalanceFilter2 {
  Environment environment = Mockito.mock(Environment.class);

  @BeforeEach
  public void setUp() {
    Mockito.when(environment.getProperty("servicecomb.loadbalance.userDefinedEndpoint.enabled",
        boolean.class, false)).thenReturn(false);
  }

  @Test
  public void testZoneAwareFilterWorks() {
    ReferenceConfig referenceConfig = Mockito.mock(ReferenceConfig.class);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    InvocationRuntimeType invocationRuntimeType = Mockito.mock(InvocationRuntimeType.class);
    SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);
    when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    MicroserviceMeta microserviceMeta = Mockito.mock(MicroserviceMeta.class);
    when(schemaMeta.getMicroserviceMeta()).thenReturn(microserviceMeta);
    when(schemaMeta.getMicroserviceName()).thenReturn("testMicroserviceName");
    when(microserviceMeta.getAppId()).thenReturn("testApp");
    when(referenceConfig.getTransport()).thenReturn("rest");
    Invocation invocation = new Invocation(referenceConfig, operationMeta, invocationRuntimeType, new HashMap<>());
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    Transport transport = Mockito.mock(Transport.class);
    SCBEngine scbEngine = Mockito.mock(SCBEngine.class);
    Mockito.when(scbEngine.getEnvironment()).thenReturn(environment);
    Mockito.when(scbEngine.getTransportManager()).thenReturn(transportManager);

    // set up data
    DataCenterProperties myself = new DataCenterProperties();
    myself.setName("test");
    myself.setRegion("test-Region");
    myself.setAvailableZone("test-zone");

    DiscoveryInstance discoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance allmatchInstance = new StatefulDiscoveryInstance(discoveryInstance);
    DataCenterInfo info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region");
    info.setAvailableZone("test-zone");
    List<String> allMatchEndpoint = new ArrayList<>();
    allMatchEndpoint.add("rest://localhost:9090");
    Mockito.when(discoveryInstance.getEndpoints()).thenReturn(allMatchEndpoint);
    Mockito.when(discoveryInstance.getDataCenterInfo()).thenReturn(info);
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn("allmatchInstance");

    DiscoveryInstance regionMatchDiscoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance regionMatchInstance = new StatefulDiscoveryInstance(regionMatchDiscoveryInstance);
    DataCenterInfo regionMatchInfo = new DataCenterInfo();
    regionMatchInfo.setName("test");
    regionMatchInfo.setRegion("test-Region");
    regionMatchInfo.setAvailableZone("test-zone2");
    List<String> regionMatchEndpoint = new ArrayList<>();
    regionMatchEndpoint.add("rest://localhost:9091");
    Mockito.when(regionMatchDiscoveryInstance.getEndpoints()).thenReturn(regionMatchEndpoint);
    Mockito.when(regionMatchDiscoveryInstance.getDataCenterInfo()).thenReturn(regionMatchInfo);
    Mockito.when(regionMatchDiscoveryInstance.getInstanceId()).thenReturn("regionMatchInstance");

    DiscoveryInstance noneMatchDiscoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance noneMatchInstance = new StatefulDiscoveryInstance(noneMatchDiscoveryInstance);
    DataCenterInfo noneMatchInfo = new DataCenterInfo();
    noneMatchInfo.setName("test");
    noneMatchInfo.setRegion("test-Region2");
    noneMatchInfo.setAvailableZone("test-zone2");
    List<String> noMatchEndpoint = new ArrayList<>();
    noMatchEndpoint.add("rest://localhost:9092");
    Mockito.when(noneMatchDiscoveryInstance.getEndpoints()).thenReturn(noMatchEndpoint);
    Mockito.when(noneMatchDiscoveryInstance.getDataCenterInfo()).thenReturn(noneMatchInfo);
    Mockito.when(noneMatchDiscoveryInstance.getInstanceId()).thenReturn("noneMatchInstance");

    List<StatefulDiscoveryInstance> data = new ArrayList<>();
    DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent").data(data);
    when(transportManager.findTransport("rest")).thenReturn(transport);

    LoadBalanceFilter handler;
    LoadBalancer loadBalancer;
    ServiceCombServer server;

    DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
    Mockito.when(discoveryManager.getOrCreateVersionedCache("testApp", "testMicroserviceName"))
        .thenReturn(parent);
    DiscoveryTree discoveryTree = new DiscoveryTree(discoveryManager);
    ZoneAwareDiscoveryFilter zoneAwareDiscoveryFilter = new ZoneAwareDiscoveryFilter();
    zoneAwareDiscoveryFilter.setEnvironment(environment);
    zoneAwareDiscoveryFilter.setDataCenterProperties(myself);
    ServerDiscoveryFilter serverDiscoveryFilter = new ServerDiscoveryFilter();
    serverDiscoveryFilter.setScbEngine(scbEngine);
    Mockito.when(environment.getProperty("servicecomb.loadbalance.filter.zoneaware.enabled",
        Boolean.class, true)).thenReturn(true);
    discoveryTree.setDiscoveryFilters(Arrays.asList(zoneAwareDiscoveryFilter,
        serverDiscoveryFilter));
    handler = new LoadBalanceFilter(new ExtensionsManager(new ArrayList<>()),
        discoveryTree, scbEngine);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertNull(server);

    data.add(noneMatchInstance);
    parent.cacheVersion(1);
    handler = new LoadBalanceFilter(new ExtensionsManager(new ArrayList<>()),
        discoveryTree, scbEngine);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9092", server.getEndpoint().getEndpoint());

    data.add(regionMatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9091", server.getEndpoint().getEndpoint());

    data.add(allmatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9090", server.getEndpoint().getEndpoint());
  }


  @Test
  public void testIsolationEventWithEndpoint() {
    ReferenceConfig referenceConfig = Mockito.mock(ReferenceConfig.class);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    InvocationRuntimeType invocationRuntimeType = Mockito.mock(InvocationRuntimeType.class);
    SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);
    when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    MicroserviceMeta microserviceMeta = Mockito.mock(MicroserviceMeta.class);
    when(schemaMeta.getMicroserviceMeta()).thenReturn(microserviceMeta);
    when(schemaMeta.getMicroserviceName()).thenReturn("testMicroserviceName");
    when(microserviceMeta.getAppId()).thenReturn("testApp");
    when(referenceConfig.getTransport()).thenReturn("rest");
    Invocation invocation = new Invocation(referenceConfig, operationMeta, invocationRuntimeType, new HashMap<>());
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    Transport transport = Mockito.mock(Transport.class);
    SCBEngine scbEngine = Mockito.mock(SCBEngine.class);
    Mockito.when(scbEngine.getEnvironment()).thenReturn(environment);
    Mockito.when(scbEngine.getTransportManager()).thenReturn(transportManager);

    // set up data
    DataCenterProperties myself = new DataCenterProperties();
    myself.setName("test");
    myself.setRegion("test");
    myself.setAvailableZone("test");

    DiscoveryInstance discoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance instance = new StatefulDiscoveryInstance(discoveryInstance);
    DataCenterInfo info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test");
    info.setAvailableZone("test");
    List<String> allMatchEndpoint = new ArrayList<>();
    allMatchEndpoint.add("rest://localhost:9090");
    Mockito.when(discoveryInstance.getEndpoints()).thenReturn(allMatchEndpoint);
    Mockito.when(discoveryInstance.getDataCenterInfo()).thenReturn(info);
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn("instance");

    List<StatefulDiscoveryInstance> data = new ArrayList<>();
    DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent").data(data);

    when(transportManager.findTransport("rest")).thenReturn(transport);

    LoadBalanceFilter handler;
    LoadBalancer loadBalancer;
    ServiceCombServer server;

    DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
    Mockito.when(discoveryManager.getOrCreateVersionedCache("testApp", "testMicroserviceName"))
        .thenReturn(parent);
    DiscoveryTree discoveryTree = new DiscoveryTree(discoveryManager);
    ZoneAwareDiscoveryFilter zoneAwareDiscoveryFilter = new ZoneAwareDiscoveryFilter();
    zoneAwareDiscoveryFilter.setEnvironment(environment);
    zoneAwareDiscoveryFilter.setDataCenterProperties(myself);
    Mockito.when(environment.getProperty("servicecomb.loadbalance.filter.zoneaware.enabled",
        Boolean.class, true)).thenReturn(true);
    ServerDiscoveryFilter serverDiscoveryFilter = new ServerDiscoveryFilter();
    serverDiscoveryFilter.setScbEngine(scbEngine);
    discoveryTree.setDiscoveryFilters(Arrays.asList(zoneAwareDiscoveryFilter,
        serverDiscoveryFilter));
    handler = new LoadBalanceFilter(new ExtensionsManager(new ArrayList<>()),
        discoveryTree, scbEngine);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertNull(server);

    data.add(instance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9090", server.getEndpoint().getEndpoint());
  }

  @Test
  public void testZoneAwareFilterWorksEmptyInstanceProtectionEnabled() {
    ReferenceConfig referenceConfig = Mockito.mock(ReferenceConfig.class);
    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    InvocationRuntimeType invocationRuntimeType = Mockito.mock(InvocationRuntimeType.class);
    SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);
    when(operationMeta.getSchemaMeta()).thenReturn(schemaMeta);
    MicroserviceMeta microserviceMeta = Mockito.mock(MicroserviceMeta.class);
    when(schemaMeta.getMicroserviceMeta()).thenReturn(microserviceMeta);
    when(schemaMeta.getMicroserviceName()).thenReturn("testMicroserviceName");
    when(microserviceMeta.getAppId()).thenReturn("testApp");
    when(referenceConfig.getTransport()).thenReturn("rest");
    Invocation invocation = new Invocation(referenceConfig, operationMeta, invocationRuntimeType, new HashMap<>());
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    Transport transport = Mockito.mock(Transport.class);
    SCBEngine scbEngine = Mockito.mock(SCBEngine.class);
    Mockito.when(scbEngine.getTransportManager()).thenReturn(transportManager);
    Mockito.when(scbEngine.getEnvironment()).thenReturn(environment);

    // set up data
    DataCenterProperties myself = new DataCenterProperties();
    myself.setName("test");
    myself.setRegion("test-Region");
    myself.setAvailableZone("test-zone");

    DiscoveryInstance discoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance allmatchInstance = new StatefulDiscoveryInstance(discoveryInstance);
    DataCenterInfo info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region");
    info.setAvailableZone("test-zone");
    List<String> allMatchEndpoint = new ArrayList<>();
    allMatchEndpoint.add("rest://localhost:9090");
    Mockito.when(discoveryInstance.getEndpoints()).thenReturn(allMatchEndpoint);
    Mockito.when(discoveryInstance.getDataCenterInfo()).thenReturn(info);
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn("allmatchInstance");

    DiscoveryInstance regionMatchDiscoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance regionMatchInstance = new StatefulDiscoveryInstance(regionMatchDiscoveryInstance);
    DataCenterInfo regionMatchInfo = new DataCenterInfo();
    regionMatchInfo.setName("test");
    regionMatchInfo.setRegion("test-Region");
    regionMatchInfo.setAvailableZone("test-zone2");
    List<String> regionMatchEndpoint = new ArrayList<>();
    regionMatchEndpoint.add("rest://localhost:9091");
    Mockito.when(regionMatchDiscoveryInstance.getEndpoints()).thenReturn(regionMatchEndpoint);
    Mockito.when(regionMatchDiscoveryInstance.getDataCenterInfo()).thenReturn(regionMatchInfo);
    Mockito.when(regionMatchDiscoveryInstance.getInstanceId()).thenReturn("regionMatchInstance");

    DiscoveryInstance noneMatchDiscoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance noneMatchInstance = new StatefulDiscoveryInstance(noneMatchDiscoveryInstance);
    DataCenterInfo noneMatchInfo = new DataCenterInfo();
    noneMatchInfo.setName("test");
    noneMatchInfo.setRegion("test-Region2");
    noneMatchInfo.setAvailableZone("test-zone2");
    List<String> noMatchEndpoint = new ArrayList<>();
    noMatchEndpoint.add("rest://localhost:9092");
    Mockito.when(noneMatchDiscoveryInstance.getEndpoints()).thenReturn(noMatchEndpoint);
    Mockito.when(noneMatchDiscoveryInstance.getDataCenterInfo()).thenReturn(noneMatchInfo);
    Mockito.when(noneMatchDiscoveryInstance.getInstanceId()).thenReturn("noneMatchInstance");

    List<StatefulDiscoveryInstance> data = new ArrayList<>();
    DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent").data(data);

    when(transportManager.findTransport("rest")).thenReturn(transport);

    LoadBalanceFilter handler;
    LoadBalancer loadBalancer;
    ServiceCombServer server;

    DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
    Mockito.when(discoveryManager.getOrCreateVersionedCache("testApp", "testMicroserviceName"))
        .thenReturn(parent);
    DiscoveryTree discoveryTree = new DiscoveryTree(discoveryManager);
    ZoneAwareDiscoveryFilter zoneAwareDiscoveryFilter = new ZoneAwareDiscoveryFilter();
    zoneAwareDiscoveryFilter.setEnvironment(environment);
    zoneAwareDiscoveryFilter.setDataCenterProperties(myself);
    Mockito.when(environment.getProperty("servicecomb.loadbalance.filter.zoneaware.enabled",
        Boolean.class, true)).thenReturn(true);
    ServerDiscoveryFilter serverDiscoveryFilter = new ServerDiscoveryFilter();
    serverDiscoveryFilter.setScbEngine(scbEngine);
    discoveryTree.setDiscoveryFilters(Arrays.asList(zoneAwareDiscoveryFilter,
        serverDiscoveryFilter));
    handler = new LoadBalanceFilter(new ExtensionsManager(new ArrayList<>()), discoveryTree, scbEngine);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertNull(server);

    data.add(noneMatchInstance);
    parent.cacheVersion(1);
    handler = new LoadBalanceFilter(new ExtensionsManager(new ArrayList<>()), discoveryTree, scbEngine);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9092", server.getEndpoint().getEndpoint());

    data.add(regionMatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9091", server.getEndpoint().getEndpoint());

    data.add(allmatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:9090", server.getEndpoint().getEndpoint());
  }

  @Test
  public void testZoneAwareFilterUsingMockedInvocationWorks() {
    Invocation invocation = new NonSwaggerInvocation("testApp", "testMicroserviceName");
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    Transport transport = Mockito.mock(Transport.class);
    SCBEngine scbEngine = Mockito.mock(SCBEngine.class);
    Mockito.when(scbEngine.getTransportManager()).thenReturn(transportManager);
    Mockito.when(scbEngine.getEnvironment()).thenReturn(environment);
    Mockito.when(environment.getProperty("servicecomb.loadbalance.filter.operation.enabled",
        boolean.class, true)).thenReturn(false);

    // set up data
    DataCenterProperties myself = new DataCenterProperties();
    myself.setName("test");
    myself.setRegion("test-Region");
    myself.setAvailableZone("test-zone");

    DiscoveryInstance discoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance allmatchInstance = new StatefulDiscoveryInstance(discoveryInstance);
    DataCenterInfo info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region");
    info.setAvailableZone("test-zone");
    List<String> allMatchEndpoint = new ArrayList<>();
    allMatchEndpoint.add("rest://localhost:7090");
    Mockito.when(discoveryInstance.getEndpoints()).thenReturn(allMatchEndpoint);
    Mockito.when(discoveryInstance.getDataCenterInfo()).thenReturn(info);
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn("allmatchInstance");

    DiscoveryInstance regionMatchDiscoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance regionMatchInstance = new StatefulDiscoveryInstance(regionMatchDiscoveryInstance);
    DataCenterInfo regionMatchInfo = new DataCenterInfo();
    regionMatchInfo.setName("test");
    regionMatchInfo.setRegion("test-Region");
    regionMatchInfo.setAvailableZone("test-zone2");
    List<String> regionMatchEndpoint = new ArrayList<>();
    regionMatchEndpoint.add("rest://localhost:7091");
    Mockito.when(regionMatchDiscoveryInstance.getEndpoints()).thenReturn(regionMatchEndpoint);
    Mockito.when(regionMatchDiscoveryInstance.getDataCenterInfo()).thenReturn(regionMatchInfo);
    Mockito.when(regionMatchDiscoveryInstance.getInstanceId()).thenReturn("regionMatchInstance");

    DiscoveryInstance noneMatchDiscoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance noneMatchInstance = new StatefulDiscoveryInstance(noneMatchDiscoveryInstance);
    DataCenterInfo noneMatchInfo = new DataCenterInfo();
    noneMatchInfo.setName("test");
    noneMatchInfo.setRegion("test-Region2");
    noneMatchInfo.setAvailableZone("test-zone2");
    List<String> noMatchEndpoint = new ArrayList<>();
    noMatchEndpoint.add("rest://localhost:7092");
    Mockito.when(noneMatchDiscoveryInstance.getEndpoints()).thenReturn(noMatchEndpoint);
    Mockito.when(noneMatchDiscoveryInstance.getDataCenterInfo()).thenReturn(noneMatchInfo);
    Mockito.when(noneMatchDiscoveryInstance.getInstanceId()).thenReturn("noneMatchInstance");

    List<StatefulDiscoveryInstance> data = new ArrayList<>();
    DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent").data(data);

    when(transportManager.findTransport("rest")).thenReturn(transport);

    LoadBalanceFilter handler;
    LoadBalancer loadBalancer;
    ServiceCombServer server;

    DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
    Mockito.when(discoveryManager.getOrCreateVersionedCache("testApp", "testMicroserviceName"))
        .thenReturn(parent);
    DiscoveryTree discoveryTree = new DiscoveryTree(discoveryManager);
    ZoneAwareDiscoveryFilter zoneAwareDiscoveryFilter = new ZoneAwareDiscoveryFilter();
    Environment environment = Mockito.mock(Environment.class);
    zoneAwareDiscoveryFilter.setEnvironment(environment);
    zoneAwareDiscoveryFilter.setDataCenterProperties(myself);
    Mockito.when(environment.getProperty("servicecomb.loadbalance.filter.zoneaware.enabled",
        Boolean.class, true)).thenReturn(true);
    ServerDiscoveryFilter serverDiscoveryFilter = new ServerDiscoveryFilter();
    serverDiscoveryFilter.setScbEngine(scbEngine);
    discoveryTree.setDiscoveryFilters(Arrays.asList(zoneAwareDiscoveryFilter,
        serverDiscoveryFilter));
    handler = new LoadBalanceFilter(new ExtensionsManager(new ArrayList<>()), discoveryTree, scbEngine);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertNull(server);

    data.add(noneMatchInstance);
    parent.cacheVersion(1);
    handler = new LoadBalanceFilter(new ExtensionsManager(new ArrayList<>()), discoveryTree, scbEngine);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7092", server.getEndpoint().getEndpoint());

    data.add(regionMatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7091", server.getEndpoint().getEndpoint());

    data.add(allmatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7090", server.getEndpoint().getEndpoint());
  }

  @Test
  public void testStatusFilterUsingMockedInvocationWorks() {
    Invocation invocation = new NonSwaggerInvocation("testApp", "testMicroserviceName");
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    Transport transport = Mockito.mock(Transport.class);
    SCBEngine scbEngine = Mockito.mock(SCBEngine.class);
    Mockito.when(scbEngine.getTransportManager()).thenReturn(transportManager);
    Mockito.when(scbEngine.getEnvironment()).thenReturn(environment);

    // set up data
    DataCenterProperties myself = new DataCenterProperties();
    myself.setName("test");
    myself.setRegion("test-Region");
    myself.setAvailableZone("test-zone");

    DiscoveryInstance discoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance allmatchInstance = new StatefulDiscoveryInstance(discoveryInstance);
    DataCenterInfo info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region");
    info.setAvailableZone("test-zone");
    List<String> allMatchEndpoint = new ArrayList<>();
    allMatchEndpoint.add("rest://localhost:7090");
    Mockito.when(discoveryInstance.getEndpoints()).thenReturn(allMatchEndpoint);
    Mockito.when(discoveryInstance.getDataCenterInfo()).thenReturn(info);
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn("allmatchInstance");
    Mockito.when(discoveryInstance.getStatus()).thenReturn(MicroserviceInstanceStatus.TESTING);

    DiscoveryInstance regionMatchDiscoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance regionMatchInstance = new StatefulDiscoveryInstance(regionMatchDiscoveryInstance);
    DataCenterInfo regionMatchInfo = new DataCenterInfo();
    regionMatchInfo.setName("test");
    regionMatchInfo.setRegion("test-Region");
    regionMatchInfo.setAvailableZone("test-zone2");
    List<String> regionMatchEndpoint = new ArrayList<>();
    regionMatchEndpoint.add("rest://localhost:7091");
    Mockito.when(regionMatchDiscoveryInstance.getEndpoints()).thenReturn(regionMatchEndpoint);
    Mockito.when(regionMatchDiscoveryInstance.getDataCenterInfo()).thenReturn(regionMatchInfo);
    Mockito.when(regionMatchDiscoveryInstance.getInstanceId()).thenReturn("regionMatchInstance");

    DiscoveryInstance noneMatchDiscoveryInstance = Mockito.mock(DiscoveryInstance.class);
    StatefulDiscoveryInstance noneMatchInstance = new StatefulDiscoveryInstance(noneMatchDiscoveryInstance);
    DataCenterInfo noneMatchInfo = new DataCenterInfo();
    noneMatchInfo.setName("test");
    noneMatchInfo.setRegion("test-Region2");
    noneMatchInfo.setAvailableZone("test-zone2");
    List<String> noMatchEndpoint = new ArrayList<>();
    noMatchEndpoint.add("rest://localhost:7092");
    Mockito.when(noneMatchDiscoveryInstance.getEndpoints()).thenReturn(noMatchEndpoint);
    Mockito.when(noneMatchDiscoveryInstance.getDataCenterInfo()).thenReturn(noneMatchInfo);
    Mockito.when(noneMatchDiscoveryInstance.getInstanceId()).thenReturn("noneMatchInstance");

    List<StatefulDiscoveryInstance> data = new ArrayList<>();
    DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent").data(data);

    when(transportManager.findTransport("rest")).thenReturn(transport);

    LoadBalanceFilter handler;
    LoadBalancer loadBalancer;
    ServiceCombServer server;

    DiscoveryManager discoveryManager = Mockito.mock(DiscoveryManager.class);
    Mockito.when(discoveryManager.getOrCreateVersionedCache("testApp", "testMicroserviceName"))
        .thenReturn(parent);
    DiscoveryTree discoveryTree = new DiscoveryTree(discoveryManager);
    ZoneAwareDiscoveryFilter zoneAwareDiscoveryFilter = new ZoneAwareDiscoveryFilter();
    Environment environment = Mockito.mock(Environment.class);
    zoneAwareDiscoveryFilter.setEnvironment(environment);
    zoneAwareDiscoveryFilter.setDataCenterProperties(myself);
    Mockito.when(environment.getProperty("servicecomb.loadbalance.filter.zoneaware.enabled",
        Boolean.class, true)).thenReturn(true);
    ServerDiscoveryFilter serverDiscoveryFilter = new ServerDiscoveryFilter();
    serverDiscoveryFilter.setScbEngine(scbEngine);
    discoveryTree.setDiscoveryFilters(Arrays.asList(zoneAwareDiscoveryFilter,
        serverDiscoveryFilter));
    handler = new LoadBalanceFilter(new ExtensionsManager(new ArrayList<>()), discoveryTree, scbEngine);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertNull(server);

    data.add(noneMatchInstance);
    parent.cacheVersion(1);
    handler = new LoadBalanceFilter(new ExtensionsManager(new ArrayList<>()), discoveryTree, scbEngine);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7092", server.getEndpoint().getEndpoint());

    data.add(regionMatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7091", server.getEndpoint().getEndpoint());

    data.add(allmatchInstance);
    parent.cacheVersion(parent.cacheVersion() + 1);
    loadBalancer = handler.getOrCreateLoadBalancer(invocation);
    server = loadBalancer.chooseServer(invocation);
    Assertions.assertEquals("rest://localhost:7090", server.getEndpoint().getEndpoint());
  }
}
