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
import org.apache.servicecomb.serviceregistry.task.event.MicroserviceNotExistEvent;
import org.apache.servicecomb.serviceregistry.task.event.PeriodicPullEvent;
import org.apache.servicecomb.serviceregistry.task.event.RecoveryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class MicroserviceManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceManager.class);

  private AppManager appManager;

  private String appId;

  // key: microserviceName
  private Map<String, MicroserviceVersions> versionsByName = new ConcurrentHashMapEx<>();

  public MicroserviceManager(AppManager appManager, String appId) {
    this.appManager = appManager;
    this.appId = appId;

    appManager.getEventBus().register(this);
  }

  public Map<String, MicroserviceVersions> getVersionsByName() {
    return versionsByName;
  }

  public MicroserviceVersions getOrCreateMicroserviceVersions(String microserviceName) {
    MicroserviceVersions microserviceVersions = versionsByName.computeIfAbsent(microserviceName, name -> {
      MicroserviceVersions instance = new MicroserviceVersions(appManager, appId, microserviceName);
      instance.submitPull();
      return instance;
    });
    if (!microserviceVersions.isValidated()) {
      // remove this microservice if it does not exist or not registered in order to get it back when access it again
      removeMicroservice(microserviceName);
    }
    return microserviceVersions;
  }

  protected void removeMicroservice(String microserviceName) {
    // must use containsKey and then remove
    // because removeMicroservice maybe invoked inside "versionsByName.computeIfAbsent"
    //  in this time, containsKey will return false, and will not invoke remove
    // otherwise, remove will block the thread forever
    if (versionsByName.containsKey(microserviceName)) {
      MicroserviceVersions microserviceVersions = versionsByName.remove(microserviceName);
      appManager.getEventBus().unregister(microserviceVersions);
      LOGGER.info("remove microservice, appId={}, microserviceName={}.", appId, microserviceName);
    }
  }

  public MicroserviceVersionRule getOrCreateMicroserviceVersionRule(String microserviceName,
      String versionRule) {
    MicroserviceVersions microserviceVersions = getOrCreateMicroserviceVersions(microserviceName);

    return microserviceVersions.getOrCreateMicroserviceVersionRule(versionRule);
  }

  @Subscribe
  public void periodicPull(PeriodicPullEvent event) {
    refreshInstances();
  }

  @Subscribe
  public void serviceRegistryRecovery(RecoveryEvent event) {
    refreshInstances();
  }

  protected void refreshInstances() {
    for (MicroserviceVersions versions : versionsByName.values()) {
      versions.submitPull();
    }
  }

  @Subscribe
  private void onMicroserviceNotExistEvent(MicroserviceNotExistEvent event) {
    if (!appId.equals(event.getAppId())) {
      return;
    }

    removeMicroservice(event.getMicroserviceName());
  }
}
