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

package io.servicecomb.serviceregistry.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;

public class TestInstanceCache {
  private static InstanceCache instanceCache = null;

  @BeforeClass
  public static void beforeClass() {
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setStatus(MicroserviceInstanceStatus.UP);
    List<String> endpoints = new ArrayList<>();
    endpoints.add("rest://127.0.0.1:8080");
    instance.setEndpoints(endpoints);
    instance.setInstanceId("1");

    Map<String, MicroserviceInstance> instMap = new HashMap<>();
    instMap.put(instance.getInstanceId(), instance);
    instanceCache = new InstanceCache("testAppID", "testMicroServiceName", "1.0", instMap);
  }

  @Test
  public void testGetMethod() {
    Assert.assertEquals("testAppID", instanceCache.getAppId());
    Assert.assertEquals("testMicroServiceName", instanceCache.getMicroserviceName());
    Assert.assertEquals("1.0", instanceCache.getMicroserviceVersionRule());
    Assert.assertNotNull(instanceCache.getInstanceMap());
  }

  @Test
  public void testGetOrCreateTransportMap() {
    Map<String, List<CacheEndpoint>> transportMap = instanceCache.getOrCreateTransportMap();
    Assert.assertEquals(1, transportMap.size());
  }

  @Test
  public void testCacheChanged() {
    InstanceCache newCache =
        new InstanceCache("testAppID", "testMicroServiceName", "1.0", instanceCache.getInstanceMap());
    Assert.assertTrue(instanceCache.cacheChanged(newCache));
  }
}
