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

import org.apache.servicecomb.loadbalance.CseServer;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.DataCenterInfo;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.junit.Assert;
import org.junit.Test;

import com.netflix.loadbalancer.Server;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestZoneAwareServerListFilterExt {

  @Test
  public void testZoneAwareServerListFilterExt(@Mocked RegistryUtils registryUtils) {
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
    allmatchInstance.setDataCenterInfo(info);

    MicroserviceInstance regionMatchInstance = new MicroserviceInstance();
    info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region");
    info.setAvailableZone("test-zone2");
    regionMatchInstance.setDataCenterInfo(info);

    MicroserviceInstance noneMatchInstance = new MicroserviceInstance();
    info = new DataCenterInfo();
    info.setName("test");
    info.setRegion("test-Region2");
    info.setAvailableZone("test-zone2");
    noneMatchInstance.setDataCenterInfo(info);

    new Expectations() {
      {
        RegistryUtils.getMicroserviceInstance();
        result = myself;
      }
    };
    ZoneAwareServerListFilterExt filter = new ZoneAwareServerListFilterExt();
    List<Server> servers = new ArrayList<>();
    CseServer noneMatchServer = new MockUp<CseServer>() {
      @Mock
      public String toString() {
        return "noneMatchServer";
      }

      @Mock
      public String getHost() {
        return "noneMatchServer";
      }

      @Mock
      public MicroserviceInstance getInstance() {
        return noneMatchInstance;
      }
    }.getMockInstance();
    CseServer regionMatchregionMatchServer = new MockUp<CseServer>() {
      @Mock
      public String toString() {
        return "regionMatchregionMatchServer";
      }

      @Mock
      public String getHost() {
        return "regionMatchregionMatchServer";
      }

      @Mock
      public MicroserviceInstance getInstance() {
        return regionMatchInstance;
      }
    }.getMockInstance();

    CseServer allmatchServer = new MockUp<CseServer>() {
      @Mock
      public String toString() {
        return "allmatchServer";
      }

      @Mock
      public String getHost() {
        return "allmatchServer";
      }

      @Mock
      public MicroserviceInstance getInstance() {
        return allmatchInstance;
      }
    }.getMockInstance();

    servers.add(noneMatchServer);
    List<Server> result = filter.getFilteredListOfServers(servers);
    Assert.assertEquals(result.size(), 1);
    Assert.assertEquals(result.get(0), noneMatchServer);

    servers.add(regionMatchregionMatchServer);
    result = filter.getFilteredListOfServers(servers);
    Assert.assertEquals(result.size(), 1);
    Assert.assertEquals(result.get(0), regionMatchregionMatchServer);

    servers.add(allmatchServer);
    result = filter.getFilteredListOfServers(servers);
    Assert.assertEquals(result.size(), 1);
    Assert.assertEquals(result.get(0), allmatchServer);
  }
}
