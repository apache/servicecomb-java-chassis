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
package org.apache.servicecomb.loadbalance.filterext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.DataCenterInfo;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.cache.CacheEndpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestZoneAwareDiscoveryFilter {
  @BeforeEach
  public void setUp() {
    ConfigUtil.createLocalConfig();
  }

  @AfterEach
  public void tearDown() {
    ArchaiusUtils.resetConfig();
    RegistrationManager.renewInstance();
  }

  @Test
  public void test_not_enough_instance() {
    ZoneAwareDiscoveryFilter filter = new ZoneAwareDiscoveryFilter();

    // set up data
    MicroserviceInstance myself = Mockito.mock(MicroserviceInstance.class);
    RegistrationManager registrationManager = Mockito.mock(RegistrationManager.class);
    Mockito.when(registrationManager.getMicroserviceInstance()).thenReturn(myself);
    RegistrationManager.setINSTANCE(registrationManager);
    DataCenterInfo myDcInfo = new DataCenterInfo();
    myDcInfo.setName("test");
    myDcInfo.setRegion("test-Region");
    myDcInfo.setAvailableZone("test-zone");
    Mockito.when(myself.getDataCenterInfo()).thenReturn(myDcInfo);

    MicroserviceInstance discoveryInstance = Mockito.mock(MicroserviceInstance.class);
    List<String> allMatchEndpoint = new ArrayList<>();
    allMatchEndpoint.add("rest://localhost:9090");
    Mockito.when(discoveryInstance.getEndpoints()).thenReturn(allMatchEndpoint);
    CacheEndpoint allmatchCacheEndpoint = Mockito.mock(CacheEndpoint.class);
    Mockito.when(allmatchCacheEndpoint.getEndpoint()).thenReturn("rest://localhost:9090");
    Transport transport = Mockito.mock(Transport.class);
    Mockito.when(allmatchCacheEndpoint.getInstance()).thenReturn(discoveryInstance);
    ServiceCombServer allmatchInstance = new ServiceCombServer("test", transport, allmatchCacheEndpoint);
    DataCenterInfo info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region");
    info.setAvailableZone("test-zone");
    Mockito.when(discoveryInstance.getDataCenterInfo()).thenReturn(info);
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn("allmatchInstance");

    MicroserviceInstance regionMatchDiscoveryInstance = Mockito.mock(MicroserviceInstance.class);
    List<String> regionMatchEndpoint = new ArrayList<>();
    regionMatchEndpoint.add("rest://localhost:9091");
    Mockito.when(regionMatchDiscoveryInstance.getEndpoints()).thenReturn(regionMatchEndpoint);
    CacheEndpoint regionMatchCacheEndpoint = Mockito.mock(CacheEndpoint.class);
    Mockito.when(regionMatchCacheEndpoint.getEndpoint()).thenReturn("rest://localhost:9091");
    Mockito.when(regionMatchCacheEndpoint.getInstance()).thenReturn(regionMatchDiscoveryInstance);
    ServiceCombServer regionMatchInstance = new ServiceCombServer("test", transport, regionMatchCacheEndpoint);
    DataCenterInfo regionMatchInfo = new DataCenterInfo();
    regionMatchInfo.setName("test");
    regionMatchInfo.setRegion("test-Region");
    regionMatchInfo.setAvailableZone("test-zone2");
    Mockito.when(regionMatchDiscoveryInstance.getDataCenterInfo()).thenReturn(regionMatchInfo);
    Mockito.when(regionMatchDiscoveryInstance.getInstanceId()).thenReturn("regionMatchInstance");

    MicroserviceInstance noneMatchDiscoveryInstance = Mockito.mock(MicroserviceInstance.class);
    List<String> noMatchEndpoint = new ArrayList<>();
    noMatchEndpoint.add("rest://localhost:9092");
    Mockito.when(noneMatchDiscoveryInstance.getEndpoints()).thenReturn(noMatchEndpoint);
    CacheEndpoint noneMatchCacheEndpoint = Mockito.mock(CacheEndpoint.class);
    Mockito.when(noneMatchCacheEndpoint.getEndpoint()).thenReturn("rest://localhost:9092");
    Mockito.when(noneMatchCacheEndpoint.getInstance()).thenReturn(noneMatchDiscoveryInstance);
    ServiceCombServer noneMatchInstance = new ServiceCombServer("test", transport, noneMatchCacheEndpoint);
    DataCenterInfo noneMatchInfo = new DataCenterInfo();
    noneMatchInfo.setName("test");
    noneMatchInfo.setRegion("test-Region2");
    noneMatchInfo.setAvailableZone("test-zone2");
    Mockito.when(noneMatchDiscoveryInstance.getDataCenterInfo()).thenReturn(noneMatchInfo);
    Mockito.when(noneMatchDiscoveryInstance.getInstanceId()).thenReturn("noneMatchInstance");

    // run test
    Invocation invocation = Mockito.mock(Invocation.class);
    List<ServiceCombServer> data = Arrays.asList(allmatchInstance, regionMatchInstance, noneMatchInstance);
    List<ServiceCombServer> result = filter.getFilteredListOfServers(data, invocation);

    // check result
    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals("regionMatchInstance", result.get(0).getInstance().getInstanceId());
    Assertions.assertEquals("allmatchInstance", result.get(1).getInstance().getInstanceId());
  }

  @Test
  public void test_enough_instance() {
    ArchaiusUtils.setProperty(ZoneAwareDiscoveryFilter.CONFIG_RATIO, 0);
    ZoneAwareDiscoveryFilter filter = new ZoneAwareDiscoveryFilter();

    // set up data
    MicroserviceInstance myself = Mockito.mock(MicroserviceInstance.class);
    RegistrationManager registrationManager = Mockito.mock(RegistrationManager.class);
    Mockito.when(registrationManager.getMicroserviceInstance()).thenReturn(myself);
    RegistrationManager.setINSTANCE(registrationManager);
    DataCenterInfo myDcInfo = new DataCenterInfo();
    myDcInfo.setName("test");
    myDcInfo.setRegion("test-Region");
    myDcInfo.setAvailableZone("test-zone");
    Mockito.when(myself.getDataCenterInfo()).thenReturn(myDcInfo);

    MicroserviceInstance discoveryInstance = Mockito.mock(MicroserviceInstance.class);
    List<String> allMatchEndpoint = new ArrayList<>();
    allMatchEndpoint.add("rest://localhost:9090");
    Mockito.when(discoveryInstance.getEndpoints()).thenReturn(allMatchEndpoint);
    CacheEndpoint allmatchCacheEndpoint = Mockito.mock(CacheEndpoint.class);
    Mockito.when(allmatchCacheEndpoint.getEndpoint()).thenReturn("rest://localhost:9090");
    Transport transport = Mockito.mock(Transport.class);
    Mockito.when(allmatchCacheEndpoint.getInstance()).thenReturn(discoveryInstance);
    ServiceCombServer allmatchInstance = new ServiceCombServer("test", transport, allmatchCacheEndpoint);
    DataCenterInfo info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region");
    info.setAvailableZone("test-zone");
    Mockito.when(discoveryInstance.getDataCenterInfo()).thenReturn(info);
    Mockito.when(discoveryInstance.getInstanceId()).thenReturn("allmatchInstance");

    MicroserviceInstance regionMatchDiscoveryInstance = Mockito.mock(MicroserviceInstance.class);
    List<String> regionMatchEndpoint = new ArrayList<>();
    regionMatchEndpoint.add("rest://localhost:9091");
    Mockito.when(regionMatchDiscoveryInstance.getEndpoints()).thenReturn(regionMatchEndpoint);
    CacheEndpoint regionMatchCacheEndpoint = Mockito.mock(CacheEndpoint.class);
    Mockito.when(regionMatchCacheEndpoint.getEndpoint()).thenReturn("rest://localhost:9091");
    Mockito.when(regionMatchCacheEndpoint.getInstance()).thenReturn(regionMatchDiscoveryInstance);
    ServiceCombServer regionMatchInstance = new ServiceCombServer("test", transport, regionMatchCacheEndpoint);
    DataCenterInfo regionMatchInfo = new DataCenterInfo();
    regionMatchInfo.setName("test");
    regionMatchInfo.setRegion("test-Region");
    regionMatchInfo.setAvailableZone("test-zone2");
    Mockito.when(regionMatchDiscoveryInstance.getDataCenterInfo()).thenReturn(regionMatchInfo);
    Mockito.when(regionMatchDiscoveryInstance.getInstanceId()).thenReturn("regionMatchInstance");

    MicroserviceInstance noneMatchDiscoveryInstance = Mockito.mock(MicroserviceInstance.class);
    List<String> noMatchEndpoint = new ArrayList<>();
    noMatchEndpoint.add("rest://localhost:9092");
    Mockito.when(noneMatchDiscoveryInstance.getEndpoints()).thenReturn(noMatchEndpoint);
    CacheEndpoint noneMatchCacheEndpoint = Mockito.mock(CacheEndpoint.class);
    Mockito.when(noneMatchCacheEndpoint.getEndpoint()).thenReturn("rest://localhost:9092");
    Mockito.when(noneMatchCacheEndpoint.getInstance()).thenReturn(noneMatchDiscoveryInstance);
    ServiceCombServer noneMatchInstance = new ServiceCombServer("test", transport, noneMatchCacheEndpoint);
    DataCenterInfo noneMatchInfo = new DataCenterInfo();
    noneMatchInfo.setName("test");
    noneMatchInfo.setRegion("test-Region2");
    noneMatchInfo.setAvailableZone("test-zone2");
    Mockito.when(noneMatchDiscoveryInstance.getDataCenterInfo()).thenReturn(noneMatchInfo);
    Mockito.when(noneMatchDiscoveryInstance.getInstanceId()).thenReturn("noneMatchInstance");

    // run test
    Invocation invocation = Mockito.mock(Invocation.class);
    List<ServiceCombServer> data = Arrays.asList(allmatchInstance, regionMatchInstance, noneMatchInstance);
    List<ServiceCombServer> result = filter.getFilteredListOfServers(data, invocation);

    // check result
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals("allmatchInstance", result.get(0).getInstance().getInstanceId());
  }
}
