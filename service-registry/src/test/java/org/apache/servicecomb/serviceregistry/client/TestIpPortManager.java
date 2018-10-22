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
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.CacheEndpoint;
import org.apache.servicecomb.serviceregistry.cache.InstanceCache;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManager;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.registry.AbstractServiceRegistry;
import org.apache.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
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

  IpPortManager manager;

  @Before
  public void setup() {
    ConfigUtil.createLocalConfig();
    serviceRegistry = (AbstractServiceRegistry) ServiceRegistryFactory.createLocal();
    serviceRegistry.setServiceRegistryClient(srClient);
    serviceRegistry.init();

    manager = serviceRegistry.getIpPortManager();
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

    IpPortManager manager = new IpPortManager(config, cacheManager);
    IpPort address1 = manager.getAvailableAddress();

    if (address1.getPort() == 9980) {
      Assert.assertEquals("127.0.0.1", address1.getHostOrIp());
      Assert.assertEquals(9980, address1.getPort());
    } else {
      Assert.assertEquals("127.0.0.1", address1.getHostOrIp());
      Assert.assertEquals(9981, address1.getPort());
    }

    IpPort address2 = manager.getNextAvailableAddress(address1);
    if (address1.getPort() == 9980) {
      Assert.assertEquals("127.0.0.1", address2.getHostOrIp());
      Assert.assertEquals(9981, address2.getPort());
    } else {
      Assert.assertEquals("127.0.0.1", address2.getHostOrIp());
      Assert.assertEquals(9980, address2.getPort());
    }

    IpPort address3 = manager.getAvailableAddress();
    if (address1.getPort() == 9980) {
      Assert.assertEquals("127.0.0.1", address3.getHostOrIp());
      Assert.assertEquals(9981, address3.getPort());
    } else {
      Assert.assertEquals("127.0.0.1", address3.getHostOrIp());
      Assert.assertEquals(9980, address3.getPort());
    }

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

    manager.initAutoDiscovery();  //init result is false at first time
    IpPort address4 = manager.getNextAvailableAddress(address3);
    if (address1.getPort() == 9980) {
      Assert.assertEquals("127.0.0.1", address4.getHostOrIp());
      Assert.assertEquals(9980, address4.getPort());
    } else {
      address4 = manager.getNextAvailableAddress(address1);
      Assert.assertEquals("127.0.0.1", address4.getHostOrIp());
      Assert.assertEquals(9980, address4.getPort());
    }

    IpPort address5 = manager.getNextAvailableAddress(address4);
    Assert.assertEquals("127.0.0.1", address5.getHostOrIp());
    Assert.assertEquals(9981, address5.getPort());

    manager.setAutoDiscoveryInited(true);
    IpPort address6 = manager.getNextAvailableAddress(address3);
    if (address1.getPort() == 9980) {
      Assert.assertEquals("127.0.0.1", address6.getHostOrIp());
      Assert.assertEquals(9982, address6.getPort());
    } else {
      address6 = manager.getNextAvailableAddress(address1);
      Assert.assertEquals("127.0.0.1", address6.getHostOrIp());
      Assert.assertEquals(9982, address6.getPort());
    }
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
