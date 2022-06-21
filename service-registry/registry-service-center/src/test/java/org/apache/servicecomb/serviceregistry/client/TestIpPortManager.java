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

package org.apache.servicecomb.serviceregistry.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.cache.CacheEndpoint;
import org.apache.servicecomb.registry.cache.InstanceCache;
import org.apache.servicecomb.registry.cache.InstanceCacheManager;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.refresh.ClassificationAddress;
import org.apache.servicecomb.serviceregistry.registry.AbstractServiceRegistry;
import org.apache.servicecomb.serviceregistry.registry.LocalServiceRegistryFactory;
import org.junit.Before;
import org.junit.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestIpPortManager {
  @Mocked
  ServiceRegistryClient srClient;

  AbstractServiceRegistry serviceRegistry;

  @Before
  public void setup() {
    ConfigUtil.createLocalConfig();
    serviceRegistry = (AbstractServiceRegistry) LocalServiceRegistryFactory.createLocal();
    serviceRegistry.setServiceRegistryClient(srClient);
    serviceRegistry.init();
  }

  @Test
  public void testGetAvailableAddress(@Injectable ServiceRegistryConfig config,
      @Injectable InstanceCacheManager cacheManager,
      @Injectable InstanceCache cache,
      @Injectable ClassificationAddress classificationAddress) {
    ArrayList<IpPort> ipPortList = new ArrayList<>();
    ipPortList.add(new IpPort("127.0.0.1", 9980));
    ipPortList.add(new IpPort("127.0.0.1", 9981));

    new Expectations() {
      {
        config.getIpPort();
        result = ipPortList;
        config.getTransport();
        result = "rest";
        config.isRegistryAutoDiscovery();
        result = true;
      }
    };

    IpPortManager manager = new IpPortManager(config);
    manager.instanceCacheManager = cacheManager;
    IpPort address1 = manager.getAvailableAddress();

    // test initial
    Assertions.assertEquals("127.0.0.1", address1.getHostOrIp());
    Assertions.assertTrue(address1.getPort() == 9980 || address1.getPort() == 9981);

    // test getAvailableAddress()
    IpPort address2 = manager.getAvailableAddress();
    Assertions.assertEquals("127.0.0.1", address2.getHostOrIp());
    if (address1.getPort() == 9980) {
      Assertions.assertEquals(9981, address2.getPort());
    } else {
      Assertions.assertEquals(9980, address2.getPort());
    }

    // test getAvailableAddress() when reaching the end
    IpPort address3 = manager.getAvailableAddress();
    Assertions.assertEquals("127.0.0.1", address3.getHostOrIp());
    Assertions.assertEquals(address1.getPort(), address3.getPort());

    // mock endpoint list
    Map<String, List<CacheEndpoint>> addresses = new HashMap<>();
    List<CacheEndpoint> cacheEndpoints = new ArrayList<>();
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setDataCenterInfo(null);
    cacheEndpoints.add(new CacheEndpoint("http://127.0.0.1:9982", instance));
    addresses.put("rest", cacheEndpoints);
    manager.classificationAddress = new ClassificationAddress(config, cacheManager);
    new Expectations() {
      {
        cacheManager.getOrCreate("default", "SERVICECENTER", "latest");
        result = cache;
        cache.getOrCreateTransportMap();
        result = addresses;
      }
    };

    // test getAvailableAddress() when auto discovery is disabled
    IpPort address4 = manager.getAvailableAddress();
    Assertions.assertEquals("127.0.0.1", address4.getHostOrIp());
    if (address1.getPort() == 9980) {
      address4 = manager.getAvailableAddress();
    }
    Assertions.assertEquals(9980, address4.getPort());

    IpPort address5 = manager.getAvailableAddress();
    Assertions.assertEquals("127.0.0.1", address5.getHostOrIp());
    Assertions.assertEquals(9981, address5.getPort());

    //mock RegistrationManager.INSTANCE
    String instanceId = "e8a04b54cf2711e7b701286ed488fc20";
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setInstanceId(instanceId);
    Map<String, String> properties = new HashMap<>();
    microserviceInstance.setProperties(properties);
    new Expectations(RegistrationManager.INSTANCE) {
      {
        RegistrationManager.INSTANCE.getMicroserviceInstance();
        result = microserviceInstance;
      }
    };
    // test getAvailable address when auto discovery is enabled
    manager.initAutoDiscovery();
    manager.setAutoDiscoveryInited(true);
    IpPort address6 = manager.getAvailableAddress();
    Assertions.assertEquals("127.0.0.1", address6.getHostOrIp());
    Assertions.assertEquals(9982, address6.getPort());
  }

  @Test
  public void testCreateServiceRegistryCacheWithInstanceCache() {

    List<MicroserviceInstance> list = new ArrayList<>();
    MicroserviceInstance e1 = new MicroserviceInstance();
    list.add(e1);

    new MockUp<RegistryUtils>() {

      @Mock
      public List<MicroserviceInstance> findServiceInstance(String appId, String serviceName,
          String versionRule) {
        return list;
      }
    };
  }
}
