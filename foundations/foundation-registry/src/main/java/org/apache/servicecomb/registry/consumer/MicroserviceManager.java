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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.registry.api.event.MicroserviceInstanceChangedEvent;
import org.apache.servicecomb.registry.api.event.task.SafeModeChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicroserviceManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceManager.class);

  private AppManager appManager;

  private String appId;

  // key: microserviceName
  private Map<String, MicroserviceVersions> versionsByName = new ConcurrentHashMapEx<>();

  private Object lock = new Object();

  public MicroserviceManager(AppManager appManager, String appId) {
    this.appManager = appManager;
    this.appId = appId;
  }

  public Map<String, MicroserviceVersions> getVersionsByName() {
    return versionsByName;
  }

  public MicroserviceVersions getOrCreateMicroserviceVersions(String microserviceName) {
    // do not use ConcurrentHashMap computeIfAbsent for versionsByName
    // because: when create MicroserviceVersions, one creation may depend on another
    // MicroserviceVersions. And pullInstances will create a new MicroserviceVersions.
    // Calling ConcurrentHashMap computeIfAbsent inside will get deadlock.
    MicroserviceVersions microserviceVersions = versionsByName.get(microserviceName);
    if (microserviceVersions == null) {
      synchronized (lock) {
        microserviceVersions = versionsByName.get(microserviceName);
        if (microserviceVersions == null) {
          microserviceVersions = new MicroserviceVersions(appManager, appId, microserviceName);
          microserviceVersions.pullInstances();
          versionsByName.put(microserviceName, microserviceVersions);
        }
      }
    }

    tryRemoveInvalidMicroservice(microserviceVersions);

    return microserviceVersions;
  }

  private void tryRemoveInvalidMicroservice(MicroserviceVersions microserviceVersions) {
    if (!microserviceVersions.isWaitingDelete()) {
      return;
    }

    // remove this microservice if it does not exist or not registered in order to get it back when access it again
    String microserviceName = microserviceVersions.getMicroserviceName();
    if (versionsByName.remove(microserviceName) != null) {
      microserviceVersions.destroy();
      LOGGER.info("remove microservice, appId={}, microserviceName={}.", appId, microserviceName);
    }
  }

  public MicroserviceVersionRule getOrCreateMicroserviceVersionRule(String microserviceName,
      String versionRule) {
    MicroserviceVersions microserviceVersions = getOrCreateMicroserviceVersions(microserviceName);

    return microserviceVersions.getOrCreateMicroserviceVersionRule(versionRule);
  }

  public void pullInstances() {
    for (MicroserviceVersions microserviceVersions : versionsByName.values()) {
      microserviceVersions.pullInstances();

      tryRemoveInvalidMicroservice(microserviceVersions);
    }
  }

  public void onMicroserviceInstanceChanged(MicroserviceInstanceChangedEvent changedEvent) {
    for (MicroserviceVersions microserviceVersions : versionsByName.values()) {
      microserviceVersions.onMicroserviceInstanceChanged(changedEvent);

      tryRemoveInvalidMicroservice(microserviceVersions);
    }
  }

  public void onSafeModeChanged(SafeModeChangeEvent modeChangeEvent) {
    versionsByName.values().forEach(microserviceVersions -> microserviceVersions.onSafeModeChanged(modeChangeEvent));
  }
}
