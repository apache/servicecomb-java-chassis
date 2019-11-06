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

package org.apache.servicecomb.service.center.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("service")
public class Microservice {

  private String serviceId;

  private Framework framework;

  private String registerBy;

  private String environment;

  private String appId;

  private String serviceName;

  /**
   * for governance
   * when invoke cross app, if not use alias name, then {microservice}:{schema}:{operation} will conflict
   */
  private String alias;

  private String version;

  private String description;

  private String level;

  private List<String> schemas = new ArrayList<String>();

  private List<BasePath> paths = new ArrayList<>();

  private MicroserviceStatus status = MicroserviceStatus.UP;

  private Map<String, String> properties = new HashMap<String, String>();

  private List<MicroserviceInstance> instances;

  private String timestamp;

  private String modTimestamp;

  public Microservice() {

  }

  public Microservice(String serviceName) {
    this.serviceName = serviceName;
  }

  public List<MicroserviceInstance> getInstances() {
    return instances;
  }

  public void setInstances(List<MicroserviceInstance> instances) {
    this.instances = instances;
  }

  public Framework getFramework() {
    return framework;
  }

  public void setFramework(Framework framework) {
    this.framework = framework;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
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

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
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

  public MicroserviceStatus getStatus() {
    return status;
  }

  public void setStatus(MicroserviceStatus status) {
    this.status = status;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getModTimestamp() {
    return modTimestamp;
  }

  public void setModTimestamp(String modTimestamp) {
    this.modTimestamp = modTimestamp;
  }

  public List<BasePath> getPaths() {
    return paths;
  }

  public void setPaths(List<BasePath> paths) {
    this.paths = paths;
  }
}
