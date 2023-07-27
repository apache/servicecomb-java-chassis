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

package org.apache.servicecomb.registry;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.registry.api.Discovery;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.api.LifeCycle;
import org.springframework.util.CollectionUtils;

public class DiscoveryManager implements LifeCycle {
  // TODO: 1. instance init and notification; 2. ping status; 3. isolation status.
  private final List<Discovery<? extends DiscoveryInstance>> discoveryList;

  private List<? extends DiscoveryInstance> highestInstances;

  private List<? extends DiscoveryInstance> normalInstances;

  private List<? extends DiscoveryInstance> lowestInstances;

  private Map<String, Map<String, VersionedCache>> versionedCache = new ConcurrentHashMapEx<>();

  public DiscoveryManager(List<Discovery<? extends DiscoveryInstance>> discoveryList) {
    this.discoveryList = discoveryList;
  }

  public VersionedCache getOrCreateVersionedCache(String application, String serviceName) {
    return versionedCache.computeIfAbsent(application, key ->
            new ConcurrentHashMapEx<>())
        .computeIfAbsent(serviceName, key -> {
          if (!CollectionUtils.isEmpty(highestInstances)) {
            return new VersionedCache()
                .name(key)
                .autoCacheVersion()
                .data(highestInstances);
          }
          if (!CollectionUtils.isEmpty(normalInstances)) {
            return new VersionedCache()
                .name(key)
                .autoCacheVersion()
                .data(normalInstances);
          }
          if (!CollectionUtils.isEmpty(lowestInstances)) {
            return new VersionedCache()
                .name(key)
                .autoCacheVersion()
                .data(lowestInstances);
          }
          return new VersionedCache()
              .name(key)
              .autoCacheVersion()
              .data(Collections.emptyList());
        });
  }

  public List<? extends DiscoveryInstance> findServiceInstances(String application, String serviceName) {
    List<? extends DiscoveryInstance> result;
    for (Discovery<? extends DiscoveryInstance> discovery : discoveryList) {
      result = discovery.findServiceInstances(application, serviceName);
      if (CollectionUtils.isEmpty(result)) {
        continue;
      }
      return result;
    }
    return Collections.emptyList();
  }

  public CompletableFuture<List<? extends DiscoveryInstance>> findServiceInstancesAsync(String application,
      String serviceName) {
    // TODO: async implementation
    return null;
  }

  @Override
  public void destroy() {
    discoveryList.forEach(LifeCycle::destroy);
  }

  @Override
  public void run() {
    discoveryList.forEach(LifeCycle::run);
  }

  @Override
  public void init() {
    discoveryList.forEach(LifeCycle::init);
  }
}
