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
package org.apache.servicecomb.registry.sc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.RegistrationInstance;
import org.apache.servicecomb.service.center.client.ServiceCenterRegistration;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;

public class SCRegistrationInstance implements RegistrationInstance {
  private final Microservice microservice;

  private final MicroserviceInstance microserviceInstance;

  private final ServiceCenterRegistration serviceCenterRegistration;

  public SCRegistrationInstance(Microservice microservice,
      MicroserviceInstance microserviceInstance,
      ServiceCenterRegistration serviceCenterRegistration) {
    this.microservice = microservice;
    this.microserviceInstance = microserviceInstance;
    this.serviceCenterRegistration = serviceCenterRegistration;
  }

  @Override
  public String getEnvironment() {
    return microservice.getEnvironment();
  }

  @Override
  public String getApplication() {
    return microservice.getAppId();
  }

  @Override
  public String getServiceName() {
    return microservice.getServiceName();
  }

  @Override
  public String getAlias() {
    return microservice.getAlias();
  }

  @Override
  public String getVersion() {
    return microservice.getVersion();
  }

  @Override
  public DataCenterInfo getDataCenterInfo() {
    return new DataCenterInfo(microserviceInstance.getDataCenterInfo().getName(),
        microserviceInstance.getDataCenterInfo().getRegion(),
        microserviceInstance.getDataCenterInfo().getAvailableZone());
  }

  @Override
  public String getDescription() {
    return microservice.getDescription();
  }

  @Override
  public Map<String, String> getProperties() {
    return microserviceInstance.getProperties();
  }

  @Override
  public Map<String, String> getSchemas() {
    Map<String, String> result = new HashMap<>();
    serviceCenterRegistration.getSchemaInfos().forEach(info -> result.put(info.getSchemaId(), info.getSchema()));
    return result;
  }

  @Override
  public List<String> getEndpoints() {
    return microserviceInstance.getEndpoints();
  }

  @Override
  public String getInstanceId() {
    return microserviceInstance.getInstanceId();
  }

  @Override
  public MicroserviceInstanceStatus getInitialStatus() {
    return MicroserviceInstanceStatus.STARTING;
  }

  @Override
  public MicroserviceInstanceStatus getReadyStatus() {
    return MicroserviceInstanceStatus.UP;
  }

  public Microservice getBackendMicroservice() {
    return microservice;
  }

  public MicroserviceInstance getBackendMicroserviceInstance() {
    return microserviceInstance;
  }
}
