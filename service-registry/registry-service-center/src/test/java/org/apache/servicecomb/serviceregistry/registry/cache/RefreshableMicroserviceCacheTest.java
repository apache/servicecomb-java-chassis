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
import java.util.Set;
import java.util.function.Function;

import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.registry.api.registry.FindInstancesResponse;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.registry.consumer.MicroserviceInstancePing;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache.MicroserviceCacheStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mockit.Mock;
import mockit.MockUp;

public class RefreshableMicroserviceCacheTest {

  private Holder<Function<Object[], MicroserviceInstances>> findServiceInstancesOprHolder = new Holder<>();

  private ServiceRegistryClient srClient;

  private RefreshableMicroserviceCache microserviceCache;

  private List<MicroserviceInstance> pulledInstances = new ArrayList<>();

  private Microservice consumerService;

  @Before
  public void setUp() throws Exception {
    srClient = new MockUp<ServiceRegistryClient>() {
      @Mock
      MicroserviceInstances findServiceInstances(String consumerId, String appId, String serviceName,
          String versionRule, String revision) {
        return findServiceInstancesOprHolder.value
            .apply(new Object[] {consumerId, appId, serviceName, versionRule, revision});
      }
    }.getMockInstance();
    consumerService = new Microservice();
    consumerService.setServiceId("consumerId");
    microserviceCache = new RefreshableMicroserviceCache(
        consumerService,
        MicroserviceCacheKey.builder().env("env").appId("app").serviceName("svc").build(),
        srClient,
        false);

    findServiceInstancesOprHolder.value = params -> {
      MicroserviceInstances microserviceInstances = new MicroserviceInstances();
      microserviceInstances.setNeedRefresh(true);
      microserviceInstances.setRevision("rev0");
      microserviceInstances.setMicroserviceNotExist(false);

      FindInstancesResponse instancesResponse = new FindInstancesResponse();
      instancesResponse.setInstances(pulledInstances);
      microserviceInstances.setInstancesResponse(instancesResponse);

      return microserviceInstances;
    };
  }

  @Test
  public void forceRefresh() {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setInstanceId("instanceId00");
    ArrayList<MicroserviceInstance> instances = new ArrayList<>();
    instances.add(microserviceInstance);
    findServiceInstancesOprHolder.value = params -> {
      Assert.assertEquals("consumerId", params[0]);
      Assert.assertEquals("app", params[1]);
      Assert.assertEquals("svc", params[2]);
      Assert.assertEquals("0.0.0.0+", params[3]);
      Assert.assertNull(params[4]);
      MicroserviceInstances microserviceInstances = new MicroserviceInstances();
      microserviceInstances.setNeedRefresh(true);
      microserviceInstances.setRevision("rev2");
      microserviceInstances.setMicroserviceNotExist(false);

      FindInstancesResponse instancesResponse = new FindInstancesResponse();
      instancesResponse.setInstances(instances);

      microserviceInstances.setInstancesResponse(instancesResponse);
      return microserviceInstances;
    };

    microserviceCache.revisionId = "rev";
    microserviceCache.forceRefresh();

    Assert.assertEquals(MicroserviceCacheStatus.REFRESHED, microserviceCache.getStatus());
    List<MicroserviceInstance> cachedInstances = microserviceCache.getInstances();
    Assert.assertEquals(1, cachedInstances.size());
    MicroserviceInstance instance = cachedInstances.iterator().next();
    Assert.assertEquals("instanceId00", instance.getInstanceId());
    Assert.assertEquals("rev2", microserviceCache.getRevisionId());
  }

  @Test
  public void refresh() {
    ArrayList<MicroserviceInstance> instances = new ArrayList<>();
    findServiceInstancesOprHolder.value = params -> {
      Assert.assertEquals("consumerId", params[0]);
      Assert.assertEquals("app", params[1]);
      Assert.assertEquals("svc", params[2]);
      Assert.assertEquals("0.0.0.0+", params[3]);
      Assert.assertNull(params[4]);
      MicroserviceInstances microserviceInstances = new MicroserviceInstances();
      microserviceInstances.setNeedRefresh(true);
      microserviceInstances.setRevision("rev0");
      microserviceInstances.setMicroserviceNotExist(false);

      FindInstancesResponse instancesResponse = new FindInstancesResponse();
      instancesResponse.setInstances(instances);

      microserviceInstances.setInstancesResponse(instancesResponse);
      return microserviceInstances;
    };

    // at the beginning, no instances in cache
    List<MicroserviceInstance> cachedInstances = microserviceCache.getInstances();
    Assert.assertEquals(0, cachedInstances.size());
    Assert.assertNull(microserviceCache.getRevisionId());

    // find 1 instance from sc
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    instances.add(microserviceInstance);
    microserviceInstance.setInstanceId("instanceId00");

    microserviceCache.refresh();
    Assert.assertEquals(MicroserviceCacheStatus.REFRESHED, microserviceCache.getStatus());

    cachedInstances = microserviceCache.getInstances();
    Assert.assertEquals(1, cachedInstances.size());
    MicroserviceInstance instance = cachedInstances.iterator().next();
    Assert.assertEquals("instanceId00", instance.getInstanceId());
    Assert.assertEquals("rev0", microserviceCache.getRevisionId());

    // 2nd time, find 2 instances, one of them is the old instance
    MicroserviceInstance microserviceInstance1 = new MicroserviceInstance();
    instances.add(microserviceInstance1);
    microserviceInstance1.setInstanceId("instanceId01");

    findServiceInstancesOprHolder.value = params -> {
      Assert.assertEquals("consumerId", params[0]);
      Assert.assertEquals("app", params[1]);
      Assert.assertEquals("svc", params[2]);
      Assert.assertEquals("0.0.0.0+", params[3]);
      Assert.assertEquals("rev0", params[4]);
      MicroserviceInstances microserviceInstances = new MicroserviceInstances();
      microserviceInstances.setNeedRefresh(true);
      microserviceInstances.setRevision("rev1");
      microserviceInstances.setMicroserviceNotExist(false);

      FindInstancesResponse instancesResponse = new FindInstancesResponse();
      instancesResponse.setInstances(instances);

      microserviceInstances.setInstancesResponse(instancesResponse);
      return microserviceInstances;
    };

    microserviceCache.refresh();
    Assert.assertEquals(MicroserviceCacheStatus.REFRESHED, microserviceCache.getStatus());
    cachedInstances = microserviceCache.getInstances();
    Assert.assertEquals(2, cachedInstances.size());
    Assert.assertEquals("instanceId00", cachedInstances.get(0).getInstanceId());
    Assert.assertEquals("instanceId01", cachedInstances.get(1).getInstanceId());
  }

  @Test
  public void refresh_service_error() {
    findServiceInstancesOprHolder.value = params -> null;

    List<MicroserviceInstance> oldInstanceList = microserviceCache.getInstances();

    microserviceCache.refresh();
    Assert.assertEquals(MicroserviceCacheStatus.CLIENT_ERROR, microserviceCache.getStatus());
    Assert.assertSame(oldInstanceList, microserviceCache.getInstances());
  }

  @Test
  public void refresh_service_not_exist() {
    findServiceInstancesOprHolder.value = params -> {
      MicroserviceInstances microserviceInstances = new MicroserviceInstances();
      microserviceInstances.setMicroserviceNotExist(true);
      return microserviceInstances;
    };

    List<MicroserviceInstance> oldInstanceList = microserviceCache.getInstances();

    microserviceCache.refresh();
    Assert.assertEquals(MicroserviceCacheStatus.SERVICE_NOT_FOUND, microserviceCache.getStatus());
    Assert.assertSame(oldInstanceList, microserviceCache.getInstances());
  }

  @Test
  public void refresh_service_no_change() {
    findServiceInstancesOprHolder.value = params -> {
      MicroserviceInstances microserviceInstances = new MicroserviceInstances();
      microserviceInstances.setMicroserviceNotExist(false);
      microserviceInstances.setNeedRefresh(false);
      return microserviceInstances;
    };

    List<MicroserviceInstance> oldInstanceList = microserviceCache.getInstances();

    microserviceCache.refresh();
    Assert.assertEquals(MicroserviceCacheStatus.NO_CHANGE, microserviceCache.getStatus());
    Assert.assertSame(oldInstanceList, microserviceCache.getInstances());
  }

  @Test
  public void refresh_error_in_setInstances() {
    microserviceCache = new RefreshableMicroserviceCache(
        consumerService,
        MicroserviceCacheKey.builder().env("env").appId("app").serviceName("svc").build(),
        srClient,
        false) {
      @Override
      protected Set<MicroserviceInstance> mergeInstances(List<MicroserviceInstance> pulledInstances) {
        throw new IllegalStateException("a mock exception");
      }
    };

    List<MicroserviceInstance> oldInstanceList = microserviceCache.getInstances();
    Assert.assertEquals(MicroserviceCacheStatus.INIT, microserviceCache.getStatus());

    microserviceCache.refresh();

    Assert.assertEquals(MicroserviceCacheStatus.SETTING_CACHE_ERROR, microserviceCache.getStatus());
    List<MicroserviceInstance> newInstanceList = microserviceCache.getInstances();
    Assert.assertEquals(0, newInstanceList.size());
    Assert.assertSame(oldInstanceList, newInstanceList);
  }

  @Test
  public void refresh_empty_instance_protection_disabled() {
    microserviceCache.instances = new ArrayList<>();
    MicroserviceInstance instance0 = new MicroserviceInstance();
    instance0.setInstanceId("instanceId0");
    microserviceCache.instances.add(instance0);

    pulledInstances = new ArrayList<>();
    microserviceCache.refresh();

    Assert.assertEquals(MicroserviceCacheStatus.REFRESHED, microserviceCache.getStatus());
    Assert.assertEquals(0, microserviceCache.getInstances().size());
  }

  @Test
  public void refresh_empty_instance_protection_enabled() {
    microserviceCache.setEmptyInstanceProtectionEnabled(true);
    microserviceCache.instancePing = new MicroserviceInstancePing() {
      @Override
      public int getOrder() {
        return 0;
      }

      @Override
      public boolean ping(MicroserviceInstance instance) {
        return true;
      }
    };
    microserviceCache.instances = new ArrayList<>();
    MicroserviceInstance instance0 = new MicroserviceInstance();
    instance0.setInstanceId("instanceId0");
    microserviceCache.instances.add(instance0);

    pulledInstances = new ArrayList<>();
    microserviceCache.refresh();

    Assert.assertEquals(MicroserviceCacheStatus.REFRESHED, microserviceCache.getStatus());
    Assert.assertEquals(1, microserviceCache.getInstances().size());
    Assert.assertEquals("instanceId0", microserviceCache.getInstances().get(0).getInstanceId());
  }

  @Test
  public void set_consumer_service_id() {
    Holder<Integer> assertCounter = new Holder<>(0);
    Function<Object[], MicroserviceInstances> preservedLogic = findServiceInstancesOprHolder.value;
    findServiceInstancesOprHolder.value = params -> {
      Assert.assertEquals("consumerId", params[0]);
      assertCounter.value++;
      return preservedLogic.apply(params);
    };
    microserviceCache.refresh();

    consumerService.setServiceId("consumerId2");

    findServiceInstancesOprHolder.value = params -> {
      Assert.assertEquals("consumerId2", params[0]);
      assertCounter.value++;
      return preservedLogic.apply(params);
    };
    microserviceCache.refresh();
    Assert.assertEquals(Integer.valueOf(2), assertCounter.value);
  }
}