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

package org.apache.servicecomb.registry.lightweight;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.lightweight.store.InstanceStore;
import org.apache.servicecomb.registry.lightweight.store.MicroserviceStore;
import org.apache.servicecomb.registry.lightweight.store.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;

@Component
public class RegistryServerService {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegistryServerService.class);

  private final EventBus eventBus;

  private final Store store;

  private final DiscoveryClient discoveryClient;

  public RegistryServerService(EventBus eventBus, Store store, DiscoveryClient discoveryClient) {
    this.store = store;
    this.discoveryClient = discoveryClient;
    this.eventBus = eventBus;
  }

  public InstanceStore register(RegisterRequest request) {
    return AsyncUtils.toSync(registerAsync(request));
  }

  public CompletableFuture<InstanceStore> registerAsync(RegisterRequest request) {
    return doRegisterAsync(request)
        .whenComplete((r, e) -> logFailedRegister(request, e));
  }

  private void logFailedRegister(RegisterRequest request, Throwable throwable) {
    if (throwable == null) {
      return;
    }

    LOGGER.error("register instance failed, serviceId={}, instanceId={}, endpoints={}, message={}.",
        request.getServiceId(),
        request.getInstanceId(),
        request.getEndpoints(),
        throwable.getMessage());
  }

  private CompletableFuture<InstanceStore> doRegisterAsync(RegisterRequest request) {
    InstanceStore instanceStore = store.findInstanceStore(request.getInstanceId());
    if (instanceStore == null) {
      return addInstance(request);
    }

    if (instanceStore.isStatusChanged(request.getStatus())) {
      updateInstanceStatus(request, instanceStore);
    }
    return heartbeat(instanceStore);
  }

  private CompletableFuture<InstanceStore> addInstance(RegisterRequest request) {
    Endpoint endpoint = request.selectFirstEndpoint();
    if (endpoint == null) {
      return AsyncUtils.completeExceptionally(new RegisterException("can not select endpoint"));
    }

    MicroserviceStore microserviceStore = this.store.findMicroserviceStore(request.getServiceId());
    if (microserviceStore == null) {
      return addMicroserviceAndInstance(endpoint, request);
    }

    return checkSchemaSummary(request, microserviceStore)
        .thenCompose(v -> discoveryClient.getInstanceAsync(endpoint))
        .thenApply(instance -> addInstance(microserviceStore, instance));
  }

  private CompletableFuture<InstanceStore> addMicroserviceAndInstance(Endpoint endpoint, RegisterRequest request) {
    return discoveryClient.getInfoAsync(endpoint)
        .thenApply(info -> {
          info.getMicroservice().getSchemaMap().putAll(info.getSchemasById());
          MicroserviceStore microserviceStore = store
              .addMicroservice(info.getMicroservice(), request.getSchemasSummary());
          InstanceStore instanceStore = doAddInstance(microserviceStore, info.getInstance());

          LOGGER.info("add microservice and instance, serviceId={}, instanceId={}, endpoints={}",
              request.getServiceId(),
              request.getInstanceId(),
              request.getEndpoints());
          return instanceStore;
        });
  }

  private InstanceStore addInstance(MicroserviceStore microserviceStore, MicroserviceInstance instance) {
    LOGGER.info("add instance, serviceId={}, instanceId={}, endpoints={}",
        instance.getServiceId(),
        instance.getInstanceId(),
        instance.getEndpoints());

    return doAddInstance(microserviceStore, instance);
  }

  private InstanceStore doAddInstance(MicroserviceStore microserviceStore, MicroserviceInstance instance) {
    InstanceStore instanceStore = store.addInstance(microserviceStore, instance);
    eventBus.post(new RegisterInstanceEvent(instance));
    return instanceStore;
  }

  private CompletableFuture<Void> checkSchemaSummary(RegisterRequest request, MicroserviceStore microserviceStore) {
    if (!microserviceStore.isSchemaChanged(request.getSchemasSummary())) {
      return CompletableFuture.completedFuture(null);
    }

    if (microserviceStore.hasInstance()) {
      String message = String.format("schemas changed, but version not changed, and has %d existing instances",
          microserviceStore.getInstanceCount());
      return AsyncUtils.completeExceptionally(new RegisterException(message));
    }

    microserviceStore.setSchemasSummary(request.getSchemasSummary());
    eventBus.post(new SchemaChangedEvent(microserviceStore.getMicroservice()));
    LOGGER.warn(
        "schemas changed, but version not changed, only allow in dev stage, serviceId={}, instanceId={}, endpoints={}",
        request.getServiceId(),
        request.getInstanceId(),
        request.getEndpoints());
    return CompletableFuture.completedFuture(null);
  }

  private void updateInstanceStatus(RegisterRequest request, InstanceStore instanceStore) {
    LOGGER.info("update instance status, old status={}, new status={}, serviceId={}, instanceId={}, endpoints={}",
        instanceStore.getStatus(),
        request.getStatus(),
        instanceStore.getServiceId(),
        instanceStore.getInstanceId(),
        instanceStore.getEndpoints());

    store.findMicroserviceStore(instanceStore.getServiceId())
        .updateInstanceStatus(instanceStore, request.getStatus());
  }

  private CompletableFuture<InstanceStore> heartbeat(InstanceStore instanceStore) {
    instanceStore.updateLastHeartBeat();

    LOGGER.debug("instance heartbeat, serviceId={}, instanceId={}, endpoints={}",
        instanceStore.getServiceId(),
        instanceStore.getInstanceId(),
        instanceStore.getEndpoints());

    return CompletableFuture.completedFuture(instanceStore);
  }

  public Void unregister(UnregisterRequest request) {
    return AsyncUtils.toSync(unregisterAsync(request));
  }

  public CompletableFuture<Void> unregisterAsync(UnregisterRequest request) {
    deleteInstance("unregister", request.getServiceId(), request.getInstanceId());
    return CompletableFuture.completedFuture(null);
  }

  public void deleteInstance(String action, String serviceId, String instanceId) {
    InstanceStore instanceStore = store.deleteInstance(serviceId, instanceId);
    if (instanceStore == null) {
      return;
    }

    LOGGER.info("{} instance, serviceId={}, instanceId={}, endpoints={}",
        action,
        instanceStore.getServiceId(),
        instanceStore.getInstanceId(),
        instanceStore.getEndpoints());
  }

  public void deleteDeadInstances(Duration timeout) {
    store.findDeadInstances(timeout)
        .forEach(instance -> deleteInstance("delete dead", instance.getServiceId(), instance.getInstanceId()));
  }
}
