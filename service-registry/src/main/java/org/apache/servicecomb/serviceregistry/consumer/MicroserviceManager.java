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

import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.serviceregistry.api.response.MicroserviceInstanceChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicroserviceManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceManager.class);

  private AppManager appManager;

  private String appId;

  // key: microserviceName
  private Map<String, MicroserviceVersions> versionsByName = new ConcurrentHashMapEx<>();

  public MicroserviceManager(AppManager appManager, String appId) {
    this.appManager = appManager;
    this.appId = appId;
  }

  public Map<String, MicroserviceVersions> getVersionsByName() {
    return versionsByName;
  }

  public MicroserviceVersions getOrCreateMicroserviceVersions(String microserviceName) {
    MicroserviceVersions microserviceVersions = versionsByName.computeIfAbsent(microserviceName, name -> {
      MicroserviceVersions instance = new MicroserviceVersions(appManager, appId, microserviceName);
      instance.pullInstances();
      return instance;
    });

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
}
