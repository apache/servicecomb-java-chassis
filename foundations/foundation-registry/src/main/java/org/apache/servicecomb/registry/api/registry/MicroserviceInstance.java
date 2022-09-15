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

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.registry.config.InstancePropertiesLoader;
import org.apache.servicecomb.registry.definition.DefinitionConst;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.netflix.config.DynamicPropertyFactory;

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
  public static MicroserviceInstance createFromDefinition(Configuration configuration) {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    // default hard coded values
    microserviceInstance.setStage(DefinitionConst.DEFAULT_STAGE);
    microserviceInstance.setStatus(MicroserviceInstanceStatus
        .valueOf(BootStrapProperties.readServiceInstanceInitialStatus()));
    HealthCheck healthCheck = new HealthCheck();
    healthCheck.setMode(HealthCheckMode.HEARTBEAT);
    microserviceInstance.setHealthCheck(healthCheck);

    // load properties
    Map<String, String> propertiesMap = InstancePropertiesLoader.INSTANCE.loadProperties(configuration);
    microserviceInstance.setProperties(propertiesMap);

    // load data center information
    loadDataCenterInfo(microserviceInstance);
    return microserviceInstance;
  }

  private static void loadDataCenterInfo(MicroserviceInstance microserviceInstance) {
    String dataCenterName = DynamicPropertyFactory.getInstance()
        .getStringProperty("servicecomb.datacenter.name", "default")
        .get();
    DataCenterInfo dataCenterInfo = new DataCenterInfo();
    dataCenterInfo.setName(dataCenterName);
    dataCenterInfo.setRegion(DynamicPropertyFactory.getInstance().
        getStringProperty("servicecomb.datacenter.region", "default").get());
    dataCenterInfo.setAvailableZone(DynamicPropertyFactory.getInstance().
        getStringProperty("servicecomb.datacenter.availableZone", "default").get());
    microserviceInstance.setDataCenterInfo(dataCenterInfo);
  }
}
