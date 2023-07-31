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

import java.util.List;
import java.util.Map;

import org.apache.servicecomb.registry.api.AbstractDiscoveryInstance;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.lightweight.model.Microservice;
import org.apache.servicecomb.registry.lightweight.model.MicroserviceInstance;

public class ZeroConfigDiscoveryInstance extends AbstractDiscoveryInstance {
  private final Microservice microservice;

  private final MicroserviceInstance microserviceInstance;

  public ZeroConfigDiscoveryInstance(Microservice microservice,
      MicroserviceInstance microserviceInstance) {
    this.microservice = microservice;
    this.microserviceInstance = microserviceInstance;
  }

  @Override
  public MicroserviceInstanceStatus getStatus() {
    return microserviceInstance.getStatus();
  }

  @Override
  public String getDiscoveryName() {
    return AbstractLightweightDiscovery.ZERO_CONFIG_NAME;
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
    return microserviceInstance.getDataCenterInfo();
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
    return microservice.getSchemaMap();
  }

  @Override
  public List<String> getEndpoints() {
    return microserviceInstance.getEndpoints();
  }

  @Override
  public String getInstanceId() {
    return microserviceInstance.getInstanceId();
  }
}
