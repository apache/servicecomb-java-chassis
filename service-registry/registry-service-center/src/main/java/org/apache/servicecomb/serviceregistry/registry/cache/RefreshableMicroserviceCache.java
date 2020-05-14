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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceInstancePing;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.task.event.SafeModeChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefreshableMicroserviceCache implements MicroserviceCache {
  private static final Logger LOGGER = LoggerFactory.getLogger(RefreshableMicroserviceCache.class);

  MicroserviceCacheKey key;

  List<MicroserviceInstance> instances = Collections.unmodifiableList(new ArrayList<>());

  Microservice consumerService;

  String revisionId;

  ServiceRegistryClient srClient;

  boolean safeMode;

  MicroserviceCacheStatus status = MicroserviceCacheStatus.INIT;

  private final Object SET_OPERATION_LOCK = new Object();

  boolean emptyInstanceProtectionEnabled;

  MicroserviceInstancePing instancePing = SPIServiceUtils.getPriorityHighestService(MicroserviceInstancePing.class);

  RefreshableMicroserviceCache(Microservice consumerService, MicroserviceCacheKey key, ServiceRegistryClient srClient,
      boolean emptyInstanceProtectionEnabled) {
    this.key = key;
    this.consumerService = consumerService;
    this.srClient = srClient;
    this.emptyInstanceProtectionEnabled = emptyInstanceProtectionEnabled;
  }

  @Override
  public void refresh() {
    safePullInstance(revisionId);
  }

  @Override
  public void forceRefresh() {
    safePullInstance(null);
  }

  void safePullInstance(String revisionId) {
    try {
      pullInstance(revisionId);
    } catch (Throwable e) {
      LOGGER.error("unknown error occurs while pulling instances", e);
      setStatus(MicroserviceCacheStatus.UNKNOWN_ERROR);
    }
  }

  void pullInstance(String revisionId) {
    MicroserviceInstances serviceInstances = pullInstanceFromServiceCenter(revisionId);

    if (serviceInstances == null) {
      LOGGER.error("Can not find any instances from service center due to previous errors. service={}/{}/{}",
          key.getAppId(),
          key.getServiceName(),
          key.getVersionRule());
      setStatus(MicroserviceCacheStatus.CLIENT_ERROR);
      return;
    }

    if (serviceInstances.isMicroserviceNotExist()) {
      setStatus(MicroserviceCacheStatus.SERVICE_NOT_FOUND);
      return;
    }

    if (!serviceInstances.isNeedRefresh()) {
      LOGGER.debug("instances revision is not changed, service={}/{}/{}", key.getAppId(), key.getServiceName(),
          key.getVersionRule());
      setStatus(MicroserviceCacheStatus.NO_CHANGE);
      return;
    }

    List<MicroserviceInstance> instances = serviceInstances.getInstancesResponse().getInstances();
    LOGGER.info("find instances[{}] from service center success. service={}/{}/{}, old revision={}, new revision={}",
        instances.size(),
        key.getAppId(),
        key.getServiceName(),
        key.getVersionRule(),
        this.revisionId,
        serviceInstances.getRevision());
    for (MicroserviceInstance instance : instances) {
      LOGGER.info("service id={}, instance id={}, endpoints={}",
          instance.getServiceId(),
          instance.getInstanceId(),
          instance.getEndpoints());
    }
    safeSetInstances(instances, serviceInstances.getRevision());
  }

  MicroserviceInstances pullInstanceFromServiceCenter(String revisionId) {
    return srClient.findServiceInstances(consumerService.getServiceId(),
        key.getAppId(), key.getServiceName(), key.getVersionRule(), revisionId);
  }

  private void safeSetInstances(List<MicroserviceInstance> pulledInstances, String rev) {
    try {
      synchronized (SET_OPERATION_LOCK) {
        setInstances(pulledInstances, rev);
        setStatus(MicroserviceCacheStatus.REFRESHED);
      }
    } catch (Throwable e) {
      setStatus(MicroserviceCacheStatus.SETTING_CACHE_ERROR);
      LOGGER.error("Failed to setInstances, appId={}, microserviceName={}.",
          key.getAppId(),
          key.getServiceName(),
          e);
    }
  }

  private void setInstances(List<MicroserviceInstance> pulledInstances, String rev) {
    Set<MicroserviceInstance> mergedInstances = mergeInstances(pulledInstances);
    LOGGER.debug("actually set instances[{}] for {}", mergedInstances.size(), key.plainKey());
    for (MicroserviceInstance mergedInstance : mergedInstances) {
      LOGGER.debug("serviceId={}, instanceId={}, endpoints={}",
          mergedInstance.getServiceId(),
          mergedInstance.getInstanceId(),
          mergedInstance.getEndpoints());
    }
    instances = Collections.unmodifiableList(new ArrayList<>(mergedInstances));
    revisionId = rev;
  }

  protected Set<MicroserviceInstance> mergeInstances(List<MicroserviceInstance> pulledInstances) {
    Set<MicroserviceInstance> mergedInstances = new LinkedHashSet<>(pulledInstances);

    if (safeMode) {
      // in safe mode, instances will never be deleted
      mergedInstances.addAll(instances);
      return mergedInstances;
    }

    if (!inEmptyPulledInstancesProtectionSituation(pulledInstances)) {
      return mergedInstances;
    }

    if (null == instancePing) {
      LOGGER.info("no MicroserviceInstancePing implementation loaded, abandon the old instance list");
      return mergedInstances;
    }

    instances.forEach(instance -> {
      if (!mergedInstances.contains(instance)) {
        if (instancePing.ping(instance)) {
          mergedInstances.add(instance);
        }
      }
    });
    return mergedInstances;
  }

  private boolean inEmptyPulledInstancesProtectionSituation(List<MicroserviceInstance> pulledInstances) {
    return pulledInstances.isEmpty()
        && instances != null
        && !instances.isEmpty()
        && isEmptyInstanceProtectionEnabled();
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

  void setStatus(MicroserviceCacheStatus status) {
    this.status = status;
  }

  boolean isEmptyInstanceProtectionEnabled() {
    return emptyInstanceProtectionEnabled;
  }

  void setEmptyInstanceProtectionEnabled(boolean emptyInstanceProtectionEnabled) {
    this.emptyInstanceProtectionEnabled = emptyInstanceProtectionEnabled;
  }

  void onMicroserviceInstanceChanged(MicroserviceInstanceChangedEvent event) {
    if (!microserviceMatched(event)) {
      return;
    }
    refresh();
  }

  void onSafeModeChanged(SafeModeChangeEvent modeChangeEvent) {
    this.safeMode = modeChangeEvent.getCurrentMode();
  }

  private boolean microserviceMatched(MicroserviceInstanceChangedEvent event) {
    return (key.getAppId().equals(event.getKey().getAppId())) // appId matched
        && ( // microserviceName matched
        key.getServiceName().equals(event.getKey().getServiceName())
            || key.getServiceName().equals(
            event.getKey().getAppId() + DefinitionConst.APP_SERVICE_SEPARATOR + event.getKey().getServiceName()
        ));
  }
}
