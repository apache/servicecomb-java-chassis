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

package org.apache.servicecomb.serviceregistry.cache;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.cache.MicroserviceInstanceCache;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TestMicroserviceInstanceCache {
  @Before
  public void setup() {
    ConfigUtil.installDynamicConfig();
  }

  @AfterClass
  public static void classTeardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testGetOrCreateMicroservice(
      @Mocked Microservice microservice) {
    new MockUp<DiscoveryManager>() {
      @Mock
      public Microservice getMicroservice(String microserviceId) {
        if ("forkedid".equals(microserviceId)) {
          return microservice;
        }
        if ("forkedidNull".equals(microserviceId)) {
          return null;
        }
        throw new IllegalArgumentException("unrecognized param");
      }
    };

    Microservice cachedService = MicroserviceInstanceCache.getOrCreate("forkedid");
    Assertions.assertNotNull(cachedService);
    cachedService = MicroserviceInstanceCache.getOrCreate("forkedid");
    Assertions.assertNotNull(cachedService);
    cachedService = MicroserviceInstanceCache.getOrCreate("forkedidNull");
    Assertions.assertNull(cachedService);
  }

  @Test
  public void testGetOrCreateMicroserviceInstance(@Mocked ServiceRegistry serviceRegistry,
      @Mocked ServiceRegistryClient client,
      @Mocked MicroserviceInstance instance) {
    new MockUp<RegistryUtils>() {
      @Mock
      ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
      }
    };
    new Expectations() {
      {
        serviceRegistry.getServiceRegistryClient();
        result = client;
        client.findServiceInstance("forkedserviceid", "forkedinstanceid");
        result = instance;
        client.findServiceInstance("forkedserviceidNull", "forkedinstanceidNull");
        result = null;
      }
    };
    MicroserviceInstance cachedInstance = MicroserviceInstanceCache.getOrCreate("forkedserviceid", "forkedinstanceid");
    Assertions.assertNotNull(cachedInstance);
    cachedInstance = MicroserviceInstanceCache.getOrCreate("forkedserviceid", "forkedinstanceid");
    Assertions.assertNotNull(cachedInstance);
    cachedInstance = MicroserviceInstanceCache.getOrCreate("forkedserviceidNull", "forkedinstanceidNull");
    Assertions.assertNull(cachedInstance);
  }
}
