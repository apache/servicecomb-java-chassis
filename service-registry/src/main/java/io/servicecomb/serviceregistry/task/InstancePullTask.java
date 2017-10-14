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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.serviceregistry.cache.InstanceCache;
import io.servicecomb.serviceregistry.cache.InstanceCacheManager;

public class InstancePullTask implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstancePullTask.class);

  private ServiceCenterTaskMonitor serviceCenterTaskMonitor = new ServiceCenterTaskMonitor();
  private InstanceCacheManager cacheManager;
  private int interval;

  public InstancePullTask(int interval, InstanceCacheManager cacheManager) {
    this.interval = interval;
    this.cacheManager = cacheManager;
  }

  @Override
  public void run() {
    try {
      serviceCenterTaskMonitor.beginCycle(interval);
      for (InstanceCache cache : this.cacheManager.getCachedEntries()) {
        InstanceCache newCache = cacheManager.createInstanceCache(cache.getAppId(),
            cache.getMicroserviceName(),
            cache.getMicroserviceVersionRule());
        if (newCache != null) {
          cacheManager.updateInstanceMap(cache.getAppId(), cache.getMicroserviceName(), newCache);
        }
      }
      serviceCenterTaskMonitor.endCycle();
    } catch (Throwable e) {
      // do a protection of long run task.
      LOGGER.error("unexpected exception in instance pull task.", e);
    }
  }

}
