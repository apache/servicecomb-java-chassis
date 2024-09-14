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
import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.config.DataCenterProperties;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryTreeNode;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class TestZoneAwareDiscoveryFilter {
  Environment environment = Mockito.mock(Environment.class);

  @BeforeEach
  public void setUp() {
    Mockito.when(environment.getProperty("servicecomb.loadbalance.filter.zoneaware.enabled",
        Boolean.class, true)).thenReturn(true);
  }

  @Test
  public void test_not_enough_instance() {
    Mockito.when(environment.getProperty("servicecomb.loadbalance.filter.zoneaware.ratio",
        int.class, 30)).thenReturn(50);
    Mockito.when(environment.getProperty("servicecomb.loadbalance.filter.zoneaware.ratioCeiling",
        int.class, 50)).thenReturn(70);

    ZoneAwareDiscoveryFilter filter = new ZoneAwareDiscoveryFilter();
    filter.setEnvironment(environment);

    // set up data
    DataCenterProperties myself = new DataCenterProperties();
    myself.setName("test");
    myself.setRegion("test-Region");
    myself.setAvailableZone("test-zone");
    filter.setDataCenterProperties(myself);

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

    // run test
    List<StatefulDiscoveryInstance> data = Arrays.asList(allmatchInstance, regionMatchInstance, noneMatchInstance);
    DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent").data(data);
    DiscoveryContext context = new DiscoveryContext();
    DiscoveryTreeNode result = filter.discovery(context, parent);

    // check result
    Integer level = context.getContextParameter(filter.contextParameter());
    Integer groups = parent.attribute(filter.groupsSizeParameter());
    List<StatefulDiscoveryInstance> resultData = result.data();
    Assertions.assertEquals(1, level);
    Assertions.assertEquals(2, groups);
    Assertions.assertEquals(2, resultData.size());
    Assertions.assertEquals("regionMatchInstance", resultData.get(0).getInstanceId());
    Assertions.assertEquals("allmatchInstance", resultData.get(1).getInstanceId());
  }

  @Test
  public void test_not_enough_instance_both_ceiling_floor() {
    Mockito.when(environment.getProperty("servicecomb.loadbalance.filter.zoneaware.ratio",
        int.class, 30)).thenReturn(40);
    Mockito.when(environment.getProperty("servicecomb.loadbalance.filter.zoneaware.ratioCeiling",
        int.class, 60)).thenReturn(60);

    ZoneAwareDiscoveryFilter filter = new ZoneAwareDiscoveryFilter();
    filter.setEnvironment(environment);

    // set up data
    DataCenterProperties myself = new DataCenterProperties();
    myself.setName("test");
    myself.setRegion("test-Region");
    myself.setAvailableZone("test-zone");
    filter.setDataCenterProperties(myself);

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

    // run test
    List<StatefulDiscoveryInstance> data = Arrays.asList(allmatchInstance, regionMatchInstance, noneMatchInstance);
    DiscoveryTreeNode parent = new DiscoveryTreeNode().name("parent").data(data);
    DiscoveryContext context = new DiscoveryContext();
    DiscoveryTreeNode result = filter.discovery(context, parent);

    // check result
    Integer level = context.getContextParameter(filter.contextParameter());
    Integer groups = parent.attribute(filter.groupsSizeParameter());
    List<StatefulDiscoveryInstance> resultData = result.data();
    Assertions.assertEquals(null, level);
    Assertions.assertEquals(1, groups);
    Assertions.assertEquals(3, resultData.size());
    Assertions.assertEquals("noneMatchInstance", resultData.get(0).getInstanceId());
    Assertions.assertEquals("regionMatchInstance", resultData.get(1).getInstanceId());
    Assertions.assertEquals("allmatchInstance", resultData.get(2).getInstanceId());
  }
}
