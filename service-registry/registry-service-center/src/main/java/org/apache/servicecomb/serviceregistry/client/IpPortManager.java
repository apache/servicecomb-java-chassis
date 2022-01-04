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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.cache.CacheEndpoint;
import org.apache.servicecomb.registry.cache.InstanceCache;
import org.apache.servicecomb.registry.cache.InstanceCacheManager;
import org.apache.servicecomb.registry.cache.InstanceCacheManagerNew;
import org.apache.servicecomb.registry.consumer.AppManager;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public class IpPortManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(IpPortManager.class);

  private ServiceRegistryConfig serviceRegistryConfig;

  InstanceCacheManager instanceCacheManager;

  private String defaultTransport = "rest";

  private ArrayList<IpPort> defaultIpPort;

  private AtomicInteger currentAvailableIndex;

  private boolean autoDiscoveryInited = false;

  private int maxRetryTimes;

  private final List<String> addresses = new ArrayList<>();

  private int index = 0;

  private List<String> sameAZ = new ArrayList<>();

  private List<String> sameRegion = new ArrayList<>();

  public static final Cache<String, Boolean> availableIpCache = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .expireAfterAccess(10, TimeUnit.SECONDS)
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

  public IpPort getAvailableAddress() {
    return getAvailableAddress(currentAvailableIndex.incrementAndGet());
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
//    microservices.put("test","true");
//    microservices.put("test2","true");
//    for(int i =0;i<10;i++){
//      try {
//        Thread.sleep(1000);
//
//        microservices.get("test", () ->  "false");
//      } catch (InterruptedException | ExecutionException e) {
//        e.printStackTrace();
//      }
//    }

    maxRetryTimes = defaultIpPort.size() + endpoints.size();
    CacheEndpoint nextEndpoint = endpoints.get(index - defaultIpPort.size());
    return new URIEndpointObject(nextEndpoint.getEndpoint());
  }

  private List<CacheEndpoint> getDiscoveredIpPort() {
    if (!autoDiscoveryInited || !this.serviceRegistryConfig.isRegistryAutoDiscovery()) {
      return null;
    }
    InstanceCache instanceCache = instanceCacheManager.getOrCreate(REGISTRY_APP_ID,
        REGISTRY_SERVICE_NAME,
        DefinitionConst.VERSION_RULE_LATEST);
    return instanceCache.getOrCreateTransportMap().get(defaultTransport);
  }

  private void getIpPort() {
    MicroserviceInstance myself = RegistrationManager.INSTANCE.getMicroserviceInstance();
    List<CacheEndpoint> endpoints = getDiscoveredIpPort();

//    List<String> sameAZ = new ArrayList<>();
//    List<String> sameRegion = new ArrayList<>();
    for (CacheEndpoint cacheEndpoint: endpoints) {
      availableIpCache.put(getUri(cacheEndpoint.getEndpoint()),true);
      if(regionAndAZMatch(myself,cacheEndpoint.getInstance())) {
        sameAZ.add(cacheEndpoint.getEndpoint());
      } else if(regionMatch(myself,cacheEndpoint.getInstance())) {
        sameRegion.add(cacheEndpoint.getEndpoint());
      }
    }
  }

  private IpPort getAvailableIpPort() {
    synchronized (this) {
      try {

        addresses.addAll(getAvailableAddress(sameAZ));
        if(!getAvailableAddress(sameAZ).isEmpty()){
          
        }
        this.index++;
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
      if (this.index >= addresses.size()) {
        this.index = 0;
      }
      return addresses.get(index);
    }

  }

  private List<String> getAvailableAddress(List<String> endpoints) throws ExecutionException {
    List<String> result = new ArrayList<>();
    for(String endpoint: endpoints) {
      if(availableIpCache.get(endpoint,()->true)) {
        result.add(endpoint);
      }
    }
    return result;
  }

  private String getUri(String endpoint) {
    return StringUtils.split(endpoint, "//")[1];
  }

  private List<String> getSameAZAnd() {
    MicroserviceInstance myself = RegistrationManager.INSTANCE.getMicroserviceInstance();
    return null;
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
