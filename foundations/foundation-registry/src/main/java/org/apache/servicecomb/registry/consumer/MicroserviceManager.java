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
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.vertx.executor.SinglePoolBlockingExecutor;
import org.apache.servicecomb.registry.api.event.MicroserviceInstanceChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;

public class MicroserviceManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceManager.class);

  private final AppManager appManager;

  private final String appId;

  // key: microserviceName
  private final Map<String, MicroserviceVersions> versionsByName = new ConcurrentHashMapEx<>();

  private final Object lock = new Object();

  public MicroserviceManager(AppManager appManager, String appId) {
    this.appManager = appManager;
    this.appId = appId;
  }

  public Map<String, MicroserviceVersions> getVersionsByName() {
    return versionsByName;
  }

  /**
   * update instance information triggered by first timeout pull
   */
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

  public CompletableFuture<MicroserviceVersions> getOrCreateMicroserviceVersionsAsync(String microserviceName) {
    MicroserviceVersions microserviceVersions = versionsByName.get(microserviceName);
    if (microserviceVersions == null) {
      if (Vertx.currentContext() == null) {
        // not in event-loop, execute in current thread
        return CompletableFuture.completedFuture(getOrCreateMicroserviceVersions(microserviceName));
      } else {
        // execute in an single thread pool to make sure make less requests to service center
        return CompletableFuture.supplyAsync(() -> getOrCreateMicroserviceVersions(microserviceName),
            SinglePoolBlockingExecutor.create());
      }
    } else {
      // here do not need switch to another thread, can improve performance
      tryRemoveInvalidMicroservice(microserviceVersions);
      return CompletableFuture.completedFuture(microserviceVersions);
    }
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

  /**
   * update instance information triggered by scheduler
   */
  public void pullInstances() {
    synchronized (lock) {
      for (MicroserviceVersions microserviceVersions : versionsByName.values()) {
        microserviceVersions.pullInstances();

        tryRemoveInvalidMicroservice(microserviceVersions);
      }
    }
  }

  /**
   * update instance information triggered by event
   */
  public void onMicroserviceInstanceChanged(MicroserviceInstanceChangedEvent changedEvent) {
    synchronized (lock) {
      for (MicroserviceVersions microserviceVersions : versionsByName.values()) {
        microserviceVersions.onMicroserviceInstanceChanged(changedEvent);

        tryRemoveInvalidMicroservice(microserviceVersions);
      }
    }
  }
}
