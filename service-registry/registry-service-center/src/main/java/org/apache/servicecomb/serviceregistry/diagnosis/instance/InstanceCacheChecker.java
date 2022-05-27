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
package org.apache.servicecomb.serviceregistry.diagnosis.instance;

import java.time.Clock;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.servicecomb.foundation.common.utils.TimeUtils;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.registry.consumer.AppManager;
import org.apache.servicecomb.registry.consumer.MicroserviceManager;
import org.apache.servicecomb.registry.consumer.MicroserviceVersions;
import org.apache.servicecomb.registry.consumer.StaticMicroserviceVersions;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.diagnosis.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.Json;

public class InstanceCacheChecker {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceCacheChecker.class);

  Clock clock = TimeUtils.getSystemDefaultZoneClock();

  private final AppManager appManager;

  private final Set<Status> statuses = new HashSet<>();

  private final InstanceCacheSummary instanceCacheSummary = new InstanceCacheSummary();

  public InstanceCacheChecker(AppManager appManager) {
    this.appManager = appManager;
  }

  public InstanceCacheSummary check() {
    instanceCacheSummary.setAppId(RegistryUtils.getMicroservice().getAppId());
    instanceCacheSummary.setMicroserviceName(RegistryUtils.getMicroservice().getServiceName());
    instanceCacheSummary.setTimestamp(clock.millis());

    for (MicroserviceManager microserviceManager : appManager.getApps().values()) {
      for (MicroserviceVersions microserviceVersions : microserviceManager.getVersionsByName().values()) {
        if (microserviceVersions instanceof StaticMicroserviceVersions) {
          continue;
        }
        InstanceCacheResult instanceCacheResult = check(microserviceVersions);
        addInstanceCacheResult(instanceCacheResult);
      }
    }

    generateStatus();

    return instanceCacheSummary;
  }

  private void addInstanceCacheResult(InstanceCacheResult instanceCacheResult) {
    statuses.add(instanceCacheResult.getStatus());
    instanceCacheSummary.getProducers().add(instanceCacheResult);
  }

  protected InstanceCacheResult check(MicroserviceVersions microserviceVersions) {
    InstanceCacheResult instanceCacheResult = new InstanceCacheResult();
    instanceCacheResult.setAppId(microserviceVersions.getAppId());
    instanceCacheResult.setMicroserviceName(microserviceVersions.getMicroserviceName());
    instanceCacheResult.setPulledInstances(microserviceVersions.getPulledInstances());

    MicroserviceInstances microserviceInstances = RegistryUtils
        .findServiceInstances(microserviceVersions.getAppId(),
            microserviceVersions.getMicroserviceName(),
            DefinitionConst.VERSION_RULE_ALL);
    if (microserviceInstances == null) {
      instanceCacheResult.setStatus(Status.UNKNOWN);
      instanceCacheResult.setDetail("failed to find instances from service center");
      return instanceCacheResult;
    }
    if (microserviceInstances.isMicroserviceNotExist()) {
      // no problem, will be deleted from MicroserviceManager in next pull
      instanceCacheResult.setStatus(Status.UNKNOWN);
      instanceCacheResult.setDetail("microservice is not exist anymore, will be deleted from memory in next pull");
      return instanceCacheResult;
    }

    if (!Objects.equals(microserviceInstances.getRevision(), microserviceVersions.getRevision())) {
      // maybe not pull, wait for next pull we get the same revision
      instanceCacheResult.setStatus(Status.UNKNOWN);
      instanceCacheResult.setDetail(String.format(
          "revision is different, will be synchronized in next pull. local revision=%s, remote revision=%s",
          microserviceVersions.getRevision(), microserviceInstances.getRevision()));
      // better to change revision and more likely to find the correct instances in next pull.
      microserviceVersions.setRevision(null);
      return instanceCacheResult;
    }

    // compare all instances
    List<MicroserviceInstance> remoteInstances = microserviceInstances.getInstancesResponse().getInstances();
    remoteInstances.sort(Comparator.comparing(MicroserviceInstance::getInstanceId));
    String local = Json.encode(microserviceVersions.getPulledInstances());
    String remote = Json.encode(remoteInstances);
    if (local.equals(remote)) {
      LOGGER.info("instance cache match. appId={}, microservice={}.\n"
              + "current cache: {}\n",
          microserviceVersions.getAppId(),
          microserviceVersions.getMicroserviceName(),
              remoteInstances);
      instanceCacheResult.setStatus(Status.NORMAL);
      return instanceCacheResult;
    }

    LOGGER.error("instance cache not match. appId={}, microservice={}.\n"
            + "local cache: {}\n"
            + "remote cache: {}",
        microserviceVersions.getAppId(),
        microserviceVersions.getMicroserviceName(),
        microserviceVersions.getPulledInstances().toString(),
            remoteInstances);
    instanceCacheResult.setStatus(Status.ABNORMAL);
    instanceCacheResult.setDetail("instance cache not match");

    // auto fix, will do a full pull request when invoke MicroserviceVersions.pullInstances
    microserviceVersions.setRevision(null);

    return instanceCacheResult;
  }

  protected void generateStatus() {
    if (statuses.contains(Status.ABNORMAL)) {
      instanceCacheSummary.setStatus(Status.ABNORMAL);
    } else if (statuses.contains(Status.UNKNOWN)) {
      instanceCacheSummary.setStatus(Status.UNKNOWN);
    } else {
      instanceCacheSummary.setStatus(Status.NORMAL);
    }
  }
}
