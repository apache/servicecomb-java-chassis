/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.api.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.servicecomb.serviceregistry.api.Const;

/**
 * Created by on 2016/12/5.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Microservice {
  private String serviceId;

  private Map<String, String> framework = new HashMap<>();

  @JsonProperty(value = "registerBy")
  private String registeredBy;

  private String appId;

  private String serviceName;

  private String alias;

  private String version;

  private String description;

  private String level;

  private List<String> schemas = new ArrayList<>();

  @JsonIgnore
  private Map<String, String> schemaMap = new HashMap<>();

  private List<BasePath> paths = new ArrayList<>();

  private MicroserviceStatus status = MicroserviceStatus.UP;

  private Map<String, String> properties = new HashMap<>();

  @JsonIgnore
  private MicroserviceInstance intance;

  public Microservice() {
  }

  public MicroserviceInstance getIntance() {
    return intance;
  }

  public void setIntance(MicroserviceInstance intance) {
    this.intance = intance;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public List<String> getSchemas() {
    return schemas;
  }

  public void setSchemas(List<String> schemas) {
    this.schemas = schemas;
  }

  public void addSchema(String schemaId, String content) {
    this.schemaMap.put(schemaId, content);
    schemas.add(schemaId);
  }

  public Map<String, String> getSchemaMap() {
    return schemaMap;
  }

  public String getStatus() {
    return status.toString();
  }

  public void setStatus(String status) {
    this.status = MicroserviceStatus.valueOf(status);
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public static String generateAbsoluteMicroserviceName(String appId, String microserviceName) {
    StringBuilder sb = new StringBuilder(appId.length() + microserviceName.length() + 1);
    sb.append(appId).append(Const.APP_SERVICE_SEPARATOR).append(microserviceName);
    return sb.toString();
  }

  public List<BasePath> getPaths() {
    return paths;
  }

  public void setPaths(List<BasePath> paths) {
    this.paths = paths;
  }

  public Map<String, String> getFramework() {
    return framework;
  }

  public void setFramework(Map<String, String> framework) {
    this.framework = framework;
  }

  public String getRegisteredBy() {
    return registeredBy;
  }

  public void setRegisteredBy(String registeredBy) {
    this.registeredBy = registeredBy;
  }

}
