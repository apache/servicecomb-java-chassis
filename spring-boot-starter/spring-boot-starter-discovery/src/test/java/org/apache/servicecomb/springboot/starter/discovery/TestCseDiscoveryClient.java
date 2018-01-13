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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryContext;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTree;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTreeNode;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

public class TestCseDiscoveryClient {
  @Test
  public void testCseDiscoveryClient(@Mocked RegistryUtils registryUtils,
      @Injectable ServiceRegistryClient serviceRegistryClient,
      @Mocked DiscoveryTree discoveryTree,
      @Injectable DiscoveryTreeNode versionedCache) {
    List<Microservice> microserviceList = new ArrayList<>();
    Microservice service1 = new Microservice();
    service1.setServiceName("service1");
    microserviceList.add(service1);
    Microservice server2 = new Microservice();
    microserviceList.add(server2);
    server2.setServiceName("server2");

    Map<String, MicroserviceInstance> servers = new HashMap<>();
    List<String> endpoints = new ArrayList<>();
    endpoints.add("rest://localhost:3333");
    endpoints.add("rest://localhost:4444");
    MicroserviceInstance instance1 = new MicroserviceInstance();
    instance1.setServiceId("service1");
    instance1.setInstanceId("service1-instance1");
    instance1.setEndpoints(endpoints);
    servers.put("service1-instance1", instance1);

    new Expectations() {
      {
        RegistryUtils.getServiceRegistryClient();
        result = serviceRegistryClient;
        serviceRegistryClient.getAllMicroservices();
        result = microserviceList;
        discoveryTree.discovery((DiscoveryContext) any, anyString, anyString, anyString);
        result = versionedCache;
        versionedCache.data();
        result = servers;
      }
    };

    DiscoveryClient client = new CseDiscoveryClient();
    Assert.assertEquals("Spring Cloud CSE Discovery Client", client.description());
    Assert.assertEquals(null, client.getLocalServiceInstance());
    Assert.assertEquals(2, client.getServices().size());
    Assert.assertEquals("server2", client.getServices().get(1));
    Assert.assertEquals(2, client.getInstances("service1-instance1").size());
    Assert.assertEquals(4444, client.getInstances("service1-instance1").get(1).getPort());
  }
}
