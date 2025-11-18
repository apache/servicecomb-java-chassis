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

package org.apache.servicecomb.registry.sc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.http.client.event.RefreshEndpointEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.HeartBeatEvent;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.exception.OperationException;
import org.apache.servicecomb.service.center.client.model.DataCenterInfo;
import org.apache.servicecomb.service.center.client.model.FindMicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

/**
 * Support auto discovery of service center addresses.
 */
public class SCAddressManager {
  public enum Type {
    SERVICECENTER,
    KIE,
    CseConfigCenter,
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(SCAddressManager.class);

  private final ServiceCenterClient serviceCenterClient;

  private final SCRegistration scRegistration;

  private final SCConfigurationProperties configurationProperties;

  private final Map<String, HashSet<String>> lastEngineEndpointsCache = new HashMap<>();

  public SCAddressManager(SCConfigurationProperties configurationProperties,
      ServiceCenterClient serviceCenterClient,
      SCRegistration scRegistration) {
    this.configurationProperties = configurationProperties;
    this.serviceCenterClient = serviceCenterClient;
    this.scRegistration = scRegistration;
    scRegistration.getEventBus().register(this);
  }

  @Subscribe
  public void onHeartBeatEvent(HeartBeatEvent event) {
    if (event.isSuccess() && configurationProperties.isAutoDiscovery()) {
      for (Type type : Type.values()) {
        initEndPort(type.name());
      }
    }
  }

  private void initEndPort(String key) {
    List<MicroserviceInstance> instances = findServiceInstance("default", key, "0+");
    HashSet<String> currentEngineEndpoints = new HashSet<>();
    Map<String, List<String>> zoneAndRegion = generateZoneAndRegionAddress(instances, currentEngineEndpoints);
    if (zoneAndRegion == null) {
      return;
    }
    if (isEngineEndpointsChanged(lastEngineEndpointsCache.get(key), currentEngineEndpoints)) {
      LOGGER.info("auto discovery service [{}] addresses: [{}]", key, zoneAndRegion);
      lastEngineEndpointsCache.put(key, currentEngineEndpoints);
      EventManager.post(new RefreshEndpointEvent(zoneAndRegion, key));
    }
  }

  private boolean isEngineEndpointsChanged(Set<String> lastEngineEndpoints, Set<String> currentEngineEndpoints) {
    if (lastEngineEndpoints == null || lastEngineEndpoints.isEmpty()) {
      return true;
    }
    HashSet<String> compareTemp = new HashSet<>(lastEngineEndpoints);
    compareTemp.removeAll(currentEngineEndpoints);
    return !compareTemp.isEmpty() || lastEngineEndpoints.size() != currentEngineEndpoints.size();
  }

  private Map<String, List<String>> generateZoneAndRegionAddress(List<MicroserviceInstance> instances,
      HashSet<String> currentEngineEndpoints) {
    if (instances.isEmpty()) {
      return null;
    }

    Map<String, List<String>> zoneAndRegion = new HashMap<>();
    DataCenterInfo dataCenterInfo = findRegion(instances);

    Set<String> sameZone = new HashSet<>();
    Set<String> sameRegion = new HashSet<>();
    for (MicroserviceInstance microserviceInstance : instances) {
      if (regionAndAZMatch(dataCenterInfo, microserviceInstance)) {
        sameZone.addAll(microserviceInstance.getEndpoints());
      } else {
        sameRegion.addAll(microserviceInstance.getEndpoints());
      }
      currentEngineEndpoints.addAll(microserviceInstance.getEndpoints());
    }
    zoneAndRegion.put("sameZone", new ArrayList<>(sameZone));
    zoneAndRegion.put("sameRegion", new ArrayList<>(sameRegion));
    return zoneAndRegion;
  }

  private DataCenterInfo findRegion(List<MicroserviceInstance> microserviceInstances) {
    for (MicroserviceInstance microserviceInstance : microserviceInstances) {
      boolean isMatch = microserviceInstance.getEndpoints().get(0)
          .contains(scRegistration.getBackendMicroserviceInstance()
              .getEndpoints().get(0));
      if (isMatch && microserviceInstance.getDataCenterInfo() != null) {
        return microserviceInstance.getDataCenterInfo();
      }
    }

    if (scRegistration.getBackendMicroserviceInstance().getDataCenterInfo() == null) {
      return null;
    }
    return scRegistration.getBackendMicroserviceInstance().getDataCenterInfo();
  }

  private List<MicroserviceInstance> findServiceInstance(String appId, String serviceName, String versionRule) {
    try {
      FindMicroserviceInstancesResponse instancesResponse = serviceCenterClient
          .findMicroserviceInstance(scRegistration.getBackendMicroserviceInstance().getServiceId(),
              appId, serviceName, versionRule, null);
      return instancesResponse.getMicroserviceInstancesResponse().getInstances();
    } catch (OperationException operationException) {
      LOGGER.warn("not find the Microservice instance of {}", serviceName);
      return new ArrayList<>();
    }
  }

  private boolean regionAndAZMatch(DataCenterInfo myself, MicroserviceInstance target) {
    if (myself == null) {
      return true;
    }
    if (target.getDataCenterInfo() != null) {
      return myself.getRegion().equals(target.getDataCenterInfo().getRegion()) &&
          myself.getAvailableZone().equals(target.getDataCenterInfo().getAvailableZone());
    }
    return false;
  }
}
