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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.registry.api.event.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.registry.api.event.task.SafeModeChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class AppManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppManager.class);

  // key: appId
  private Map<String, MicroserviceManager> apps = new ConcurrentHashMapEx<>();

  public AppManager() {
    getEventBus().register(this);
  }

  public EventBus getEventBus() {
    return EventManager.getEventBus();
  }

  public Map<String, MicroserviceManager> getApps() {
    return apps;
  }

  // microserviceName maybe normal name or alias name
  public MicroserviceVersionRule getOrCreateMicroserviceVersionRule(String appId, String microserviceName,
      String versionRule) {
    MicroserviceManager microserviceManager = getOrCreateMicroserviceManager(appId);

    return microserviceManager.getOrCreateMicroserviceVersionRule(microserviceName, versionRule);
  }

  public MicroserviceManager getOrCreateMicroserviceManager(String appId) {
    return apps.computeIfAbsent(appId, id -> new MicroserviceManager(this, appId));
  }

  public CompletableFuture<MicroserviceVersions> getOrCreateMicroserviceVersionsAsync(String appId
      , String microserviceName) {
    MicroserviceManager microserviceManager = getOrCreateMicroserviceManager(appId);
    return microserviceManager.getOrCreateMicroserviceVersionsAsync(microserviceName);
  }

  public MicroserviceVersions getOrCreateMicroserviceVersions(String appId, String microserviceName) {
    MicroserviceManager microserviceManager = getOrCreateMicroserviceManager(appId);
    return microserviceManager.getOrCreateMicroserviceVersions(microserviceName);
  }

  public void onMicroserviceInstanceChanged(MicroserviceInstanceChangedEvent changedEvent) {
    MicroserviceManager microserviceManager = apps.get(changedEvent.getKey().getAppId());
    if (microserviceManager == null) {
      return;
    }

    microserviceManager.onMicroserviceInstanceChanged(changedEvent);
  }

  public void onSafeModeChanged(SafeModeChangeEvent modeChangeEvent) {
    apps.values().forEach(microserviceManager -> microserviceManager.onSafeModeChanged(modeChangeEvent));
  }

  public void pullInstances() {
    for (MicroserviceManager microserviceManager : apps.values()) {
      microserviceManager.pullInstances();
    }
  }

  public void safePullInstances() {
    try {
      pullInstances();
    } catch (Exception e) {
      LOGGER.error("failed to pull instances.", e);
    }
  }

  public void markWaitingDelete(String appId, String microserviceName) {
    Optional.ofNullable(apps.get(appId))
        .map(microserviceManager -> microserviceManager.getVersionsByName().get(microserviceName))
        .ifPresent(MicroserviceVersions::markWaitingDelete);
  }
}