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

package org.apache.servicecomb.serviceregistry.refresh;

import static org.apache.servicecomb.serviceregistry.api.Const.CONFIG_CENTER_NAME;
import static org.apache.servicecomb.serviceregistry.api.Const.CSE_MONITORING_NAME;
import static org.apache.servicecomb.serviceregistry.api.Const.KIE_NAME;
import static org.apache.servicecomb.serviceregistry.api.Const.REGISTRY_APP_ID;
import static org.apache.servicecomb.serviceregistry.api.Const.REGISTRY_SERVICE_NAME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.http.client.event.RefreshEndpointEvent;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.DataCenterInfo;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.cache.CacheEndpoint;
import org.apache.servicecomb.registry.cache.InstanceCache;
import org.apache.servicecomb.registry.cache.InstanceCacheManager;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.event.ServiceCenterEventBus;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCache;
import org.apache.servicecomb.serviceregistry.registry.cache.MicroserviceCacheRefreshedEvent;

import com.google.common.eventbus.Subscribe;

public class ClassificationAddress {

  private static final String SC_KEY = "SERVICECENTER@default@@0.0.0.0+";

  private static final String CC_KEY = "CseConfigCenter@default@@0.0.0.0+";

  private static final String KIE_KEY = "KIE@default@@0.0.0.0+";

  private static final String MONITORING_KEY = "CseMonitoring@default@@0.0.0.0+";

  private String defaultTransport = "rest";

  private DataCenterInfo dataCenterInfo;

  InstanceCacheManager instanceCacheManager;

  private ArrayList<IpPort> defaultIpPort;

  private int maxRetryTimes;

  public ClassificationAddress(ServiceRegistryConfig serviceRegistryConfig, InstanceCacheManager instanceCacheManager) {
    this.defaultTransport = serviceRegistryConfig.getTransport();
    this.defaultIpPort = serviceRegistryConfig.getIpPort();
    this.instanceCacheManager = instanceCacheManager;
    this.maxRetryTimes = defaultIpPort.size();
    ServiceCenterEventBus.getEventBus().register(this);
  }

  public void InitEndPoint(String typeName) {
    Map<String, List<String>> zoneAndRegion = generateZoneAndRegionAddress(typeName);
    if (zoneAndRegion == null) {
      return;
    }
    EventManager.post(new RefreshEndpointEvent(zoneAndRegion, typeName));
  }

  @Subscribe
  public void onMicroserviceCacheRefreshed(MicroserviceCacheRefreshedEvent event) {
    List<MicroserviceCache> microserviceCaches = event.getMicroserviceCaches();
    if (null == microserviceCaches || microserviceCaches.isEmpty()) {
      return;
    }

    for (MicroserviceCache microserviceCache : microserviceCaches) {
      if (microserviceCache.getKey().toString().equals(SC_KEY)) {
        refreshEndPoints(microserviceCache, REGISTRY_SERVICE_NAME);
      }
      if (microserviceCache.getKey().toString().equals(CC_KEY)) {
        refreshEndPoints(microserviceCache, KIE_NAME);
      }
      if (microserviceCache.getKey().toString().equals(KIE_KEY)) {
        refreshEndPoints(microserviceCache, CONFIG_CENTER_NAME);
      }
      if (microserviceCache.getKey().toString().equals(MONITORING_KEY)) {
        refreshEndPoints(microserviceCache, CSE_MONITORING_NAME);
      }
    }
  }

  private void refreshEndPoints(MicroserviceCache microserviceCache, String name) {
    Map<String, List<String>> zoneAndRegion = refreshEndPoint(microserviceCache);
    EventManager.post(new RefreshEndpointEvent(zoneAndRegion, name));
  }

  private Map<String, List<String>> refreshEndPoint(MicroserviceCache microserviceCache) {
    List<String> sameZone = new ArrayList<>();
    List<String> sameRegion = new ArrayList<>();
    Map<String, List<String>> zoneAndRegion = new HashMap<>();

    List<MicroserviceInstance> microserviceCacheInstances = microserviceCache.getInstances();

    microserviceCacheInstances.forEach(microserviceInstance -> {
      String endPoint = microserviceInstance.getEndpoints().get(0);
      if (regionAndAZMatch(dataCenterInfo, microserviceInstance)) {
        sameZone.add(endPoint);
      } else {
        sameRegion.add(endPoint);
      }
    });
    zoneAndRegion.put("sameZone", sameZone);
    zoneAndRegion.put("sameRegion", sameRegion);
    return zoneAndRegion;
  }

  private Map<String, List<String>> generateZoneAndRegionAddress(String key) {
    InstanceCache KieCaches = instanceCacheManager
        .getOrCreate(REGISTRY_APP_ID, key, DefinitionConst.VERSION_RULE_LATEST);
    List<CacheEndpoint> CacheEndpoints = new ArrayList<>();
    if (REGISTRY_SERVICE_NAME.equals(key)) {
      CacheEndpoints = KieCaches.getOrCreateTransportMap().get(defaultTransport);
      maxRetryTimes = CacheEndpoints.size();
    } else {
      if (KieCaches.getInstanceMap().size() <= 0) {
        return null;
      }
      CacheEndpoints = KieCaches.getOrCreateTransportMap().get(defaultTransport);
    }
    Map<String, List<String>> zoneAndRegion = new HashMap<>();
    dataCenterInfo = findRegion(CacheEndpoints);

    List<String> sameZone = new ArrayList<>();
    List<String> sameRegion = new ArrayList<>();
    for (CacheEndpoint cacheEndpoint : CacheEndpoints) {
      if (regionAndAZMatch(dataCenterInfo, cacheEndpoint.getInstance())) {
        sameZone.add(cacheEndpoint.getEndpoint());
      } else {
        sameRegion.add(cacheEndpoint.getEndpoint());
      }
    }
    zoneAndRegion.put("sameZone", sameZone);
    zoneAndRegion.put("sameRegion", sameRegion);
    return zoneAndRegion;
  }

  private DataCenterInfo findRegion(List<CacheEndpoint> CacheEndpoints) {
    MicroserviceInstance myself = RegistrationManager.INSTANCE.getMicroserviceInstance();
    if (myself.getDataCenterInfo() == null) {
      return null;
    }
    for (CacheEndpoint cacheEndpoint : CacheEndpoints) {
      boolean isMatch = cacheEndpoint.getEndpoint().contains(this.defaultIpPort.get(0).getHostOrIp());
      if (isMatch && cacheEndpoint.getInstance().getDataCenterInfo() != null) {
        return cacheEndpoint.getInstance().getDataCenterInfo();
      }
    }
    return null;
  }

  private boolean regionAndAZMatch(DataCenterInfo myself, MicroserviceInstance target) {
    if (myself == null) {
      // when instance have no datacenter info, it will match all other datacenters
      return true;
    }
    if (target.getDataCenterInfo() != null) {
      return myself.getRegion().equals(target.getDataCenterInfo().getRegion()) &&
          myself.getAvailableZone().equals(target.getDataCenterInfo().getAvailableZone());
    }
    return false;
  }

  public int getMaxRetryTimes() {
    return maxRetryTimes;
  }
}
