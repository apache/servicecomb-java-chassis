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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.registry.api.registry.FindInstancesResponse;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;

import com.google.common.base.Ticker;

public class MicroserviceStore {
  private final Ticker ticker;

  private final Microservice microservice;

  private final Map<String, InstanceStore> instancesById = new ConcurrentHashMapEx<>();

  private String schemasSummary;

  private String instancesRevision;

  public MicroserviceStore(Ticker ticker, Microservice microservice, String schemasSummary) {
    this.ticker = ticker;
    this.microservice = microservice;
    this.schemasSummary = schemasSummary;

    updateInstancesRevision();
  }

  public void updateInstancesRevision() {
    this.instancesRevision = String.valueOf(ticker.read());
  }

  public String getServiceName() {
    return microservice.getServiceName();
  }

  public String getServiceId() {
    return microservice.getServiceId();
  }

  public Microservice getMicroservice() {
    return microservice;
  }

  public InstanceStore addInstance(MicroserviceInstance instance) {
    InstanceStore instanceStore = new InstanceStore(ticker, instance);

    instancesById.put(instance.getInstanceId(), instanceStore);

    updateInstancesRevision();
    return instanceStore;
  }

  public InstanceStore deleteInstance(String instanceId) {
    InstanceStore instanceStore = instancesById.remove(instanceId);
    if (instanceStore != null) {
      updateInstancesRevision();
    }
    return instanceStore;
  }

  public void updateInstanceStatus(InstanceStore instanceStore, MicroserviceInstanceStatus status) {
    instanceStore.setStatus(status);
    updateInstancesRevision();
  }

  public MicroserviceInstances findServiceInstances(String revision) {
    if (instancesRevision.equals(revision)) {
      return new MicroserviceInstances()
          .setRevision(instancesRevision)
          .setNeedRefresh(false);
    }

    List<MicroserviceInstance> instances = instancesById.values().stream()
        .map(InstanceStore::getInstance)
        .collect(Collectors.toList());
    FindInstancesResponse response = new FindInstancesResponse()
        .setInstances(instances);
    return new MicroserviceInstances()
        .setRevision(instancesRevision)
        .setInstancesResponse(response);
  }

  public boolean isSchemaChanged(String schemasSummary) {
    return !Objects.equals(this.schemasSummary, schemasSummary);
  }

  public MicroserviceStore setSchemasSummary(String schemasSummary) {
    this.schemasSummary = schemasSummary;
    return this;
  }

  public boolean hasInstance() {
    return !instancesById.isEmpty();
  }

  public int getInstanceCount() {
    return instancesById.size();
  }
}
