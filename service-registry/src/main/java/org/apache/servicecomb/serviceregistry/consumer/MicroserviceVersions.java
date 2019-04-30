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

package org.apache.servicecomb.serviceregistry.consumer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.task.event.MicroserviceNotExistEvent;
import org.apache.servicecomb.serviceregistry.task.event.PullMicroserviceVersionsInstancesEvent;
import org.apache.servicecomb.serviceregistry.task.event.SafeModeChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class MicroserviceVersions {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceVersions.class);

  protected AppManager appManager;

  private String appId;

  private String microserviceName;

  // revision and pulledInstances directly equals to SC's response
  private String revision = null;

  private List<MicroserviceInstance> pulledInstances;

  // in safe mode, instances will never be deleted
  private boolean safeMode = false;

  // instances not always equals to pulledInstances
  // in the future:
  //  pulledInstances means all instance
  //  instances means available instance
  List<MicroserviceInstance> instances;

  // key is service id
  Map<String, MicroserviceVersion> versions = new ConcurrentHashMapEx<>();

  // key is version rule
  Map<String, MicroserviceVersionRule> versionRules = new ConcurrentHashMapEx<>();

  // process pulled instances and create versionRule must be protected by lock
  // otherwise maybe lost instance or version in versionRule
  private final Object lock = new Object();

  // to avoid pull too many time
  // only pendingPullCount is 0, then do a real pull 
  private AtomicInteger pendingPullCount = new AtomicInteger();

  boolean validated = false;

  public MicroserviceVersions(AppManager appManager, String appId, String microserviceName) {
    this.appManager = appManager;
    this.appId = appId;
    this.microserviceName = microserviceName;

    LOGGER.info("create MicroserviceVersions, appId={}, microserviceName={}.",
        appId,
        microserviceName);

    appManager.getEventBus().register(this);
  }

  public boolean isValidated() {
    return validated;
  }

  public AppManager getAppManager() {
    return appManager;
  }

  public String getAppId() {
    return appId;
  }

  public String getMicroserviceName() {
    return microserviceName;
  }

  public Map<String, MicroserviceVersion> getVersions() {
    return versions;
  }

  @SuppressWarnings("unchecked")
  public <T extends MicroserviceVersion> T getVersion(String serviceId) {
    return (T) versions.get(serviceId);
  }

  public String getRevision() {
    return revision;
  }

  public void setRevision(String revision) {
    this.revision = revision;
  }

  public List<MicroserviceInstance> getPulledInstances() {
    return pulledInstances;
  }

  public void submitPull() {
    pendingPullCount.incrementAndGet();

    pullInstances();
  }

  public void pullInstances() {
    if (pendingPullCount.decrementAndGet() != 0) {
      return;
    }

    MicroserviceInstances microserviceInstances = RegistryUtils.findServiceInstances(appId,
        microserviceName,
        DefinitionConst.VERSION_RULE_ALL,
        revision);
    if (microserviceInstances == null) {
      return;
    }
    if (microserviceInstances.isMicroserviceNotExist()) {
      appManager.getEventBus().post(new MicroserviceNotExistEvent(appId, microserviceName));
      return;
    }

    if (!microserviceInstances.isNeedRefresh()) {
      return;
    }

    pulledInstances = microserviceInstances.getInstancesResponse().getInstances();
    pulledInstances.sort(Comparator.comparing(MicroserviceInstance::getInstanceId));
    String rev = microserviceInstances.getRevision();

    safeSetInstances(pulledInstances, rev);
  }

  protected void safeSetInstances(List<MicroserviceInstance> pulledInstances, String rev) {
    try {
      setInstances(pulledInstances, rev);
      validated = true;
    } catch (Throwable e) {
      LOGGER.error("Failed to setInstances, appId={}, microserviceName={}.",
          getAppId(),
          getMicroserviceName(),
          e);
    }
  }

  private void postPullInstanceEvent(long msTime) {
    pendingPullCount.incrementAndGet();
    appManager.getEventBus().post(new PullMicroserviceVersionsInstancesEvent(this, msTime));
  }

  private void setInstances(List<MicroserviceInstance> pulledInstances, String rev) {
    synchronized (lock) {
      instances = mergeInstances(pulledInstances, instances);
      for (MicroserviceInstance instance : instances) {
        // ensure microserviceVersion exists
        versions.computeIfAbsent(instance.getServiceId(),
            microserviceId -> appManager.getMicroserviceVersionFactory().create(microserviceName, microserviceId));
      }

      for (MicroserviceVersionRule microserviceVersionRule : versionRules.values()) {
        microserviceVersionRule.update(versions, instances);
      }
      revision = rev;
    }
  }

  private List<MicroserviceInstance> mergeInstances(List<MicroserviceInstance> pulledInstances,
      List<MicroserviceInstance> inUseInstances) {
    List<MicroserviceInstance> upInstances = new ArrayList<>(pulledInstances);
    if (safeMode) {
      // in safe mode, instances will never be deleted
      upInstances.forEach(instance -> {
        if (!inUseInstances.contains(instance)) {
          inUseInstances.add(instance);
        }
      });
      return inUseInstances;
    }
    if (upInstances.isEmpty() && inUseInstances != null && ServiceRegistryConfig.INSTANCE
        .isEmptyInstanceProtectionEnabled()) {
      MicroserviceInstancePing ping = SPIServiceUtils.getPriorityHighestService(MicroserviceInstancePing.class);
      inUseInstances.forEach(instance -> {
        if (!upInstances.contains(instance)) {
          if (ping.ping(instance)) {
            upInstances.add(instance);
          }
        }
      });
    }
    return upInstances;
  }

  public MicroserviceVersionRule getOrCreateMicroserviceVersionRule(String versionRule) {
    // do not use computeIfAbsent
    MicroserviceVersionRule microserviceVersionRule = versionRules.get(versionRule);
    if (microserviceVersionRule == null) {
      synchronized (lock) {
        microserviceVersionRule = versionRules.computeIfAbsent(versionRule, this::createAndInitMicroserviceVersionRule);
      }
    }

    return microserviceVersionRule;
  }

  protected MicroserviceVersionRule createAndInitMicroserviceVersionRule(String strVersionRule) {
    LOGGER.info("create MicroserviceVersionRule, appId={}, microserviceName={}, versionRule={}.",
        appId,
        microserviceName,
        strVersionRule);

    MicroserviceVersionRule microserviceVersionRule =
        new MicroserviceVersionRule(appId, microserviceName, strVersionRule);
    microserviceVersionRule.update(versions, instances);
    return microserviceVersionRule;
  }

  @Subscribe
  public void onMicroserviceInstanceChanged(MicroserviceInstanceChangedEvent changedEvent) {
    if (!isEventAccept(changedEvent)) {
      return;
    }
    // pull instances always replace old instances, not append
    //
    // pull result and watch event sequence is not defined even inside SC.
    // it's not safe to trust the event, so we just send a new pull request
    //
    // CREATE/UPDATE:
    //   if pull 1/2/3, and then add 4, but "add 4" received before pull result, will lost 4.
    // DELETE:
    //   if pull 1/2/3, and then delete 3, but "delete 3" received before pull result, will have wrong 3.
    // EXPIRE::
    //   black/white config in SC changed, we must refresh all data from sc.
    postPullInstanceEvent(0);
  }

  @Subscribe
  public void onSafeModeChanged(SafeModeChangeEvent modeChangeEvent) {
    this.safeMode = modeChangeEvent.getCurrentMode();
  }

  protected boolean isEventAccept(MicroserviceInstanceChangedEvent changedEvent) {
    return (appId.equals(changedEvent.getKey().getAppId()) &&
        microserviceName.equals(changedEvent.getKey().getServiceName())) ||
        microserviceName.equals(
            changedEvent.getKey().getAppId() + Const.APP_SERVICE_SEPARATOR + changedEvent.getKey().getServiceName());
  }

  public void destroy() {
    for (MicroserviceVersion microserviceVersion : versions.values()) {
      microserviceVersion.destroy();
    }
  }
}
