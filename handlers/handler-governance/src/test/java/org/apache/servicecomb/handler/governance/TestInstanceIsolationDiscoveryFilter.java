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
package org.apache.servicecomb.handler.governance;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.registry.discovery.EndpointDiscoveryFilter;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.cache.InstanceCacheManager;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryTree;
import org.apache.servicecomb.registry.discovery.DiscoveryTreeNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestInstanceIsolationDiscoveryFilter {
  @Test
  @SuppressWarnings("unchecked")
  public void test_instance_isolation_correct() throws Exception {
    DiscoveryTree discoveryTree = new DiscoveryTree();
    DiscoveryContext discoveryContext = new DiscoveryContext();
    discoveryTree.addFilter(new InstanceIsolationDiscoveryFilter());
    discoveryTree.addFilter(new EndpointDiscoveryFilter());

    Invocation invocation = Mockito.mock(Invocation.class);
    discoveryContext.setInputParameters(invocation);
    Mockito.when(invocation.getConfigTransportName()).thenReturn("rest");

    TransportManager transportManager = Mockito.mock(TransportManager.class);
    SCBEngine.getInstance().setTransportManager(transportManager);
    Transport transport = Mockito.mock(Transport.class);
    Mockito.when(transportManager.findTransport("rest")).thenReturn(transport);
    Mockito.when(transport.parseAddress("rest://localhost:9090")).thenReturn("9090");
    Mockito.when(transport.parseAddress("rest://localhost:9091")).thenReturn("9091");

    Map<String, MicroserviceInstance> service1 = new HashMap<>();
    MicroserviceInstance instance1 = Mockito.mock(MicroserviceInstance.class);
    MicroserviceInstance instance2 = Mockito.mock(MicroserviceInstance.class);
    Mockito.when(instance1.getInstanceId()).thenReturn("instance1");
    Mockito.when(instance1.getStatus()).thenReturn(MicroserviceInstanceStatus.UP);
    Mockito.when(instance1.getEndpoints()).thenReturn(Collections.singletonList("rest://localhost:9090"));
    Mockito.when(instance2.getInstanceId()).thenReturn("instance2");
    Mockito.when(instance2.getStatus()).thenReturn(MicroserviceInstanceStatus.UP);
    Mockito.when(instance2.getEndpoints()).thenReturn(Collections.singletonList("rest://localhost:9092"));
    service1.put(instance1.getInstanceId(), instance1);
    service1.put(instance2.getInstanceId(), instance2);

    InstanceCacheManager instanceCacheManager = Mockito.mock(InstanceCacheManager.class);
    DiscoveryManager.INSTANCE = Mockito.spy(DiscoveryManager.INSTANCE);
    Mockito.when(DiscoveryManager.INSTANCE.getInstanceCacheManager()).thenReturn(instanceCacheManager);

    VersionedCache expects0 = new VersionedCache().autoCacheVersion().name("0+").data(service1);
    Mockito.when(instanceCacheManager.getOrCreateVersionedCache("app", "service1",
        "0+")).thenReturn(expects0);

    DiscoveryTreeNode result = discoveryTree.discovery(discoveryContext, "app", "service1", "0+");
    Assertions.assertEquals(2, ((List<Object>) result.data()).size());

    // isolate
    EventManager.post(new InstanceIsolatedEvent("instance1", Duration.ofMillis(8)));
    result = discoveryTree.discovery(discoveryContext, "app", "service1", "0+");
    Assertions.assertEquals(1, ((List<Object>) result.data()).size());
    //recover
    Thread.sleep(10);
    result = discoveryTree.discovery(discoveryContext, "app", "service1", "0+");
    Assertions.assertEquals(2, ((List<Object>) result.data()).size());
  }
}
