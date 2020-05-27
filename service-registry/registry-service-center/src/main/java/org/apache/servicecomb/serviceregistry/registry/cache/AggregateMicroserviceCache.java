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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;

public class AggregateMicroserviceCache implements MicroserviceCache {
  private MicroserviceCacheKey key;

  Map<String, MicroserviceCache> caches;

  AtomicLong revisionCounter = new AtomicLong();

  private String revisionId = revisionCounter.toString();

  private MicroserviceCacheStatus status = MicroserviceCacheStatus.INIT;

  private List<MicroserviceInstance> instances = new ArrayList<>();

  Collection<ServiceRegistry> serviceRegistries;

  private final Object refreshLock = new Object();

  public AggregateMicroserviceCache(MicroserviceCacheKey key, Collection<ServiceRegistry> serviceRegistries) {
    this.key = key;
    this.serviceRegistries = serviceRegistries;

    refresh();
  }

  @Override
  public void refresh() {
    refreshInnerState(false);
  }

  private void refreshInnerState(boolean b) {
    synchronized (refreshLock) {
      fillInMicroserviceCaches(b);
      fillInInstanceList();
      updateRevisionId();
      refreshStatus();
    }
  }

  @Override
  public void forceRefresh() {
    refreshInnerState(true);
  }

  private void fillInMicroserviceCaches(boolean isForce) {
    HashMap<String, MicroserviceCache> cacheMap = new LinkedHashMap<>();
    for (ServiceRegistry serviceRegistry : serviceRegistries) {
      MicroserviceCache microserviceCache = serviceRegistry.findMicroserviceCache(key);
      if (!isValidMicroserviceCache(microserviceCache)) {
        continue;
      }
      if (isForce) {
        microserviceCache.forceRefresh();
      }
      cacheMap.put(serviceRegistry.getName(), microserviceCache);
    }
    caches = cacheMap;
  }

  private void fillInInstanceList() {
    ArrayList<MicroserviceInstance> instances = new ArrayList<>();
    for (Entry<String, MicroserviceCache> stringMicroserviceCacheEntry : caches.entrySet()) {
      instances.addAll(stringMicroserviceCacheEntry.getValue().getInstances());
    }
    this.instances = Collections.unmodifiableList(instances);
  }

  private void updateRevisionId() {
    revisionCounter.incrementAndGet();
    revisionId = revisionCounter.toString();
  }

  private void refreshStatus() {
    if (caches.size() == 0) {
      status = MicroserviceCacheStatus.SERVICE_NOT_FOUND;
    } else {
      status = MicroserviceCacheStatus.REFRESHED;
    }
  }

  private boolean isValidMicroserviceCache(MicroserviceCache microserviceCache) {
    return !(
        Objects.isNull(microserviceCache)
            || MicroserviceCacheStatus.SERVICE_NOT_FOUND.equals(microserviceCache.getStatus())
    );
  }

  @Override
  public MicroserviceCacheKey getKey() {
    return key;
  }

  @Override
  public List<MicroserviceInstance> getInstances() {
    return instances;
  }

  @Override
  public String getRevisionId() {
    return revisionId;
  }

  @Override
  public MicroserviceCacheStatus getStatus() {
    return status;
  }
}
