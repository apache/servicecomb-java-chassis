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

package org.apache.servicecomb.serviceregistry;

import java.util.Collection;
import java.util.List;

import org.apache.servicecomb.registry.api.Discovery;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.serviceregistry.api.Const;

import com.netflix.config.DynamicPropertyFactory;

public class ServiceCenterDiscovery implements Discovery {
  public static final String NAME = "service center discovery";

  @Override
  public void init() {
    // ServiceCenterRegistration has already done it
  }

  @Override
  public void run() {
    // ServiceCenterRegistration has already done it
  }

  @Override
  public void destroy() {
    // ServiceCenterRegistration has already done it
  }

  @Override
  public int getOrder() {
    return Const.SERVICE_CENTER_ORDER;
  }

  @Override
  public Microservice getMicroservice(String microserviceId) {
    return RegistryUtils.getMicroservice(microserviceId);
  }

  @Override
  public List<Microservice> getAllMicroservices() {
    return RegistryUtils.getAllMicroservices();
  }

  @Override
  public String getSchema(String microserviceId, Collection<MicroserviceInstance> instances, String schemaId) {
    return RegistryUtils
        .getAggregatedSchema(microserviceId, schemaId);
  }

  @Override
  public MicroserviceInstance getMicroserviceInstance(String serviceId, String instanceId) {
    return RegistryUtils.getResultFromFirstValidServiceRegistry(
        sr -> sr.getServiceRegistryClient().findServiceInstance(serviceId, instanceId));
  }

  @Override
  public MicroserviceInstances findServiceInstances(String appId, String serviceName, String versionRule,
      String revision) {
    return RegistryUtils.findServiceInstances(appId,
        serviceName,
        versionRule);
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty(Const.SERVICE_CENTER_ENABLED, true).get();
  }
}
