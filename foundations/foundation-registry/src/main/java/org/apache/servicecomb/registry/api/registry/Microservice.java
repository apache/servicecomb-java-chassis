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

package org.apache.servicecomb.registry.api.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.registry.definition.DefinitionConst;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by on 2016/12/5.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Microservice {
  // service center rule: max length: 64
  // two way to generate service id
  // 1. microservice instance generate by some rule, ex: env/app/name/version
  //    and then register to service center with the id
  // 2. register to service center with the id to be null, and then service center generate by UUID
  private String serviceId;

  private Framework framework;

  private String registerBy;

  private String environment;

  // service center rule: max length: 160
  private String appId;

  // service center rule: max length: 128
  private String serviceName;

  /**
   * for governance
   * when invoke cross app, if not use alias name, then {microservice}.{schema}.{operation} will conflict
   */
  private String alias;

  private String version;

  private String description;

  private String level;

  private List<String> schemas = new ArrayList<>();

  @JsonIgnore
  private final Map<String, String> schemaMap = new HashMap<>();

  private List<BasePath> paths = new ArrayList<>();

  private MicroserviceStatus status = MicroserviceStatus.UP;

  private Map<String, String> properties = new HashMap<>();

  @JsonIgnore
  private MicroserviceInstance instance;

  /**
   * Currently this field only exists in ServiceComb-Java-Chassis,
   * and ServiceComb-Service-Center does not hold this field.
   * Once the 3rd party services are supported to be registered into ServiceComb-Service-Center,
   * the corresponding field should be added into Service-Center.
   */
  private boolean thirdPartyService;

  public Microservice() {
  }

  public MicroserviceInstance getInstance() {
    return instance;
  }

  public void setInstance(MicroserviceInstance instance) {
    this.instance = instance;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public Microservice serviceId(String serviceId) {
    this.serviceId = serviceId;
    return this;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public Microservice appId(String appId) {
    this.appId = appId;
    return this;
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

  public Microservice serviceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Microservice version(String version) {
    this.version = version;
    return this;
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
    sb.append(appId).append(DefinitionConst.APP_SERVICE_SEPARATOR).append(microserviceName);
    return sb.toString();
  }

  public List<BasePath> getPaths() {
    return paths;
  }

  public void setPaths(List<BasePath> paths) {
    this.paths = paths;
  }

  public Framework getFramework() {
    return framework;
  }

  public void setFramework(Framework framework) {
    this.framework = framework;
  }

  public String getRegisterBy() {
    return registerBy;
  }

  public void setRegisterBy(String registerBy) {
    this.registerBy = registerBy;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public boolean isThirdPartyService() {
    return thirdPartyService;
  }

  public void setThirdPartyService(boolean thirdPartyService) {
    this.thirdPartyService = thirdPartyService;
  }

  // Whether to allow cross-app calls to me
  public boolean allowCrossApp() {
    return Boolean.parseBoolean(properties.get(DefinitionConst.CONFIG_ALLOW_CROSS_APP_KEY));
  }
}
