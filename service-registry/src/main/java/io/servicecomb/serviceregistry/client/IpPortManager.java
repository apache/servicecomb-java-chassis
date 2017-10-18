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

package io.servicecomb.serviceregistry.client;

import static io.servicecomb.serviceregistry.api.Const.REGISTRY_APP_ID;
import static io.servicecomb.serviceregistry.api.Const.REGISTRY_SERVICE_NAME;
import static io.servicecomb.serviceregistry.api.Const.REGISTRY_VERSION;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.foundation.common.net.IpPort;
import io.servicecomb.foundation.common.net.URIEndpointObject;
import io.servicecomb.serviceregistry.cache.CacheEndpoint;
import io.servicecomb.serviceregistry.cache.InstanceCache;
import io.servicecomb.serviceregistry.cache.InstanceCacheManager;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;

public class IpPortManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(IpPortManager.class);

  private ServiceRegistryConfig serviceRegistryConfig;

  private InstanceCacheManager instanceCacheManager;

  private String defaultTransport = "rest";

  private ArrayList<IpPort> defaultIpPort;

  private InstanceCache instanceCache = null;

  private AtomicInteger currentAvailbleIndex = new AtomicInteger(0);

  public IpPortManager(ServiceRegistryConfig serviceRegistryConfig, InstanceCacheManager instanceCacheManager) {
    this.serviceRegistryConfig = serviceRegistryConfig;
    this.instanceCacheManager = instanceCacheManager;

    defaultTransport = serviceRegistryConfig.getTransport();
    defaultIpPort = serviceRegistryConfig.getIpPort();
    if (defaultIpPort.size() == 0) {
      throw new IllegalArgumentException("Service center address is required to start the application.");
    }
  }

  // we have to do this operation after the first time setup has already done
  public void initAutoDiscovery() {
    if (this.serviceRegistryConfig.isRegistryAutoDiscovery()) {
      instanceCache = instanceCacheManager.getOrCreate(REGISTRY_APP_ID,
          REGISTRY_SERVICE_NAME,
          REGISTRY_VERSION);
    }
  }

  public IpPort getAvailableAddress(boolean invalidate) {
    int index;
    if (invalidate) {
      index = currentAvailbleIndex.incrementAndGet();
    } else {
      index = currentAvailbleIndex.get();
    }

    if (index < defaultIpPort.size()) {
      return returnWithLog(invalidate, defaultIpPort.get(index));
    }
    List<CacheEndpoint> endpoints = getDiscoveredIpPort();
    if (index >= defaultIpPort.size() + endpoints.size()) {
      currentAvailbleIndex.set(0);
      return returnWithLog(invalidate, defaultIpPort.get(0));
    }
    CacheEndpoint nextEndpoint = endpoints.get(index - defaultIpPort.size());
    return returnWithLog(invalidate, new URIEndpointObject(nextEndpoint.getEndpoint()));
  }

  private IpPort returnWithLog(boolean needLog, IpPort result) {
    if (needLog) {
      LOGGER.info("Using next service center address {}:{}.", result.getHostOrIp(), result.getPort());
    }
    return result;
  }

  private List<CacheEndpoint> getDiscoveredIpPort() {
    if (instanceCache == null) {
      return new ArrayList<>(0);
    }
    instanceCache = instanceCacheManager.getOrCreate(REGISTRY_APP_ID,
        REGISTRY_SERVICE_NAME,
        REGISTRY_VERSION);
    return instanceCache.getOrCreateTransportMap().get(defaultTransport);
  }
}
