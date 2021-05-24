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
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.cache.CacheEndpoint;
import org.apache.servicecomb.registry.cache.InstanceCache;
import org.apache.servicecomb.registry.cache.InstanceCacheManager;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.registry.AbstractServiceRegistry;
import org.apache.servicecomb.serviceregistry.registry.LocalServiceRegistryFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

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
      @Injectable InstanceCache cache) {
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
    Assert.assertEquals("127.0.0.1", address1.getHostOrIp());
    Assert.assertTrue(address1.getPort() == 9980 || address1.getPort() == 9981);

    // test getAvailableAddress()
    IpPort address2 = manager.getAvailableAddress();
    Assert.assertEquals("127.0.0.1", address2.getHostOrIp());
    if (address1.getPort() == 9980) {
      Assert.assertEquals(9981, address2.getPort());
    } else {
      Assert.assertEquals(9980, address2.getPort());
    }

    // test getAvailableAddress() when reaching the end
    IpPort address3 = manager.getAvailableAddress();
    Assert.assertEquals("127.0.0.1", address3.getHostOrIp());
    Assert.assertEquals(address1.getPort(), address3.getPort());

    // mock endpoint list
    Map<String, List<CacheEndpoint>> addresses = new HashMap<>();
    List<CacheEndpoint> instances = new ArrayList<>();
    instances.add(new CacheEndpoint("http://127.0.0.1:9982", null));
    addresses.put("rest", instances);
    new Expectations() {
      {
        cacheManager.getOrCreate("default", "SERVICECENTER", "latest");
        result = cache;
        cache.getOrCreateTransportMap();
        result = addresses;
      }
    };

    // test getAvailableAddress() when auto discovery is disabled
    manager.initAutoDiscovery();  //init result is false at first time
    IpPort address4 = manager.getAvailableAddress();
    Assert.assertEquals("127.0.0.1", address4.getHostOrIp());
    if (address1.getPort() == 9980) {
      address4 = manager.getAvailableAddress();
    }
    Assert.assertEquals(9980, address4.getPort());

    // test getAvailable address when auto discovery is enabled
    manager.setAutoDiscoveryInited(true);
    IpPort address5 = manager.getAvailableAddress();
    Assert.assertEquals("127.0.0.1", address5.getHostOrIp());
    Assert.assertEquals(9981, address5.getPort());

    IpPort address6 = manager.getAvailableAddress();
    Assert.assertEquals("127.0.0.1", address6.getHostOrIp());
    Assert.assertEquals(9982, address6.getPort());
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
