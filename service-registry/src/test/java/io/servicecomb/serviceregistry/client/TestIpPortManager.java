/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.servicecomb.config.ConfigUtil;
import io.servicecomb.foundation.common.net.IpPort;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.cache.CacheEndpoint;
import io.servicecomb.serviceregistry.cache.InstanceCache;
import io.servicecomb.serviceregistry.cache.InstanceCacheManager;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.servicecomb.serviceregistry.registry.AbstractServiceRegistry;
import io.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
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
    ArrayList<IpPort> ipPortList = new ArrayList<IpPort>();
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
    IpPort address = manager.getAvailableAddress(false);
    Assert.assertEquals("127.0.0.1", address.getHostOrIp());
    Assert.assertEquals(9980, address.getPort());

    address = manager.getAvailableAddress(true);
    Assert.assertEquals("127.0.0.1", address.getHostOrIp());
    Assert.assertEquals(9981, address.getPort());

    address = manager.getAvailableAddress(false);
    Assert.assertEquals("127.0.0.1", address.getHostOrIp());
    Assert.assertEquals(9981, address.getPort());

    Map<String, List<CacheEndpoint>> addresses = new HashMap<>();
    List<CacheEndpoint> instances = new ArrayList<>();
    instances.add(new CacheEndpoint("http://127.0.0.1:9982", null));
    addresses.put("rest", instances);
    new Expectations() {
      {
        cacheManager.getOrCreate("default", "SERVICECENTER", "3.0.0");
        result = cache;
        cache.getOrCreateTransportMap();
        result = addresses;
      }
    };
    
    manager.initAutoDiscovery();
    address = manager.getAvailableAddress(true);
    Assert.assertEquals("127.0.0.1", address.getHostOrIp());
    Assert.assertEquals(9982, address.getPort());

    address = manager.getAvailableAddress(true);
    Assert.assertEquals("127.0.0.1", address.getHostOrIp());
    Assert.assertEquals(9980, address.getPort());
  }

  @Test
  public void testCreateServiceRegistryCacheWithInstanceCache() {

    List<MicroserviceInstance> list = new ArrayList<MicroserviceInstance>();
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
