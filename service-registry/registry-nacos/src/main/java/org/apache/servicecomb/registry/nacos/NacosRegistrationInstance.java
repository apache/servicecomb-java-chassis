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

package org.apache.servicecomb.registry.nacos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.RegistrationInstance;
import org.springframework.core.env.Environment;

import com.alibaba.nacos.api.naming.pojo.Instance;

public class NacosRegistrationInstance implements RegistrationInstance {
  private final Instance instance;

  private final Map<String, String> schemas = new HashMap<>();

  private final List<String> endpoints = new ArrayList<>();

  private final Environment environment;

  public NacosRegistrationInstance(Instance instance,
      Environment environment) {
    this.instance = instance;
    this.environment = environment;
  }

  @Override
  public String getEnvironment() {
    return BootStrapProperties.readServiceEnvironment(environment);
  }

  @Override
  public String getApplication() {
    return BootStrapProperties.readApplication(environment);
  }

  @Override
  public String getServiceName() {
    return BootStrapProperties.readServiceName(environment);
  }

  @Override
  public String getAlias() {
    return BootStrapProperties.readServiceAlias(environment);
  }

  @Override
  public String getVersion() {
    return instance.getMetadata().get(NacosConst.PROPERTY_VERSION);
  }

  @Override
  public DataCenterInfo getDataCenterInfo() {
    DataCenterInfo dataCenterInfo = new DataCenterInfo();
    dataCenterInfo.setRegion(instance.getMetadata().get(NacosConst.PROPERTY_REGION));
    dataCenterInfo.setAvailableZone(instance.getMetadata().get(NacosConst.PROPERTY_ZONE));
    return dataCenterInfo;
  }

  @Override
  public String getDescription() {
    return BootStrapProperties.readServiceDescription(environment);
  }

  @Override
  public Map<String, String> getProperties() {
    return instance.getMetadata();
  }

  @Override
  public Map<String, String> getSchemas() {
    return schemas;
  }

  @Override
  public List<String> getEndpoints() {
    return endpoints;
  }

  @Override
  public String getInstanceId() {
    return instance.getInstanceId();
  }

  @Override
  public MicroserviceInstanceStatus getInitialStatus() {
    return MicroserviceInstanceStatus.STARTING;
  }

  @Override
  public MicroserviceInstanceStatus getReadyStatus() {
    return MicroserviceInstanceStatus.UP;
  }

  public void addSchema(String schemaId, String content) {
    this.schemas.put(schemaId, content);
  }

  public void addEndpoint(String endpoint) {
    this.endpoints.add(endpoint);
  }
}
