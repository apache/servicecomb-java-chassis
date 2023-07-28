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

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.config.DataCenterProperties;
import org.apache.servicecomb.config.MicroserviceProperties;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.RegistrationInstance;

public class LocalRegistrationInstance implements RegistrationInstance {
  private final MicroserviceProperties microserviceProperties;

  private final DataCenterInfo dataCenterInfo;

  private final String instanceId;

  private final Map<String, String> schemas = new HashMap<>();

  private final List<String> endpoints = new ArrayList<>();

  private final Map<String, String> properties = new HashMap<>();

  public LocalRegistrationInstance(
      MicroserviceProperties microserviceProperties,
      DataCenterProperties dataCenterProperties) {
    this.microserviceProperties = microserviceProperties;

    this.dataCenterInfo = new DataCenterInfo();
    this.dataCenterInfo.setName(dataCenterProperties.getName());
    this.dataCenterInfo.setRegion(dataCenterProperties.getRegion());
    this.dataCenterInfo.setAvailableZone(dataCenterProperties.getAvailableZone());

    if (this.microserviceProperties.getProperties() != null) {
      this.properties.putAll(this.microserviceProperties.getProperties());
    }

    this.instanceId = System.currentTimeMillis() + "-" +
        ManagementFactory.getRuntimeMXBean().getPid();
  }

  @Override
  public String getEnvironment() {
    return microserviceProperties.getEnvironment();
  }

  @Override
  public String getApplication() {
    return microserviceProperties.getApplication();
  }

  @Override
  public String getServiceName() {
    return microserviceProperties.getName();
  }

  @Override
  public String getAlias() {
    return microserviceProperties.getAlias();
  }

  @Override
  public String getVersion() {
    return microserviceProperties.getVersion();
  }

  @Override
  public DataCenterInfo getDataCenterInfo() {
    return dataCenterInfo;
  }

  @Override
  public String getDescription() {
    return microserviceProperties.getDescription();
  }

  @Override
  public Map<String, String> getProperties() {
    return this.properties;
  }

  @Override
  public Map<String, String> getSchemas() {
    return this.schemas;
  }

  @Override
  public List<String> getEndpoints() {
    return this.endpoints;
  }

  @Override
  public String getInstanceId() {
    return this.instanceId;
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

  public void addProperty(String key, String value) {
    this.properties.put(key, value);
  }
}
