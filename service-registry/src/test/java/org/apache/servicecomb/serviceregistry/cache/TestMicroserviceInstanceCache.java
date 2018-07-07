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

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.junit.Assert;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class TestMicroserviceInstanceCache {
  @Test
  public void testGetOrCreateMicroservice(@Mocked RegistryUtils utils, @Mocked ServiceRegistryClient client,
      @Mocked Microservice microservice) {
    new Expectations() {
      {
        RegistryUtils.getServiceRegistryClient();
        result = client;
        client.getMicroservice("forkedid");
        result = microservice;
        client.getMicroservice("forkedidNull");
        result = null;
      }
    };
    Microservice cachedService = MicroserviceInstanceCache.getOrCreate("forkedid");
    Assert.assertNotNull(cachedService);
    cachedService = MicroserviceInstanceCache.getOrCreate("forkedid");
    Assert.assertNotNull(cachedService);
    cachedService = MicroserviceInstanceCache.getOrCreate("forkedidNull");
    Assert.assertNull(cachedService);
  }

  @Test
  public void testGetOrCreateMicroserviceInstance(@Mocked RegistryUtils utils, @Mocked ServiceRegistryClient client,
      @Mocked MicroserviceInstance instance) {
    new Expectations() {
      {
        RegistryUtils.getServiceRegistryClient();
        result = client;
        client.findServiceInstance("forkedserviceid", "forkedinstanceid");
        result = instance;
        client.findServiceInstance("forkedserviceidNull", "forkedinstanceidNull");
        result = null;
      }
    };
    MicroserviceInstance cachedInstance = MicroserviceInstanceCache.getOrCreate("forkedserviceid", "forkedinstanceid");
    Assert.assertNotNull(cachedInstance);
    cachedInstance = MicroserviceInstanceCache.getOrCreate("forkedserviceid", "forkedinstanceid");
    Assert.assertNotNull(cachedInstance);
    cachedInstance = MicroserviceInstanceCache.getOrCreate("forkedserviceidNull", "forkedinstanceidNull");
    Assert.assertNull(cachedInstance);
  }
}
