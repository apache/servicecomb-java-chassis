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

package org.apache.servicecomb.registry.lightweight.store;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.springframework.stereotype.Component;

import com.google.common.base.Ticker;

@Component
public class Store {
  private Ticker ticker = Ticker.systemTicker();

  private final Map<String, AppStore> appsById = new ConcurrentHashMapEx<>();

  private final Map<String, MicroserviceStore> microservicesByName = new ConcurrentHashMapEx<>();

  private final Map<String, MicroserviceStore> microservicesById = new ConcurrentHashMapEx<>();

  private final Map<String, InstanceStore> instancesById = new ConcurrentHashMapEx<>();

  public Store setTicker(Ticker ticker) {
    this.ticker = ticker;
    return this;
  }

  public AppStore getOrCreateAppStore(String appId) {
    return appsById.computeIfAbsent(appId, key -> new AppStore());
  }

  public MicroserviceStore findMicroserviceStore(String serviceId) {
    return microservicesById.get(serviceId);
  }

  public InstanceStore findInstanceStore(String instanceId) {
    return instancesById.get(instanceId);
  }

  public MicroserviceStore addMicroservice(Microservice microservice, String schemasSummary) {
    MicroserviceStore microserviceStore = new MicroserviceStore(ticker, microservice, schemasSummary);

    getOrCreateAppStore(microservice.getAppId())
        .addMicroservice(microserviceStore);
    microservicesById.put(microservice.getServiceId(), microserviceStore);
    microservicesByName.put(microservice.getServiceName(), microserviceStore);

    return microserviceStore;
  }

  public InstanceStore addInstance(MicroserviceStore microserviceStore, MicroserviceInstance instance) {
    InstanceStore instanceStore = microserviceStore.addInstance(instance);
    instancesById.put(instance.getInstanceId(), instanceStore);
    return instanceStore;
  }

  public InstanceStore deleteInstance(String serviceId, String instanceId) {
    MicroserviceStore microserviceStore = findMicroserviceStore(serviceId);
    if (microserviceStore == null) {
      return null;
    }

    InstanceStore instanceStore = microserviceStore.deleteInstance(instanceId);
    if (instanceStore != null) {
      instancesById.remove(instanceId);
    }
    return instanceStore;
  }

  public Optional<Microservice> getMicroservice(String microserviceId) {
    return Optional.ofNullable(microservicesById.get(microserviceId))
        .map(MicroserviceStore::getMicroservice);
  }

  public Optional<MicroserviceInstance> getMicroserviceInstance(String instanceId) {
    return Optional.ofNullable(instancesById.get(instanceId))
        .map(InstanceStore::getInstance);
  }

  public MicroserviceInstances findServiceInstances(String appId, String serviceName, String revision) {
    return Optional.ofNullable(appsById.get(appId))
        .map(appStore -> appStore.findServiceInstances(serviceName, revision))
        .orElse(new MicroserviceInstances()
            .setMicroserviceNotExist(true));
  }

  public List<Microservice> getAllMicroservices() {
    return microservicesByName.values().stream()
        .map(MicroserviceStore::getMicroservice)
        .collect(Collectors.toList());
  }

  public Stream<MicroserviceInstance> findDeadInstances(Duration timeout) {
    long nanoNow = ticker.read();
    long nanoTimeout = timeout.toNanos();
    return instancesById.values().stream()
        .filter(instanceStore -> instanceStore.isHeartBeatTimeout(nanoNow, nanoTimeout))
        .map(InstanceStore::getInstance);
  }
}