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
package org.apache.servicecomb.registry.etcd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.MicroserviceInstance;

public class EtcdInstance implements MicroserviceInstance {
  private String serviceId;

  private String instanceId;

  private String environment;

  private String application;

  private String serviceName;

  private String alias;

  private String version;

  private String description;

  private DataCenterInfo dataCenterInfo;

  private List<String> endpoints = new ArrayList<>();

  private Map<String, String> schemas = new HashMap<>();

  private Map<String, String> properties = new HashMap<>();

  public EtcdInstance() {

  }

  public EtcdInstance(EtcdInstance other) {
    this.serviceId = other.serviceId;
    this.instanceId = other.instanceId;
    this.environment = other.environment;
    this.application = other.application;
    this.serviceName = other.serviceName;
    this.alias = other.alias;
    this.version = other.version;
    this.description = other.description;
    this.dataCenterInfo = other.dataCenterInfo;
    this.endpoints = other.endpoints;
    this.schemas = other.schemas;
    this.properties = other.properties;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setDataCenterInfo(DataCenterInfo dataCenterInfo) {
    this.dataCenterInfo = dataCenterInfo;
  }

  public void setEndpoints(List<String> endpoints) {
    this.endpoints = endpoints;
  }

  public void setSchemas(Map<String, String> schemas) {
    this.schemas = schemas;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  @Override
  public String getEnvironment() {
    return this.environment;
  }

  @Override
  public String getApplication() {
    return this.application;
  }

  @Override
  public String getServiceName() {
    return this.serviceName;
  }

  @Override
  public String getAlias() {
    return alias;
  }

  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public DataCenterInfo getDataCenterInfo() {
    return dataCenterInfo;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public Map<String, String> getSchemas() {
    return schemas;
  }

  @Override
  public List<String> getEndpoints() {
    return endpoints;
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

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public String getServiceId() {
    return serviceId;
  }

  @Override
  public String toString() {
    return "EtcdInstance{" +
        "serviceId='" + serviceId + '\'' +
        ", instanceId='" + instanceId + '\'' +
        ", environment='" + environment + '\'' +
        ", application='" + application + '\'' +
        ", serviceName='" + serviceName + '\'' +
        ", alias='" + alias + '\'' +
        ", version='" + version + '\'' +
        ", description='" + description + '\'' +
        ", dataCenterInfo=" + dataCenterInfo +
        ", endpoints=" + endpoints +
        ", schemas=" + schemas +
        ", properties=" + properties +
        '}';
  }
}
