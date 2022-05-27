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

package org.apache.servicecomb.localregistry;

import java.util.Collection;
import java.util.List;

import org.apache.servicecomb.registry.api.Discovery;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;

import com.netflix.config.DynamicPropertyFactory;

public class LocalDiscovery implements Discovery {
  public static final String NAME = "local discovery";

  private final LocalRegistryStore localDiscoveryStore = LocalRegistryStore.INSTANCE;

  @Override
  public void init() {
    // done in registration
  }

  @Override
  public void run() {
    // done in registration
  }

  @Override
  public void destroy() {
    // done in registration
  }

  @Override
  public int getOrder() {
    return Const.LOCAL_ORDER;
  }

  @Override
  public Microservice getMicroservice(String microserviceId) {
    return localDiscoveryStore.getMicroservice(microserviceId);
  }

  @Override
  public List<Microservice> getAllMicroservices() {
    return localDiscoveryStore.getAllMicroservices();
  }

  @Override
  public String getSchema(String microserviceId, Collection<MicroserviceInstance> instances, String schemaId) {
    return localDiscoveryStore.getSchema(microserviceId, schemaId);
  }

  @Override
  public MicroserviceInstance getMicroserviceInstance(String serviceId, String instanceId) {
    return localDiscoveryStore.findMicroserviceInstance(serviceId, instanceId);
  }

  @Override
  public MicroserviceInstances findServiceInstances(String appId, String serviceName, String versionRule,
      String revision) {
    return localDiscoveryStore.findServiceInstances(appId, serviceName, versionRule, revision);
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty(Const.LOCAL_ENABLED, true).get();
  }
}
