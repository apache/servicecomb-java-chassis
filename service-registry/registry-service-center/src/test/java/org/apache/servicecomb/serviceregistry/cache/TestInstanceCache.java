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
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.cache.CacheEndpoint;
import org.apache.servicecomb.registry.cache.InstanceCache;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

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
    Assertions.assertEquals("testAppID", instanceCache.getAppId());
    Assertions.assertEquals("testMicroServiceName", instanceCache.getMicroserviceName());
    Assertions.assertEquals("1.0", instanceCache.getMicroserviceVersionRule());
    Assertions.assertNotNull(instanceCache.getInstanceMap());
  }

  @Test
  public void testGetOrCreateTransportMap() {
    Map<String, List<CacheEndpoint>> transportMap = instanceCache.getOrCreateTransportMap();
    Assertions.assertEquals(1, transportMap.size());
  }

  @Test
  public void testCacheChanged() {
    InstanceCache newCache =
        new InstanceCache("testAppID", "testMicroServiceName", "1.0", instanceCache.getInstanceMap());
    Assertions.assertTrue(instanceCache.cacheChanged(newCache));
  }

  @Test
  public void getVersionedCache() {
    VersionedCache versionedCache = instanceCache.getVersionedCache();
    Assertions.assertEquals("1.0", versionedCache.name());
    Assertions.assertSame(instMap, versionedCache.data());
  }
}
