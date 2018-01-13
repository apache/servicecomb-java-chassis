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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.serviceregistry.client.http.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.task.event.PullMicroserviceVersionsInstancesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class MicroserviceVersions {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceVersions.class);

  private AppManager appManager;

  private String appId;

  private String microserviceName;

  private String revision = null;

  private List<MicroserviceInstance> instances;

  // key is service id
  private Map<String, MicroserviceVersion> versions = new ConcurrentHashMapEx<>();

  // key is version rule
  private Map<String, MicroserviceVersionRule> versionRules = new ConcurrentHashMapEx<>();

  // process pulled instances and create versionRule must be protected by lock
  // otherwise maybe lost instance or version in versionRule
  private final Object lock = new Object();

  // to avoid pull too many time
  // only pendingPullCount is 0, then do a real pull 
  private AtomicInteger pendingPullCount = new AtomicInteger();

  public MicroserviceVersions(AppManager appManager, String appId, String microserviceName) {
    this.appManager = appManager;
    this.appId = appId;
    this.microserviceName = microserviceName;

    LOGGER.info("create MicroserviceVersions, appId={}, microserviceName={}.",
        appId,
        microserviceName);

    appManager.getEventBus().register(this);
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
      // exception happens and try pull again later.
      pendingPullCount.incrementAndGet();
      appManager.getEventBus().post(new PullMicroserviceVersionsInstancesEvent(this, TimeUnit.SECONDS.toMillis(1)));
      return;
    }
    if (!microserviceInstances.isNeedRefresh()) {
      return;
    }
    List<MicroserviceInstance> pulledInstances = microserviceInstances.getInstancesResponse().getInstances();
    String rev = microserviceInstances.getRevision();

    setInstances(pulledInstances, rev);
  }

  private void setInstances(List<MicroserviceInstance> pulledInstances, String rev) {
    synchronized (lock) {
      instances = pulledInstances
          .stream()
          .filter(instance -> {
            return MicroserviceInstanceStatus.UP.equals(instance.getStatus());
          })
          .collect(Collectors.toList());
      for (MicroserviceInstance instance : instances) {
        // ensure microserviceVersion exists
        versions.computeIfAbsent(instance.getServiceId(), microserviceId -> {
          MicroserviceVersion microserviceVersion =
              appManager.getMicroserviceVersionFactory().create(microserviceName, microserviceId);
          for (MicroserviceVersionRule microserviceVersionRule : versionRules.values()) {
            microserviceVersionRule.addMicroserviceVersion(microserviceVersion);
          }
          return microserviceVersion;
        });
      }

      for (MicroserviceVersionRule microserviceVersionRule : versionRules.values()) {
        microserviceVersionRule.setInstances(instances);
      }
      revision = rev;
    }
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
    for (MicroserviceVersion microserviceVersion : versions.values()) {
      microserviceVersionRule.addMicroserviceVersion(microserviceVersion);
    }
    microserviceVersionRule.setInstances(instances);
    return microserviceVersionRule;
  }

  @Subscribe
  public void onMicroserviceInstanceChanged(MicroserviceInstanceChangedEvent changedEvent) {
    if (!appId.equals(changedEvent.getKey().getAppId()) ||
        !microserviceName.equals(changedEvent.getKey().getServiceName())) {
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
    pendingPullCount.incrementAndGet();
    appManager.getEventBus().post(new PullMicroserviceVersionsInstancesEvent(this, TimeUnit.SECONDS.toMillis(1)));
  }
}
