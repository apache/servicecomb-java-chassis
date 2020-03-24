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

import java.util.List;

import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.serviceregistry.cache.CacheEndpoint;
import org.apache.servicecomb.serviceregistry.cache.InstanceCache;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManager;
import org.apache.servicecomb.serviceregistry.cache.InstanceCacheManagerNew;
import org.apache.servicecomb.serviceregistry.client.IpPortEndpoint.EndpointTag;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.consumer.AppManager;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;

public class IpPortManager {
  private ServiceRegistryConfig serviceRegistryConfig;

  InstanceCacheManager instanceCacheManager;

  private String defaultTransport = "rest";

  private IpPortEndpoint registryEndpoint;

  private IpPortEndpoint discoveryEndpoint;

  /**
   *  defaultEndpoint is only for the sc which support both registration and discovery,
   *  it's not suitable for the occasion that register on one of the sc but discover on the other.
   */
  @Deprecated
  private IpPortEndpoint defaultEndpoint;

  private boolean autoDiscoveryInited = false;

  public void setAutoDiscoveryInited(boolean autoDiscoveryInited) {
    this.autoDiscoveryInited = autoDiscoveryInited;
  }

  @Deprecated
  public int getMaxRetryTimes() {
    return defaultEndpoint.getMaxRetryTimes();
  }

  public IpPortManager(ServiceRegistryConfig serviceRegistryConfig) {
    this.serviceRegistryConfig = serviceRegistryConfig;
    this.instanceCacheManager = new InstanceCacheManagerNew(new AppManager());

    defaultTransport = serviceRegistryConfig.getTransport();
    defaultEndpoint = new IpPortEndpoint(serviceRegistryConfig.getIpPort(), this, EndpointTag.REGISTRY_DISCOVERY);
    registryEndpoint = new IpPortEndpoint(serviceRegistryConfig.getRegistryIpPort(), this, EndpointTag.REGISTRY);
    discoveryEndpoint = new IpPortEndpoint(serviceRegistryConfig.getDiscoveryIpPort(), this, EndpointTag.DISCOVERY);
  }

  // we have to do this operation after the first time setup has already done
  public void initAutoDiscovery() {
    if (!autoDiscoveryInited && this.serviceRegistryConfig.isRegistryAutoDiscovery()) {
      InstanceCache cache = instanceCacheManager.getOrCreate(REGISTRY_APP_ID,
          REGISTRY_SERVICE_NAME,
          DefinitionConst.VERSION_RULE_LATEST);
      if (cache.getInstanceMap().size() > 0) {
        setAutoDiscoveryInited(true);
      } else {
        setAutoDiscoveryInited(false);
      }
    }
  }

  @Deprecated
  public IpPort getNextAvailableAddress(IpPort failedIpPort) {
    return defaultEndpoint.getNextAvailableAddress(failedIpPort);
  }

  @Deprecated
  public IpPort getAvailableAddress() {
    return defaultEndpoint.getAvailableAddress();
  }

  public IpPort getRegistryAvailableAddress() {
    return registryEndpoint.getAvailableAddress();
  }

  public IpPort getDiscoveryAvailableAddress() {
    return discoveryEndpoint.getAvailableAddress();
  }

  //if global is false, it means that we are registering services, so we should use registry address to find from remote sc.
  public IpPort getAvailableAddressByGlobal(boolean global) {
    return global ? discoveryEndpoint.getAvailableAddress() : registryEndpoint.getAvailableAddress();
  }

  //if global is false, it means that we are registering services, so we should use registry endpoint to find from remote sc.
  public IpPortEndpoint getEndpointByGlobal(boolean global) {
    return global ? discoveryEndpoint : registryEndpoint;
  }

  public IpPortEndpoint getRegistryEndpoint() {
    return registryEndpoint;
  }

  public IpPortEndpoint getDiscoveryEndpoint() {
    return discoveryEndpoint;
  }

  public List<CacheEndpoint> getDiscoveredIpPort() {
    if (!autoDiscoveryInited || !this.serviceRegistryConfig.isRegistryAutoDiscovery()) {
      return null;
    }
    InstanceCache instanceCache = instanceCacheManager.getOrCreate(REGISTRY_APP_ID,
        REGISTRY_SERVICE_NAME,
        DefinitionConst.VERSION_RULE_LATEST);
    return instanceCache.getOrCreateTransportMap().get(defaultTransport);
  }
}
