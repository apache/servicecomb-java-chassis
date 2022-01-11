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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.cache.CacheEndpoint;
import org.apache.servicecomb.registry.cache.InstanceCache;
import org.apache.servicecomb.registry.cache.InstanceCacheManager;
import org.apache.servicecomb.registry.cache.InstanceCacheManagerNew;
import org.apache.servicecomb.registry.consumer.AppManager;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.event.ServiceCenterEventBus;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCacheRefreshedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.Subscribe;

public class IpPortManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(IpPortManager.class);

  private static final String SC_KEY = "SERVICECENTER@default@@0.0.0.0+";

  private ServiceRegistryConfig serviceRegistryConfig;

  InstanceCacheManager instanceCacheManager;

  private String defaultTransport = "rest";

  private ArrayList<IpPort> defaultIpPort;

  private AtomicInteger currentAvailableIndex;

  private boolean autoDiscoveryInited = true;

  private int maxRetryTimes;

  private final AtomicInteger index = new AtomicInteger();

  private Object lock = new Object();

  private volatile List<String> sameAZ = new ArrayList<>();

  private volatile List<String> sameRegion = new ArrayList<>();

  public static final Cache<String, Boolean> availableIpCache = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .expireAfterAccess(1, TimeUnit.MINUTES)
      .build();

  public void setAutoDiscoveryInited(boolean autoDiscoveryInited) {
    this.autoDiscoveryInited = autoDiscoveryInited;
  }

  public int getMaxRetryTimes() {
    return maxRetryTimes;
  }

  public IpPortManager(ServiceRegistryConfig serviceRegistryConfig) {
    this.serviceRegistryConfig = serviceRegistryConfig;
    this.instanceCacheManager = new InstanceCacheManagerNew(new AppManager());

    defaultTransport = serviceRegistryConfig.getTransport();
    defaultIpPort = serviceRegistryConfig.getIpPort();
    if (defaultIpPort.isEmpty()) {
      throw new IllegalArgumentException("Service center address is required to start the application.");
    }
    int initialIndex = new Random().nextInt(defaultIpPort.size());
    currentAvailableIndex = new AtomicInteger(initialIndex);
    LOGGER.info("Initial service center address is {}", getAvailableAddress());
    maxRetryTimes = defaultIpPort.size();
    ServiceCenterEventBus.getEventBus().register(this);
  }

  // we have to do this operation after the first time setup has already done
  public void initAutoDiscovery() {
    if (!autoDiscoveryInited && this.serviceRegistryConfig.isRegistryAutoDiscovery()) {
      InstanceCache cache = instanceCacheManager.getOrCreate(REGISTRY_APP_ID,
          REGISTRY_SERVICE_NAME,
          DefinitionConst.VERSION_RULE_LATEST);

      InstanceCache caches = instanceCacheManager.getOrCreate(REGISTRY_APP_ID,
          REGISTRY_SERVICE_NAME,
          DefinitionConst.VERSION_RULE_LATEST);
      if (cache.getInstanceMap().size() <= 0) {
        setAutoDiscoveryInited(false);
        return;
      }
      initIpPort();
      setAutoDiscoveryInited(true);
    }
  }

  @Subscribe
  public void onMicroserviceCacheRefreshed(MicroserviceCacheRefreshedEvent event) {
    List<MicroserviceCache> microserviceCaches = event.getMicroserviceCaches();
    if (null == microserviceCaches || microserviceCaches.isEmpty()) {
      return;
    }
    MicroserviceInstance myself = RegistrationManager.INSTANCE.getMicroserviceInstance();
    for (MicroserviceCache microserviceCache : microserviceCaches) {
      if (microserviceCache.getKey().toString().equals(SC_KEY)) {
        refreshEndPoint(myself, microserviceCache);
      }
    }
  }

  private void refreshEndPoint(MicroserviceInstance myself, MicroserviceCache microserviceCache) {
    List<MicroserviceInstance> microserviceCacheInstances = microserviceCache.getInstances();
    synchronized (lock) {
      sameAZ.clear();
      sameRegion.clear();
      microserviceCacheInstances.forEach(microserviceInstance -> {
        String endPoint = microserviceInstance.getEndpoints().get(0);
        availableIpCache.put(getUri(endPoint), true);
        if (regionAndAZMatch(myself, microserviceInstance)) {
          sameAZ.add(endPoint);
        } else if (regionMatch(myself, microserviceInstance)) {
          sameRegion.add(endPoint);
        }
      });
    }
  }

  public IpPort getAvailableAddress() {
    return getAvailableIpPort();
  }

  private List<CacheEndpoint> getDiscoveredIpPort() {
    InstanceCache instanceCache = instanceCacheManager.getOrCreate(REGISTRY_APP_ID,
        REGISTRY_SERVICE_NAME,
        DefinitionConst.VERSION_RULE_LATEST);
    return instanceCache.getOrCreateTransportMap().get(defaultTransport);
  }

  private void initIpPort() {
    MicroserviceInstance myself = RegistrationManager.INSTANCE.getMicroserviceInstance();
    List<CacheEndpoint> endpoints = getDiscoveredIpPort();

    for (CacheEndpoint cacheEndpoint : endpoints) {
      availableIpCache.put(getUri(cacheEndpoint.getEndpoint()), true);
      if (regionAndAZMatch(myself, cacheEndpoint.getInstance())) {
        sameAZ.add(cacheEndpoint.getEndpoint());
      } else if (regionMatch(myself, cacheEndpoint.getInstance())) {
        sameRegion.add(cacheEndpoint.getEndpoint());
      }
    }
    maxRetryTimes = endpoints.size();
  }

  private IpPort getAvailableIpPort() {
    IpPort ipPort = null;
    if (!autoDiscoveryInited) {
      ipPort = getDefaultIpPort();
    } else {
      List<String> addresses = getAvailableZoneIpPorts();
      if (index.get() >= addresses.size()) {
        index.set(0);
      }
      if (addresses.isEmpty()) {
        ipPort = getDefaultIpPort();
      } else {
        ipPort = new URIEndpointObject(addresses.get(index.get()));
      }
    }
    index.getAndIncrement();
    return ipPort;
  }

  private List<String> getAvailableZoneIpPorts() {
    List<String> results = new ArrayList<>();
    if (!getAvailableAddress(sameAZ).isEmpty()) {
      results.addAll(getAvailableAddress(sameAZ));
    } else {
      results.addAll(getAvailableAddress(sameRegion));
    }
    return results;
  }

  private IpPort getDefaultIpPort() {
    if (index.get() >= defaultIpPort.size()) {
      index.set(0);
    }
    return defaultIpPort.get(index.get());
  }

  private List<String> getAvailableAddress(List<String> endpoints) {
    List<String> result = new ArrayList<>();
    for (String endpoint : endpoints) {
      try {
        if (availableIpCache.get(getUri(endpoint), () -> true)) {
          result.add(endpoint);
        }
      } catch (ExecutionException e) {
        LOGGER.error("Not expected to happen, maybe a bug.", e);
      }
    }
    return result;
  }

  private String getUri(String endpoint) {
    return StringUtils.split(endpoint, "//")[1];
  }

  private boolean regionAndAZMatch(MicroserviceInstance myself, MicroserviceInstance target) {
    if (myself.getDataCenterInfo() == null) {
      // when instance have no datacenter info, it will match all other datacenters
      return true;
    }
    if (target.getDataCenterInfo() != null) {
      return myself.getDataCenterInfo().getRegion().equals(target.getDataCenterInfo().getRegion()) &&
          myself.getDataCenterInfo().getAvailableZone().equals(target.getDataCenterInfo().getAvailableZone());
    }
    return false;
  }

  private boolean regionMatch(MicroserviceInstance myself, MicroserviceInstance target) {
    if (target.getDataCenterInfo() != null) {
      return myself.getDataCenterInfo().getRegion().equals(target.getDataCenterInfo().getRegion());
    }
    return false;
  }
}
