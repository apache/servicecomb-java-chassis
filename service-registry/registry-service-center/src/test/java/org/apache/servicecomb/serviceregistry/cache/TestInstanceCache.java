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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestInstanceCache {
  private static InstanceCache instanceCache = null;

  static Map<String, MicroserviceInstance> instMap = new HashMap<>();

  @BeforeClass
  public static void beforeClass() {
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setStatus(MicroserviceInstanceStatus.UP);
    List<String> endpoints = new ArrayList<>();
    endpoints.add("rest://127.0.0.1:8080");
    instance.setEndpoints(endpoints);
    instance.setInstanceId("1");

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

  @Test
  public void getVersionedCache() {
    VersionedCache versionedCache = instanceCache.getVersionedCache();
    Assert.assertEquals("1.0", versionedCache.name());
    Assert.assertSame(instMap, versionedCache.data());
  }
}
