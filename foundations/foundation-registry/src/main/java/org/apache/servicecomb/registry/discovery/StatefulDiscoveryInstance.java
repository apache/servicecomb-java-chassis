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
package org.apache.servicecomb.registry.discovery;

import java.util.List;
import java.util.Map;

import org.apache.servicecomb.registry.api.AbstractDiscoveryInstance;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;

/**
 * Wrapper class for DiscoveryInstance with states like: isolation, health, metrics and so on.
 */
public class StatefulDiscoveryInstance extends AbstractDiscoveryInstance {
  public enum IsolationStatus {
    NORMAL,
    ISOLATED
  }

  public enum PingStatus {
    UNKNOWN,
    OK,
    FAIL
  }

  public enum HistoryStatus {
    CURRENT,
    HISTORY
  }

  private final DiscoveryInstance discoveryInstance;

  private MicroserviceInstanceStatus microserviceInstanceStatus = MicroserviceInstanceStatus.UP;

  private IsolationStatus isolationStatus = IsolationStatus.NORMAL;

  private long isolatedTime;

  private long isolateDuration;

  private PingStatus pingStatus = PingStatus.UNKNOWN;

  private long pingSuccessTime;

  private HistoryStatus historyStatus = HistoryStatus.CURRENT;

  public StatefulDiscoveryInstance(DiscoveryInstance discoveryInstance) {
    this.discoveryInstance = discoveryInstance;
  }

  public MicroserviceInstanceStatus getMicroserviceInstanceStatus() {
    return microserviceInstanceStatus;
  }

  public void setMicroserviceInstanceStatus(
      MicroserviceInstanceStatus microserviceInstanceStatus) {
    this.microserviceInstanceStatus = microserviceInstanceStatus;
  }

  public IsolationStatus getIsolationStatus() {
    return isolationStatus;
  }

  public void setIsolationStatus(
      IsolationStatus isolationStatus) {
    this.isolationStatus = isolationStatus;
  }

  public PingStatus getPingStatus() {
    return pingStatus;
  }

  public void setPingStatus(PingStatus pingStatus) {
    this.pingStatus = pingStatus;
  }

  public HistoryStatus getHistoryStatus() {
    return historyStatus;
  }

  public void setHistoryStatus(HistoryStatus historyStatus) {
    this.historyStatus = historyStatus;
  }

  public long getIsolatedTime() {
    return isolatedTime;
  }

  public void setIsolatedTime(long isolatedTime) {
    this.isolatedTime = isolatedTime;
  }

  public long getIsolateDuration() {
    return isolateDuration;
  }

  public void setIsolateDuration(long isolateDuration) {
    this.isolateDuration = isolateDuration;
  }

  public long getPingSuccessTime() {
    return pingSuccessTime;
  }

  public void setPingSuccessTime(long pingSuccessTime) {
    this.pingSuccessTime = pingSuccessTime;
  }

  @Override
  public MicroserviceInstanceStatus getStatus() {
    return this.discoveryInstance.getStatus();
  }

  @Override
  public String getRegistryName() {
    return this.discoveryInstance.getRegistryName();
  }

  @Override
  public String getEnvironment() {
    return this.discoveryInstance.getEnvironment();
  }

  @Override
  public String getApplication() {
    return this.discoveryInstance.getApplication();
  }

  @Override
  public String getServiceName() {
    return this.discoveryInstance.getServiceName();
  }

  @Override
  public String getAlias() {
    return this.discoveryInstance.getAlias();
  }

  @Override
  public String getVersion() {
    return this.discoveryInstance.getVersion();
  }

  @Override
  public DataCenterInfo getDataCenterInfo() {
    return this.discoveryInstance.getDataCenterInfo();
  }

  @Override
  public String getDescription() {
    return this.discoveryInstance.getDescription();
  }

  @Override
  public Map<String, String> getProperties() {
    return this.discoveryInstance.getProperties();
  }

  @Override
  public Map<String, String> getSchemas() {
    return this.discoveryInstance.getSchemas();
  }

  @Override
  public List<String> getEndpoints() {
    return this.discoveryInstance.getEndpoints();
  }

  @Override
  public String getInstanceId() {
    return this.discoveryInstance.getInstanceId();
  }

  @Override
  public String getServiceId() {
    return this.discoveryInstance.getServiceId();
  }
}
