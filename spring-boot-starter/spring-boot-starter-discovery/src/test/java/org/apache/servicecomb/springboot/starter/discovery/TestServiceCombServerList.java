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
package org.apache.servicecomb.springboot.starter.discovery;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryContext;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTree;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTreeNode;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.Server;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

public class TestServiceCombServerList {
  @Test
  public void testServiceCombServerList(@Injectable IClientConfig iClientConfig,
      @Mocked RegistryUtils registryUtils,
      @Mocked DiscoveryTree discoveryTree,
      @Injectable DiscoveryTreeNode versionedCache) {
    List<String> endpoints = new ArrayList<>();
    endpoints.add("rest://localhost:3333");
    endpoints.add("highway://localhost:4444");
    MicroserviceInstance instance1 = new MicroserviceInstance();
    instance1.setServiceId("service1");
    instance1.setInstanceId("service1-instance1");
    instance1.setEndpoints(endpoints);

    new Expectations() {
      {
        iClientConfig.getClientName();
        result = "serviceId1";

        RegistryUtils.getAppId();
        result = "app";
        discoveryTree.discovery((DiscoveryContext) any, anyString, anyString, anyString);
        result = versionedCache;
        versionedCache.data();
        result = Lists.newArrayList(new Server("localhost", 3333));
      }
    };

    ServiceCombServerList list = new ServiceCombServerList();
    list.initWithNiwsConfig(iClientConfig);
    List<Server> serverList = list.getInitialListOfServers();
    Assert.assertEquals(1, serverList.size());
    Assert.assertEquals(3333, serverList.get(0).getPort());
    Assert.assertEquals(serverList.size(), list.getUpdatedListOfServers().size());
  }
}
