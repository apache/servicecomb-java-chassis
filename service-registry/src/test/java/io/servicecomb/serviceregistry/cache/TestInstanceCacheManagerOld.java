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

package io.servicecomb.serviceregistry.cache;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

import io.servicecomb.foundation.common.cache.VersionedCache;
import io.servicecomb.serviceregistry.ServiceRegistry;
import io.servicecomb.serviceregistry.api.MicroserviceKey;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;
import io.servicecomb.serviceregistry.api.registry.WatchAction;
import io.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import io.servicecomb.serviceregistry.task.event.ExceptionEvent;
import io.servicecomb.serviceregistry.task.event.RecoveryEvent;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestInstanceCacheManagerOld {

  @Test
  public void testInstanceUpdate() {
    ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
    Microservice microservice = serviceRegistry.getMicroservice();
    serviceRegistry.init();
    InstanceCacheManagerOld oInstanceCacheManager =
        (InstanceCacheManagerOld) serviceRegistry.getInstanceCacheManager();
    Map<String, MicroserviceInstance> instanceMap = new HashMap<>();
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setInstanceId("88887777");
    instanceMap.put(instance.getInstanceId(), instance);
    oInstanceCacheManager
        .updateInstanceMap("default", "default", new InstanceCache("default", "default", "latest", instanceMap));

    MicroserviceInstanceChangedEvent oChangedEvent = new MicroserviceInstanceChangedEvent();
    oChangedEvent.setAction(WatchAction.UPDATE);
    MicroserviceKey oKey = new MicroserviceKey();
    oKey.setAppId(microservice.getAppId());
    oKey.setVersion(microservice.getVersion());
    oKey.setServiceName(microservice.getServiceName());
    oChangedEvent.setKey(oKey);
    oChangedEvent.setInstance(instance);
    Assert.assertEquals(1, oInstanceCacheManager.cacheMap.get("default/default").getInstanceMap().size());

    oInstanceCacheManager.onInstanceUpdate(oChangedEvent);
    oChangedEvent.setAction(WatchAction.DELETE);
    oInstanceCacheManager.onInstanceUpdate(oChangedEvent);
    Assert.assertEquals(0, oInstanceCacheManager.cacheMap.get("default/default").getInstanceMap().size());

    oChangedEvent.setAction(WatchAction.CREATE);
    oInstanceCacheManager.onInstanceUpdate(oChangedEvent);
    Assert.assertEquals(1, oInstanceCacheManager.cacheMap.get("default/default").getInstanceMap().size());
    Assert.assertEquals(1, oInstanceCacheManager.getCachedEntries().size());

    oChangedEvent.getInstance().setStatus(MicroserviceInstanceStatus.DOWN);
    oChangedEvent.setAction(WatchAction.UPDATE);
    oInstanceCacheManager.onInstanceUpdate(oChangedEvent);
    Assert.assertEquals(0, oInstanceCacheManager.cacheMap.get("default/default").getInstanceMap().size());

    oChangedEvent.getInstance().setStatus(MicroserviceInstanceStatus.UP);
    oChangedEvent.setAction(WatchAction.CREATE);
    oInstanceCacheManager.onInstanceUpdate(oChangedEvent);
    Assert.assertEquals(1, oInstanceCacheManager.cacheMap.get("default/default").getInstanceMap().size());
    Assert.assertEquals(1, oInstanceCacheManager.getCachedEntries().size());

    Assert.assertEquals("UP", microservice.getInstance().getStatus().toString());
    oChangedEvent.setAction(WatchAction.EXPIRE);
    oInstanceCacheManager.onInstanceUpdate(oChangedEvent);
    Assert.assertEquals(oInstanceCacheManager.cacheMap.size(), 0);

    InstanceCache newServiceCache = oInstanceCacheManager.getOrCreate("default", "newService", "1.0.1");
    Assert.assertNotNull(newServiceCache);
    Assert.assertEquals(1, oInstanceCacheManager.getCachedEntries().size());

    oInstanceCacheManager.cacheMap.clear();
    new MockUp<InstanceCacheManagerOld>(oInstanceCacheManager) {
      @Mock
      InstanceCache createInstanceCache(String appId, String microserviceName, String microserviceVersionRule) {
        return newServiceCache;
      }
    };
    VersionedCache versionedCache = oInstanceCacheManager.getOrCreateVersionedCache("default", "newService", "1.0.1");
    Assert.assertEquals("1.0.1", versionedCache.name());
    Assert.assertTrue(versionedCache.isEmpty());
  }

  @Test
  public void testCacheAvailable(@Mocked ServiceRegistry serviceRegistry,
      @Mocked ServiceRegistryConfig serviceRegistryConfig) {
    EventBus eventBus = new EventBus();
    InstanceCacheManagerOld instanceCacheManager =
        new InstanceCacheManagerOld(eventBus, serviceRegistry, serviceRegistryConfig);

    Assert.assertEquals(false, instanceCacheManager.cacheAvailable);
    eventBus.post(new RecoveryEvent());
    Assert.assertEquals(true, instanceCacheManager.cacheAvailable);

    eventBus.post(new ExceptionEvent(null));
    Assert.assertEquals(false, instanceCacheManager.cacheAvailable);
  }
}
