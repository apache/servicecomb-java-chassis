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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache.MicroserviceCacheStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class AggregateServiceRegistryCache implements ServiceRegistryCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(AggregateServiceRegistryCache.class);

  Collection<ServiceRegistry> serviceRegistries;

  final Map<MicroserviceCacheKey, AggregateMicroserviceCache> microserviceCache = new ConcurrentHashMapEx<>();

  private Consumer<List<MicroserviceCache>> cacheRefreshedWatcher;

  public AggregateServiceRegistryCache(Collection<ServiceRegistry> serviceRegistries) {
    this.serviceRegistries = serviceRegistries;
  }

  @Override
  public MicroserviceCache findServiceCache(MicroserviceCacheKey microserviceCacheKey) {
    AggregateMicroserviceCache microserviceCache = this.microserviceCache.computeIfAbsent(microserviceCacheKey,
        key -> new AggregateMicroserviceCache(key, serviceRegistries));
    removeMicroserviceCacheIfNotExist(microserviceCache);
    return microserviceCache;
  }

  @Override
  public ServiceRegistryCache setCacheRefreshedWatcher(Consumer<List<MicroserviceCache>> cacheRefreshedWatcher) {
    this.cacheRefreshedWatcher = cacheRefreshedWatcher;
    return this;
  }

  @Subscribe
  public void onMicroserviceCacheRefreshed(MicroserviceCacheRefreshedEvent event) {
    List<MicroserviceCache> microserviceCaches = event.getMicroserviceCaches();
    if (null == microserviceCaches || microserviceCaches.isEmpty()) {
      return;
    }

    List<MicroserviceCache> refreshedAggregateMicroserviceCaches = microserviceCaches.stream()
        .map(cache -> this.microserviceCache.get(cache.getKey()))
        .filter(Objects::nonNull)
        .peek(AggregateMicroserviceCache::refresh)
        .peek(this::removeMicroserviceCacheIfNotExist)
        .collect(Collectors.toList());

    LOGGER.info("[{}] caches get refreshed", refreshedAggregateMicroserviceCaches.size());
    refreshedAggregateMicroserviceCaches.forEach(cache -> {
      LOGGER.info("[{}]: status={}, revisionId={}", cache.getKey(), cache.getStatus(), cache.getRevisionId());
    });

    if (null != cacheRefreshedWatcher) {
      cacheRefreshedWatcher.accept(refreshedAggregateMicroserviceCaches);
    }
  }

  private void removeMicroserviceCacheIfNotExist(MicroserviceCache cache) {
    if (MicroserviceCacheStatus.SERVICE_NOT_FOUND.equals(cache.getStatus())) {
      microserviceCache.remove(cache.getKey());
      LOGGER.info("microserviceCache[{}] got removed", cache.getKey());
    }
  }

  @Override
  public Map<MicroserviceCacheKey, MicroserviceCache> getMicroserviceCaches() {
    return Collections.unmodifiableMap(microserviceCache);
  }
}
