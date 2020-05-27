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
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.FindInstancesResponse;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache.MicroserviceCacheStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RefreshableServiceRegistryCacheTest {

  private Holder<Function<String, MicroserviceInstances>> pullInstanceFromServiceCenterLogic = new Holder<>(
      rev -> {
        MicroserviceInstances microserviceInstances = new MicroserviceInstances();
        microserviceInstances.setMicroserviceNotExist(false);
        microserviceInstances.setNeedRefresh(true);
        microserviceInstances.setRevision(rev);
        FindInstancesResponse instancesResponse = new FindInstancesResponse();
        instancesResponse.setInstances(new ArrayList<>());
        microserviceInstances.setInstancesResponse(instancesResponse);
        return microserviceInstances;
      }
  );

  private RefreshableServiceRegistryCache serviceRegistryCache;

  private Microservice consumerService;

  @Before
  public void setUp() throws Exception {
    serviceRegistryCache = new RefreshableServiceRegistryCache(consumerService, null) {
      @Override
      RefreshableMicroserviceCache createMicroserviceCache(MicroserviceCacheKey microserviceCacheKey) {
        return new RefreshableMicroserviceCache(consumerService, microserviceCacheKey, null, false) {
          @Override
          MicroserviceInstances pullInstanceFromServiceCenter(String revisionId) {
            return pullInstanceFromServiceCenterLogic.value.apply(revisionId);
          }
        };
      }
    };
    consumerService = new Microservice();
    consumerService.setServiceId("testConsumer");
  }

  @Test
  public void find_service_instances() {
    MicroserviceCache microserviceCache = serviceRegistryCache
        .findServiceCache(MicroserviceCacheKey.builder().serviceName("svc").appId("app").env("env").build());

    Assert.assertEquals(MicroserviceCacheStatus.REFRESHED, microserviceCache.getStatus());
    Assert.assertEquals(0, microserviceCache.getInstances().size());
    Assert.assertEquals(1, serviceRegistryCache.microserviceCache.size());
    Entry<MicroserviceCacheKey, RefreshableMicroserviceCache> cacheEntry =
        serviceRegistryCache.microserviceCache.entrySet().iterator().next();
    Assert.assertEquals(MicroserviceCacheKey.builder().serviceName("svc").appId("app").env("env").build(),
        cacheEntry.getKey());
  }

  @Test
  public void refreshCache() {
    RefreshableMicroserviceCache microserviceCache = new RefreshableMicroserviceCache(
        consumerService,
        MicroserviceCacheKey.builder().serviceName("svc").appId("appId").env("env").build(),
        null, false) {
      @Override
      public void refresh() {
        this.status = MicroserviceCacheStatus.REFRESHED;
      }
    };
    RefreshableMicroserviceCache microserviceCache2 = new RefreshableMicroserviceCache(
        consumerService,
        MicroserviceCacheKey.builder().serviceName("svc2").appId("appId").env("env").build(),
        null, false);
    RefreshableMicroserviceCache microserviceCache3 = new RefreshableMicroserviceCache(
        consumerService,
        MicroserviceCacheKey.builder().serviceName("svc3").appId("appId").env("env").build(),
        null, false) {
      @Override
      public void refresh() {
        this.status = MicroserviceCacheStatus.SERVICE_NOT_FOUND;
      }
    };

    serviceRegistryCache.microserviceCache.put(microserviceCache.getKey(), microserviceCache);
    serviceRegistryCache.microserviceCache.put(microserviceCache2.getKey(), microserviceCache2);
    serviceRegistryCache.microserviceCache.put(microserviceCache3.getKey(), microserviceCache3);

    List<MicroserviceCache> refreshedCaches = new ArrayList<>();
    serviceRegistryCache.setCacheRefreshedWatcher(refreshedCaches::addAll);

    serviceRegistryCache.refreshCache();

    Assert.assertEquals(2, refreshedCaches.size());
    Assert.assertSame(microserviceCache.getKey(), refreshedCaches.get(0).getKey());
    Assert.assertSame(microserviceCache3.getKey(), refreshedCaches.get(1).getKey());
    Assert.assertEquals(2, serviceRegistryCache.microserviceCache.size());
    Assert.assertSame(microserviceCache, serviceRegistryCache.microserviceCache.get(microserviceCache.getKey()));
    Assert.assertSame(microserviceCache2, serviceRegistryCache.microserviceCache.get(microserviceCache2.getKey()));
  }

  @Test
  public void forceRefreshCache() {
    RefreshableMicroserviceCache microserviceCache = new RefreshableMicroserviceCache(
        consumerService,
        MicroserviceCacheKey.builder().serviceName("svc").appId("appId").env("env").build(),
        null, false) {
      @Override
      public void forceRefresh() {
        this.status = MicroserviceCacheStatus.REFRESHED;
      }
    };

    serviceRegistryCache.microserviceCache.put(microserviceCache.getKey(), microserviceCache);

    List<MicroserviceCache> refreshedCaches = new ArrayList<>();
    serviceRegistryCache.setCacheRefreshedWatcher(refreshedCaches::addAll);

    serviceRegistryCache.forceRefreshCache();

    Assert.assertEquals(1, refreshedCaches.size());
    Assert.assertSame(microserviceCache.getKey(), refreshedCaches.get(0).getKey());
  }

  @Test
  public void findServiceCache_normal() {
    mockServiceRegistryHolder().value = MicroserviceCacheStatus.REFRESHED;

    MicroserviceCacheKey cacheKey = MicroserviceCacheKey.builder().serviceName("svc").appId("app").env("env").build();
    MicroserviceCache serviceCache = serviceRegistryCache.findServiceCache(cacheKey);

    Assert.assertSame(cacheKey, serviceCache.getKey());
    Assert.assertEquals(MicroserviceCacheStatus.REFRESHED, serviceCache.getStatus());
    Assert.assertEquals(1, serviceRegistryCache.microserviceCache.size());
    Assert.assertSame(serviceCache, serviceRegistryCache.microserviceCache.get(cacheKey));
  }

  @Test
  public void findServiceCache_client_error() {
    mockServiceRegistryHolder().value = MicroserviceCacheStatus.CLIENT_ERROR;

    MicroserviceCacheKey cacheKey = MicroserviceCacheKey.builder().serviceName("svc").appId("app").env("env").build();
    MicroserviceCache serviceCache = serviceRegistryCache.findServiceCache(cacheKey);

    Assert.assertSame(cacheKey, serviceCache.getKey());
    Assert.assertEquals(MicroserviceCacheStatus.CLIENT_ERROR, serviceCache.getStatus());
    Assert.assertEquals(1, serviceRegistryCache.microserviceCache.size());
    Assert.assertSame(serviceCache, serviceRegistryCache.microserviceCache.get(cacheKey));
  }

  @Test
  public void findServiceCache_service_not_found() {
    mockServiceRegistryHolder().value = MicroserviceCacheStatus.SERVICE_NOT_FOUND;

    MicroserviceCacheKey cacheKey = MicroserviceCacheKey.builder().serviceName("svc").appId("app").env("env").build();
    MicroserviceCache serviceCache = serviceRegistryCache.findServiceCache(cacheKey);

    Assert.assertSame(cacheKey, serviceCache.getKey());
    Assert.assertEquals(MicroserviceCacheStatus.SERVICE_NOT_FOUND, serviceCache.getStatus());
    Assert.assertTrue(serviceRegistryCache.microserviceCache.isEmpty());
  }

  private Holder<MicroserviceCacheStatus> mockServiceRegistryHolder() {
    Holder<MicroserviceCacheStatus> statusHolder = new Holder<>();
    serviceRegistryCache = new RefreshableServiceRegistryCache(consumerService, null) {
      @Override
      RefreshableMicroserviceCache createMicroserviceCache(MicroserviceCacheKey microserviceCacheKey) {
        return new RefreshableMicroserviceCache(
            consumerService,
            microserviceCacheKey,
            null, false) {
          @Override
          public void refresh() {
            this.status = statusHolder.value;
          }
        };
      }
    };
    return statusHolder;
  }
}