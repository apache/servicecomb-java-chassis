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
package io.servicecomb.serviceregistry.task;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import io.servicecomb.serviceregistry.cache.InstanceCache;
import io.servicecomb.serviceregistry.cache.InstanceCacheManager;
import mockit.Expectations;
import mockit.Injectable;

public class TestInstancePullTask {
  @Test
  public void testCacheChange(@Injectable InstanceCacheManager cacheManager) {
    InstancePullTask task = new InstancePullTask(2, cacheManager);
    InstanceCache serviceCenter = new InstanceCache("sc", "sc", "0.0.1", null);
    InstanceCache otherService = new InstanceCache("other", "other", "0.0.1", null);
    Collection<InstanceCache> caches = new ArrayList<>();
    caches.add(serviceCenter);
    caches.add(otherService);

    InstanceCache changedServiceCenter = new InstanceCache("sc", "sc", "0.0.1", null);
    InstanceCache changedService = new InstanceCache("other", "other", "0.0.1", null);

    new Expectations() {
      {
        cacheManager.getCachedEntries();
        result = caches;
        cacheManager.createInstanceCache("sc", "sc", "0.0.1");
        result = changedServiceCenter;
        cacheManager.createInstanceCache("other", "other", "0.0.1");
        result = changedService;
        cacheManager.updateInstanceMap("sc", "sc", changedServiceCenter);
        cacheManager.updateInstanceMap("other", "other", changedService);
      }
    };

    task.run();
    // assertions done in expectations
  }

  @Test
  public void testCacheNotChange(@Injectable InstanceCacheManager cacheManager) {
    InstancePullTask task = new InstancePullTask(2, cacheManager);
    InstanceCache serviceCenter = new InstanceCache("sc", "sc", "0.0.1", null);
    InstanceCache otherService = new InstanceCache("other", "other", "0.0.1", null);
    Collection<InstanceCache> caches = new ArrayList<>();
    caches.add(serviceCenter);
    caches.add(otherService);

    new Expectations() {
      {
        cacheManager.getCachedEntries();
        result = caches;
        cacheManager.createInstanceCache("sc", "sc", "0.0.1");
        result = serviceCenter;
        cacheManager.createInstanceCache("other", "other", "0.0.1");
        result = otherService;
      }
    };

    task.run();
    // assertions done in expectations
  }


  @Test
  public void testUnexpectedException(@Injectable InstanceCacheManager cacheManager) {
    InstancePullTask task = new InstancePullTask(2, cacheManager);
    InstanceCache serviceCenter = new InstanceCache("sc", "sc", "0.0.1", null);
    InstanceCache otherService = new InstanceCache("other", "other", "0.0.1", null);
    Collection<InstanceCache> caches = new ArrayList<>();
    caches.add(serviceCenter);
    caches.add(otherService);

    new Expectations() {
      {
        cacheManager.getCachedEntries();
        result = caches;
        cacheManager.createInstanceCache("sc", "sc", "0.0.1");
        result = new java.lang.Error();
      }
    };

    task.run();
    // will not throw exception in this case
  }
}
