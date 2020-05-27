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

package org.apache.servicecomb.serviceregistry.registry.cache;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.registry.EmptyMockServiceRegistry;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache.MicroserviceCacheStatus;
import org.junit.Assert;
import org.junit.Test;

public class AggregateMicroserviceCacheTest {

  @Test
  public void refresh() {
    MicroserviceCacheKey microserviceCacheKey =
        MicroserviceCacheKey.builder().serviceName("svc").appId("app").env("production").build();

    MockMicroserviceCache mockMicroserviceCache0 = new MockMicroserviceCache(microserviceCacheKey,
        MicroserviceCacheStatus.NO_CHANGE);
    MockMicroserviceCache mockMicroserviceCache2 = new MockMicroserviceCache(microserviceCacheKey,
        MicroserviceCacheStatus.REFRESHED);
    mockMicroserviceCache2.instances = Arrays.asList(new MicroserviceInstance(), new MicroserviceInstance());
    MockMicroserviceCache mockMicroserviceCache3 = new MockMicroserviceCache(microserviceCacheKey,
        MicroserviceCacheStatus.SERVICE_NOT_FOUND);

    MockServiceRegistry mockServiceRegistry0 = new MockServiceRegistry().setName("s0")
        .addCache(mockMicroserviceCache0)
        .addCache(new MockMicroserviceCache(
            MicroserviceCacheKey.builder().serviceName("svc2").appId("app").env("production").build(),
            MicroserviceCacheStatus.REFRESHED));
    MockServiceRegistry mockServiceRegistry1 = new MockServiceRegistry().setName("s1");
    MockServiceRegistry mockServiceRegistry2 = new MockServiceRegistry().setName("s2")
        .addCache(mockMicroserviceCache2);
    MockServiceRegistry mockServiceRegistry3 = new MockServiceRegistry().setName("s3")
        .addCache(mockMicroserviceCache3);

    List<ServiceRegistry> serviceRegistries = Arrays.asList(
        mockServiceRegistry0,
        mockServiceRegistry1,
        mockServiceRegistry2,
        mockServiceRegistry3
    );

    AggregateMicroserviceCache compositeMicroserviceCache = new AggregateMicroserviceCache(
        microserviceCacheKey,
        serviceRegistries);

    // Test initialization
    // key
    Assert.assertSame(microserviceCacheKey, compositeMicroserviceCache.getKey());
    // status
    Assert.assertEquals(MicroserviceCacheStatus.REFRESHED, compositeMicroserviceCache.getStatus());
    // revision
    Assert.assertEquals("1", compositeMicroserviceCache.getRevisionId());
    Assert.assertEquals(1L, compositeMicroserviceCache.revisionCounter.get());
    // MicroserviceCache map
    Assert.assertEquals(2, compositeMicroserviceCache.caches.size());
    Assert.assertSame(mockMicroserviceCache0, compositeMicroserviceCache.caches.get("s0"));
    Assert.assertSame(mockMicroserviceCache2, compositeMicroserviceCache.caches.get("s2"));
    // ServiceRegistry collection
    Assert.assertEquals(serviceRegistries.size(), compositeMicroserviceCache.serviceRegistries.size());
    Iterator<ServiceRegistry> serviceRegistryIterator = compositeMicroserviceCache.serviceRegistries.iterator();
    Assert.assertSame(serviceRegistries.get(0), serviceRegistryIterator.next());
    Assert.assertSame(serviceRegistries.get(1), serviceRegistryIterator.next());
    Assert.assertSame(serviceRegistries.get(2), serviceRegistryIterator.next());
    Assert.assertSame(serviceRegistries.get(3), serviceRegistryIterator.next());
    // cached instances
    Assert.assertEquals(2, compositeMicroserviceCache.getInstances().size());
    Assert.assertSame(mockMicroserviceCache2.instances.get(0), compositeMicroserviceCache.getInstances().get(0));
    Assert.assertSame(mockMicroserviceCache2.instances.get(1), compositeMicroserviceCache.getInstances().get(1));

    // Test refresh()
    mockMicroserviceCache0.instances = Collections.singletonList(new MicroserviceInstance());
    mockMicroserviceCache2.instances = Collections.singletonList(new MicroserviceInstance());
    compositeMicroserviceCache.refresh();
    // status
    Assert.assertEquals(MicroserviceCacheStatus.REFRESHED, compositeMicroserviceCache.getStatus());
    // revision
    Assert.assertEquals("2", compositeMicroserviceCache.getRevisionId());
    Assert.assertEquals(2L, compositeMicroserviceCache.revisionCounter.get());
    // cached instances
    Assert.assertEquals(2, compositeMicroserviceCache.getInstances().size());
    Assert.assertSame(mockMicroserviceCache0.instances.get(0), compositeMicroserviceCache.getInstances().get(0));
    Assert.assertSame(mockMicroserviceCache2.instances.get(0), compositeMicroserviceCache.getInstances().get(1));

    // Test refresh()
    // microservice deleted and registered
    mockMicroserviceCache0.status = MicroserviceCacheStatus.SERVICE_NOT_FOUND;
    mockMicroserviceCache3.status = MicroserviceCacheStatus.REFRESHED;
    compositeMicroserviceCache.refresh();
    // status
    Assert.assertEquals(MicroserviceCacheStatus.REFRESHED, compositeMicroserviceCache.getStatus());
    // revision
    Assert.assertEquals("3", compositeMicroserviceCache.getRevisionId());
    Assert.assertEquals(3L, compositeMicroserviceCache.revisionCounter.get());
    // ServiceRegistries
    Assert.assertNotNull(compositeMicroserviceCache.caches.get("s2"));
    Assert.assertNotNull(compositeMicroserviceCache.caches.get("s3"));
    // cached instances
    Assert.assertEquals(1, compositeMicroserviceCache.getInstances().size());
    Assert.assertSame(mockMicroserviceCache2.instances.get(0), compositeMicroserviceCache.getInstances().get(0));
  }

  public static class MockServiceRegistry extends EmptyMockServiceRegistry {
    String name;

    Map<MicroserviceCacheKey, MicroserviceCache> cacheMap = new HashMap<>();

    public MockServiceRegistry setName(String name) {
      this.name = name;
      return this;
    }

    public MockServiceRegistry addCache(MicroserviceCache microserviceCache) {
      cacheMap.put(microserviceCache.getKey(), microserviceCache);
      return this;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public MicroserviceCache findMicroserviceCache(MicroserviceCacheKey microserviceCacheKey) {
      return cacheMap.get(microserviceCacheKey);
    }
  }

  public static class MockMicroserviceCache extends RefreshableMicroserviceCache {
    public MockMicroserviceCache(MicroserviceCacheKey key, MicroserviceCacheStatus microserviceCacheStatus) {
      super(null, key, null, false);
      setStatus(microserviceCacheStatus);
    }
  }
}
