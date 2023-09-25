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

package org.apache.servicecomb.registry.lightweight.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by   on 2016/12/5.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroserviceInstance {
  // even disconnected from service center
  // instanceId will not be changed
  // when register to service center again, use the old instanceId.
  private String instanceId;

  // service center rule: max length: 64
  private String serviceId;

  private List<String> endpoints = new ArrayList<>();

  private String hostName;

  private MicroserviceInstanceStatus status = MicroserviceInstanceStatus.UP;

  private Map<String, String> properties = new HashMap<>(); // reserved key list: region|az|stage|group

  private HealthCheck healthCheck;

  private String stage;

  private DataCenterInfo dataCenterInfo;

  private String timestamp;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("instanceId=" + instanceId + ";");
    sb.append("serviceId=" + serviceId + ";");
    sb.append("status=" + status + ";");
    sb.append("endpoints=" + endpoints.toString());
    return sb.toString();
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public MicroserviceInstance instanceId(String instanceId) {
    this.instanceId = instanceId;
    return this;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public MicroserviceInstance serviceId(String serviceId) {
    this.serviceId = serviceId;
    return this;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public List<String> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(List<String> endpoints) {
    this.endpoints = endpoints;
  }

  public MicroserviceInstanceStatus getStatus() {
    return status;
  }

  public void setStatus(MicroserviceInstanceStatus status) {
    this.status = status;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public HealthCheck getHealthCheck() {
    return healthCheck;
  }

  public void setHealthCheck(HealthCheck healthCheck) {
    this.healthCheck = healthCheck;
  }

  @Deprecated
  public String getStage() {
    return stage;
  }

  @Deprecated
  public void setStage(String stage) {
    this.stage = stage;
  }

  public DataCenterInfo getDataCenterInfo() {
    return dataCenterInfo;
  }

  @Override
  public int hashCode() {
    return this.instanceId.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MicroserviceInstance) {
      return this.instanceId.equals(((MicroserviceInstance) obj).instanceId);
    }
    return false;
  }

  public void setDataCenterInfo(DataCenterInfo dataCenterInfo) {
    this.dataCenterInfo = dataCenterInfo;
  }

  // Some properties of microservice instance are dynamic changed, not cover them all now.
  public static MicroserviceInstance createFromDefinition(Environment environment) {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    // default hard coded values
    microserviceInstance.setStage(DefinitionConst.DEFAULT_STAGE);
    microserviceInstance.setStatus(MicroserviceInstanceStatus
        .valueOf(BootStrapProperties.readServiceInstanceInitialStatus(environment)));
    HealthCheck healthCheck = new HealthCheck();
    healthCheck.setMode(HealthCheckMode.HEARTBEAT);
    microserviceInstance.setHealthCheck(healthCheck);

    // load properties
    Map<String, String> propertiesMap = InstancePropertiesLoader.INSTANCE.loadProperties(environment);
    microserviceInstance.setProperties(propertiesMap);

    // load data center information
    loadDataCenterInfo(environment, microserviceInstance);
    return microserviceInstance;
  }

  private static void loadDataCenterInfo(Environment environment, MicroserviceInstance microserviceInstance) {
    String dataCenterName = environment.getProperty("servicecomb.datacenter.name");
    if (StringUtils.isEmpty(dataCenterName)) {
      return;
    }
    String region = environment.getProperty("servicecomb.datacenter.region");
    String availableZone = environment.getProperty("servicecomb.datacenter.availableZone");
    DataCenterInfo dataCenterInfo = new DataCenterInfo();
    dataCenterInfo.setName(dataCenterName);
    dataCenterInfo.setRegion(region);
    dataCenterInfo.setAvailableZone(availableZone);
    microserviceInstance.setDataCenterInfo(dataCenterInfo);
  }
}
