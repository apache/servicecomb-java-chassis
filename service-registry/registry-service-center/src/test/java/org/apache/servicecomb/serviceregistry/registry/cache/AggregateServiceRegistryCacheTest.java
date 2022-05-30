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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.servicecomb.serviceregistry.registry.cache.AggregateMicroserviceCacheTest.MockMicroserviceCache;
import org.apache.servicecomb.serviceregistry.registry.cache.AggregateMicroserviceCacheTest.MockServiceRegistry;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache.MicroserviceCacheStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class AggregateServiceRegistryCacheTest {

  private MicroserviceCacheKey microserviceCacheKey;

  private MockServiceRegistry mockServiceRegistry0;

  private MockServiceRegistry mockServiceRegistry1;

  private MockServiceRegistry mockServiceRegistry2;

  private AggregateServiceRegistryCache aggregateServiceRegistryCache;

  @Before
  public void before() {
    microserviceCacheKey = MicroserviceCacheKey.builder()
        .serviceName("svc").appId("app").env("env").build();

    mockServiceRegistry0 = new MockServiceRegistry()
        .setName("s0")
        .addCache(new MockMicroserviceCache(
            microserviceCacheKey,
            MicroserviceCacheStatus.NO_CHANGE));
    mockServiceRegistry1 = new MockServiceRegistry()
        .setName("s1")
        .addCache(new MockMicroserviceCache(
            microserviceCacheKey,
            MicroserviceCacheStatus.REFRESHED))
        .addCache(new MockMicroserviceCache(
            MicroserviceCacheKey.builder().serviceName("svc2").appId("app").env("env").build(),
            MicroserviceCacheStatus.NO_CHANGE));
    mockServiceRegistry2 = new MockServiceRegistry()
        .setName("s2")
        .addCache(new MockMicroserviceCache(
            microserviceCacheKey,
            MicroserviceCacheStatus.SERVICE_NOT_FOUND));

    aggregateServiceRegistryCache = new AggregateServiceRegistryCache(
        Arrays.asList(mockServiceRegistry0, mockServiceRegistry1, mockServiceRegistry2));
  }

  @Test
  public void findServiceCache() {
    MicroserviceCache serviceCache = aggregateServiceRegistryCache.findServiceCache(
        MicroserviceCacheKey.builder().serviceName("svc").appId("app").env("env").build()
    );

    Assertions.assertTrue(serviceCache instanceof AggregateMicroserviceCache);
    AggregateMicroserviceCache aggregateMicroserviceCache = (AggregateMicroserviceCache) serviceCache;
    Assertions.assertEquals(2, aggregateMicroserviceCache.caches.size());
    Assertions.assertSame(mockServiceRegistry0.findMicroserviceCache(microserviceCacheKey),
        aggregateMicroserviceCache.caches.get(mockServiceRegistry0.getName()));
    Assertions.assertSame(mockServiceRegistry1.findMicroserviceCache(microserviceCacheKey),
        aggregateMicroserviceCache.caches.get(mockServiceRegistry1.getName()));
    // aggregateMicroserviceCache holds the cache of svc
    Assertions.assertEquals(1, aggregateServiceRegistryCache.microserviceCache.size());
    Assertions.assertNotNull(aggregateServiceRegistryCache.microserviceCache.get(microserviceCacheKey));

    MicroserviceCache serviceCache2 = aggregateServiceRegistryCache.findServiceCache(
        MicroserviceCacheKey.builder().serviceName("svc2").appId("app").env("env").build()
    );

    Assertions.assertTrue(serviceCache2 instanceof AggregateMicroserviceCache);
    AggregateMicroserviceCache aggregateMicroserviceCache2 = (AggregateMicroserviceCache) serviceCache2;
    Assertions.assertEquals(1, aggregateMicroserviceCache2.caches.size());
    Assertions.assertSame(
        mockServiceRegistry1.findMicroserviceCache(
            MicroserviceCacheKey.builder().serviceName("svc2").appId("app").env("env").build()),
        aggregateMicroserviceCache2.caches.get(mockServiceRegistry1.getName()));
    Assertions.assertEquals(2, aggregateServiceRegistryCache.microserviceCache.size());
    Assertions.assertNotNull(aggregateServiceRegistryCache.microserviceCache.get(
        MicroserviceCacheKey.builder().serviceName("svc2").appId("app").env("env").build()
    ));
  }

  @Test
  public void findServiceCache_not_found() {
    MicroserviceCache serviceCache = aggregateServiceRegistryCache.findServiceCache(
        MicroserviceCacheKey.builder().serviceName("svc-not-exist").appId("app").env("env").build()
    );

    Assertions.assertTrue(serviceCache instanceof AggregateMicroserviceCache);
    Assertions.assertEquals(MicroserviceCacheStatus.SERVICE_NOT_FOUND, serviceCache.getStatus());
    AggregateMicroserviceCache aggregateMicroserviceCache = (AggregateMicroserviceCache) serviceCache;
    Assertions.assertEquals(0, aggregateMicroserviceCache.caches.size());
    Assertions.assertEquals(3, aggregateMicroserviceCache.serviceRegistries.size());
    // should remove the cache of not existing microservice
    Assertions.assertEquals(0, aggregateServiceRegistryCache.microserviceCache.size());
  }

  @Test
  public void onMicroserviceCacheRefreshed() {
    MicroserviceCacheKey microserviceCacheKey =
        MicroserviceCacheKey.builder().serviceName("svc").appId("app").env("env").build();
    MicroserviceCacheKey microserviceCacheKey2 =
        MicroserviceCacheKey.builder().serviceName("svc2").appId("app").env("env").build();
    aggregateServiceRegistryCache.onMicroserviceCacheRefreshed(new MicroserviceCacheRefreshedEvent(
        Collections.singletonList(
            new MockMicroserviceCache(
                microserviceCacheKey,
                MicroserviceCacheStatus.REFRESHED
            )
        )
    ));

    Assertions.assertTrue(aggregateServiceRegistryCache.microserviceCache.isEmpty());

    MicroserviceCache serviceCache = aggregateServiceRegistryCache.findServiceCache(microserviceCacheKey);
    MicroserviceCache serviceCache2 = aggregateServiceRegistryCache.findServiceCache(microserviceCacheKey2);

    Assertions.assertEquals("1", serviceCache.getRevisionId());
    Assertions.assertEquals("1", serviceCache2.getRevisionId());

    aggregateServiceRegistryCache.onMicroserviceCacheRefreshed(new MicroserviceCacheRefreshedEvent(
        Collections.singletonList(
            new MockMicroserviceCache(
                microserviceCacheKey,
                MicroserviceCacheStatus.REFRESHED
            )
        )
    ));

    Assertions.assertEquals("2", serviceCache.getRevisionId());
    Assertions.assertEquals("1", serviceCache2.getRevisionId());

    // test watcher
    ArrayList<Object> refreshedCaches = new ArrayList<>();
    aggregateServiceRegistryCache.setCacheRefreshedWatcher(refreshedCaches::addAll);

    aggregateServiceRegistryCache.onMicroserviceCacheRefreshed(new MicroserviceCacheRefreshedEvent(
        Arrays.asList(
            new MockMicroserviceCache(
                microserviceCacheKey,
                MicroserviceCacheStatus.REFRESHED
            ),
            new MockMicroserviceCache(
                microserviceCacheKey2,
                MicroserviceCacheStatus.REFRESHED
            )
        )
    ));

    Assertions.assertEquals("3", serviceCache.getRevisionId());
    Assertions.assertEquals("2", serviceCache2.getRevisionId());
    Assertions.assertEquals(2, refreshedCaches.size());
    Assertions.assertSame(serviceCache, refreshedCaches.get(0));
    Assertions.assertSame(serviceCache2, refreshedCaches.get(1));

    refreshedCaches.clear();

    // test removing not existing service cache
    ((MockMicroserviceCache) mockServiceRegistry0.findMicroserviceCache(microserviceCacheKey))
        .setStatus(MicroserviceCacheStatus.SERVICE_NOT_FOUND);
    ((MockMicroserviceCache) mockServiceRegistry1.findMicroserviceCache(microserviceCacheKey))
        .setStatus(MicroserviceCacheStatus.SERVICE_NOT_FOUND);
    aggregateServiceRegistryCache.onMicroserviceCacheRefreshed(new MicroserviceCacheRefreshedEvent(
        Arrays.asList(
            new MockMicroserviceCache(
                microserviceCacheKey,
                MicroserviceCacheStatus.REFRESHED
            ),
            new MockMicroserviceCache(
                microserviceCacheKey2,
                MicroserviceCacheStatus.REFRESHED
            )
        )
    ));

    Assertions.assertEquals("4", serviceCache.getRevisionId());
    Assertions.assertEquals("3", serviceCache2.getRevisionId());
    Assertions.assertEquals(2, refreshedCaches.size());
    Assertions.assertSame(serviceCache, refreshedCaches.get(0));
    Assertions.assertSame(serviceCache2, refreshedCaches.get(1));
    Assertions.assertEquals(MicroserviceCacheStatus.SERVICE_NOT_FOUND, serviceCache.getStatus());
    // not existing service cache removed, only serviceCache2 is left
    Assertions.assertEquals(1, aggregateServiceRegistryCache.microserviceCache.size());
    Assertions.assertSame(serviceCache2, aggregateServiceRegistryCache.microserviceCache.get(microserviceCacheKey2));
  }
}
