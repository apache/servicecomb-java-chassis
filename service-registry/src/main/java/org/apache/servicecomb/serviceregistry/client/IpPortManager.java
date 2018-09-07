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

package org.apache.servicecomb.serviceregistry.client;

import static org.apache.servicecomb.serviceregistry.api.Const.REGISTRY_APP_ID;
import static org.apache.servicecomb.serviceregistry.api.Const.REGISTRY_SERVICE_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.serviceregistry.cache.CacheEndpoint;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpPortManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(IpPortManager.class);

  private ServiceRegistryConfig serviceRegistryConfig;

  private InstanceCacheManager instanceCacheManager;

  private String defaultTransport = "rest";

  private ArrayList<IpPort> defaultIpPort;

  private Object lockObj = new Object();

   // 缓存CacheEndpoint
  private volatile Map<String, List<CacheEndpoint>> transportMap;

  private AtomicInteger currentAvailableIndex;

  private boolean autoDiscoveryInited = false;

  private int maxRetryTimes;

  public int getMaxRetryTimes() {
    return maxRetryTimes;
  }

  public IpPortManager(ServiceRegistryConfig serviceRegistryConfig, InstanceCacheManager instanceCacheManager) {
    this.serviceRegistryConfig = serviceRegistryConfig;

    defaultTransport = serviceRegistryConfig.getTransport();
    defaultIpPort = serviceRegistryConfig.getIpPort();
    if (defaultIpPort.size() == 0) {
      throw new IllegalArgumentException("Service center address is required to start the application.");
    }
    int initialIndex = new Random().nextInt(defaultIpPort.size());
    currentAvailableIndex = new AtomicInteger(initialIndex);
    maxRetryTimes = defaultIpPort.size();
  }

  // we have to do this operation after the first time setup has already done
  public void initAutoDiscovery() {
    if (!autoDiscoveryInited && this.serviceRegistryConfig.isRegistryAutoDiscovery()) {
      instanceCacheManager.getOrCreateVersionedCache(REGISTRY_APP_ID,
          REGISTRY_SERVICE_NAME,
          DefinitionConst.VERSION_RULE_LATEST);
      autoDiscoveryInited = true;
    }
  }

  public IpPort getNextAvailableAddress(IpPort failedIpPort) {
    int currentIndex = currentAvailableIndex.get();
    IpPort current = getAvailableAddress(currentIndex);
    if (current.equals(failedIpPort)) {
      currentAvailableIndex.compareAndSet(currentIndex, currentIndex + 1);
      current = getAvailableAddress();
    }

    LOGGER.info("Change service center address from {} to {}", failedIpPort.toString(), current.toString());
    return current;
  }

  public IpPort getAvailableAddress() {
    return getAvailableAddress(currentAvailableIndex.get());
  }

  private IpPort getAvailableAddress(int index) {
    if (index < defaultIpPort.size()) {
      return defaultIpPort.get(index);
    }

    List<CacheEndpoint> endpoints = getDiscoveredIpPort();
    if (endpoints == null || (index >= defaultIpPort.size() + endpoints.size())) {
      currentAvailableIndex.set(0);
      return defaultIpPort.get(0);
    }
    maxRetryTimes = defaultIpPort.size() + endpoints.size();
    CacheEndpoint nextEndpoint = endpoints.get(index - defaultIpPort.size());
    return new URIEndpointObject(nextEndpoint.getEndpoint());
  }

  private List<CacheEndpoint> getDiscoveredIpPort() {
    if (!autoDiscoveryInited || !this.serviceRegistryConfig.isRegistryAutoDiscovery()) {
      return null;
    }

    VersionedCache versionedCache = instanceCacheManager.getOrCreateVersionedCache(REGISTRY_APP_ID,
        REGISTRY_SERVICE_NAME,
        DefinitionConst.VERSION_RULE_LATEST);
    return getOrCreateTransportMap(versionedCache).get(defaultTransport);
  }

  public Map<String, List<CacheEndpoint>> getOrCreateTransportMap(VersionedCache versionedCache) {
    if (transportMap == null) {
      synchronized (lockObj) {
        if (transportMap == null) {
          transportMap = createTransportMap(versionedCache);
        }
      }
    }
    return transportMap;
  }

  private Map<String, List<CacheEndpoint>> createTransportMap(VersionedCache versionedCache) {
    Map<String, List<CacheEndpoint>> transportMap = new HashMap<>();
    Map<String, MicroserviceInstance> instances = versionedCache.data();

    for (MicroserviceInstance instance : instances.values()) {
      // 过滤到不可用实例
      if (instance.getStatus() != MicroserviceInstanceStatus.UP) {
        continue;
      }
      for (String endpoint : instance.getEndpoints()) {
        try {
          URI uri = URI.create(endpoint);
          String transportName = uri.getScheme();
           List<CacheEndpoint> cacheEndpointList = transportMap.computeIfAbsent(transportName, k -> new ArrayList<>());
          cacheEndpointList.add(new CacheEndpoint(endpoint, instance));
        } catch (Exception e) {
          LOGGER.warn("unrecognized address find, ignore " + endpoint);
        }
      }
    }
    return transportMap;
  }
}
