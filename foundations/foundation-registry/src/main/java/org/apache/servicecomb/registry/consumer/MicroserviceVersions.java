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

package org.apache.servicecomb.registry.consumer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.servicecomb.foundation.common.VendorExtensions;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.event.CreateMicroserviceEvent;
import org.apache.servicecomb.registry.api.event.DestroyMicroserviceEvent;
import org.apache.servicecomb.registry.api.event.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.registry.config.ServiceRegistryCommonConfig;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.registry.definition.MicroserviceNameParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicroserviceVersions {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceVersions.class);

  protected AppManager appManager;

  protected String appId;

  protected String shortName;

  protected String microserviceName;

  // revision and pulledInstances directly equals to SC's response
  protected String revision = null;

  private List<MicroserviceInstance> pulledInstances;

  private MicroserviceInstances lastPulledResult;

  // instances not always equals to pulledInstances
  // in the future:
  //  pulledInstances means all instance
  //  instances means available instance
  Collection<MicroserviceInstance> instances;

  // key is service id
  Map<String, MicroserviceVersion> versions = new ConcurrentHashMapEx<>();

  // key is version rule
  Map<String, MicroserviceVersionRule> versionRules = new ConcurrentHashMapEx<>();

  // process pulled instances and create versionRule must be protected by lock
  // otherwise maybe lost instance or version in versionRule
  private final Object lock = new Object();

  private long lastPullTime = 0;

  private boolean waitingDelete = false;

  private final VendorExtensions vendorExtensions = new VendorExtensions();

  public MicroserviceVersions(AppManager appManager, String appId, String microserviceName) {
    this.appManager = appManager;
    this.appId = appId;
    this.microserviceName = microserviceName;
    this.shortName = new MicroserviceNameParser(appId, microserviceName).getShortName();

    appManager.getEventBus().post(new CreateMicroserviceEvent(this));

    LOGGER.info("create MicroserviceVersions, appId={}, microserviceName={}.",
        appId,
        microserviceName);
  }

  public boolean isWaitingDelete() {
    return waitingDelete;
  }

  public MicroserviceVersions markWaitingDelete() {
    this.waitingDelete = true;
    return this;
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

  public String getShortName() {
    return shortName;
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

  public long getLastPullTime() {
    return lastPullTime;
  }

  public MicroserviceInstances getLastPulledResult() {
    return lastPulledResult;
  }

  public VendorExtensions getVendorExtensions() {
    return vendorExtensions;
  }

  public void pullInstances() {
    lastPullTime = System.currentTimeMillis();
    MicroserviceInstances microserviceInstances = findServiceInstances();
    lastPulledResult = microserviceInstances;
    if (microserviceInstances == null) {
      // pulled failed, did not get anything
      // will not do anything, consumers will use existing instances
      return;
    }
    if (microserviceInstances.isMicroserviceNotExist()) {
      // pulled failed, SC said target not exist
      waitingDelete = true;
      return;
    }

    if (null != revision && revision.equals(microserviceInstances.getRevision())) {
      return;
    }

    pulledInstances = microserviceInstances.getInstancesResponse().getInstances();
    pulledInstances.sort(Comparator.comparing(MicroserviceInstance::getInstanceId));
    String rev = microserviceInstances.getRevision();

    safeSetInstances(pulledInstances, rev);
  }

  protected MicroserviceInstances findServiceInstances() {
    return DiscoveryManager.INSTANCE.findServiceInstances(appId,
        microserviceName,
        DefinitionConst.VERSION_RULE_ALL,
        revision);
  }

  protected void safeSetInstances(List<MicroserviceInstance> pulledInstances, String rev) {
    try {
      List<MicroserviceInstance> filteredInstance = pulledInstances;
      // 增加一个配置项只使用 `UP` 实例。 在使用 `TESTING` 进行拨测， 并且配置了
      // servicecomb.references.version-rule=latest 场景，需要保证不使用
      // `TESTING` 实例。 不能依赖 InstanceStatusDiscoveryFilter, 避免
      // 构建的 VersionRule 实例列表为空。
      if (ServiceRegistryCommonConfig.useUpInstancesOnly()) {
        filteredInstance = pulledInstances.stream().filter(item -> MicroserviceInstanceStatus.UP == item.getStatus())
            .collect(Collectors.toList());
      }
      setInstances(filteredInstance, rev);
    } catch (Throwable e) {
      waitingDelete = true;
      LOGGER.error("Failed to setInstances, appId={}, microserviceName={}.",
          getAppId(),
          getMicroserviceName(),
          e);
    }
  }

  static class MergedInstances {
    // key is microserviceId
    Map<String, List<MicroserviceInstance>> microserviceIdMap = new HashMap<>();

    // key is instanceId
    Map<String, MicroserviceInstance> instanceIdMap = new HashMap<>();

    void addInstance(MicroserviceInstance instance) {
      instanceIdMap.put(instance.getInstanceId(), instance);
      microserviceIdMap
          .computeIfAbsent(instance.getServiceId(), key -> new ArrayList<>())
          .add(instance);
    }
  }

  private void setInstances(List<MicroserviceInstance> pulledInstances, String rev) {
    synchronized (lock) {
      MergedInstances mergedInstances = mergeInstances(pulledInstances, instances);
      instances = mergedInstances.instanceIdMap.values();
      // clear cache
      versions.forEach((key, value) -> value.setInstances(new ArrayList<>()));
      for (Entry<String, List<MicroserviceInstance>> entry : mergedInstances.microserviceIdMap.entrySet()) {
        // always update microservice versions, because we allow microservice info override, like schema info
        MicroserviceVersion newVersion = createMicroserviceVersion(entry.getKey(), entry.getValue());
        newVersion.setInstances(entry.getValue());
        versions.put(entry.getKey(), newVersion);
      }

      for (MicroserviceVersionRule microserviceVersionRule : versionRules.values()) {
        microserviceVersionRule.update(versions, instances);
      }
      revision = rev;
    }
  }

  protected MicroserviceVersion createMicroserviceVersion(String microserviceId, List<MicroserviceInstance> instances) {
    return new MicroserviceVersion(this, microserviceId, microserviceName, instances);
  }

  private MergedInstances mergeInstances(List<MicroserviceInstance> pulledInstances,
      Collection<MicroserviceInstance> inUseInstances) {
    MergedInstances mergedInstances = new MergedInstances();
    pulledInstances.stream().forEach(mergedInstances::addInstance);
    MicroserviceInstancePing ping = SPIServiceUtils.getPriorityHighestService(MicroserviceInstancePing.class);

    if (pulledInstances.isEmpty() && inUseInstances != null && ServiceRegistryCommonConfig
        .isEmptyInstanceProtectionEnabled()) {

      inUseInstances.stream().forEach(instance -> {
        if (!mergedInstances.instanceIdMap.containsKey(instance.getInstanceId())) {
          if (ping.ping(instance)) {
            mergedInstances.addInstance(instance);
          }
        }
      });
    }

    return mergedInstances;
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
    pullInstances();
  }

  protected boolean isEventAccept(MicroserviceInstanceChangedEvent changedEvent) {
    return (appId.equals(changedEvent.getKey().getAppId()) &&
        microserviceName.equals(changedEvent.getKey().getServiceName())) ||
        microserviceName.equals(
            changedEvent.getKey().getAppId() + DefinitionConst.APP_SERVICE_SEPARATOR + changedEvent.getKey()
                .getServiceName());
  }

  public void destroy() {
    for (MicroserviceVersion microserviceVersion : versions.values()) {
      microserviceVersion.destroy();
    }

    appManager.getEventBus().post(new DestroyMicroserviceEvent(this));
  }
}
