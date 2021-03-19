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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.registry.api.Discovery;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.registry.consumer.AppManager;
import org.apache.servicecomb.registry.lightweight.store.MicroserviceStore;
import org.apache.servicecomb.registry.lightweight.store.Store;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractLightweightDiscovery implements Discovery, InitializingBean {
  protected EventBus eventBus;

  protected Store store;

  protected AppManager appManager;

  @Autowired
  public AbstractLightweightDiscovery setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
    return this;
  }

  @Autowired
  public AbstractLightweightDiscovery setStore(Store store) {
    this.store = store;
    return this;
  }

  @Autowired
  public AbstractLightweightDiscovery setAppManager(AppManager appManager) {
    this.appManager = appManager;
    return this;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    eventBus.register(this);
  }

  @SuppressWarnings("unused")
  @Subscribe
  public void onSchemaChanged(SchemaChangedEvent event) {
    Microservice microservice = event.getMicroservice();
    appManager.markWaitingDelete(microservice.getAppId(), microservice.getServiceName());
  }

  protected void startPullInstances(Duration pullInterval) {
    Executors
        .newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, name()))
        .scheduleAtFixedRate(appManager::safePullInstances, 0, pullInterval.getSeconds(), TimeUnit.SECONDS);
  }

  @Override
  public void init() {
  }

  @Override
  public void run() {
  }

  @Override
  public void destroy() {
  }

  @Override
  public Microservice getMicroservice(String microserviceId) {
    return store.getMicroservice(microserviceId)
        .orElse(null);
  }

  @Override
  public List<Microservice> getAllMicroservices() {
    return store.getAllMicroservices();
  }

  @Override
  public String getSchema(String microserviceId, Collection<MicroserviceInstance> instances, String schemaId) {
    return Optional.ofNullable(store.findMicroserviceStore(microserviceId))
        .map(MicroserviceStore::getMicroservice)
        .map(microservice -> microservice.getSchemaMap().get(schemaId))
        .orElse(null);
  }

  @Override
  public MicroserviceInstance getMicroserviceInstance(String serviceId, String instanceId) {
    return store.getMicroserviceInstance(instanceId)
        .orElse(null);
  }

  // ignore versionRule, instances only filter by consumer logic
  @Override
  public MicroserviceInstances findServiceInstances(String appId, String serviceName, String uselessVersionRule,
      String revision) {
    return store.findServiceInstances(appId, serviceName, revision);
  }

  @Override
  public String getRevision() {
    return null;
  }

  @Override
  public void setRevision(String revision) {
  }
}