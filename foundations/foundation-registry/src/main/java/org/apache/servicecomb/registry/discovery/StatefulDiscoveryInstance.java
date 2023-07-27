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

import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;

/**
 * Holder class for DiscoveryInstance with states like: isolation, health, metrics and so on.
 */
public class StatefulDiscoveryInstance {
  enum IsolationStatus {
    NORMAL,
    ISOLATED
  }

  enum PingStatus {
    UNKNOWN,
    OK,
    FAIL
  }

  private final DiscoveryInstance discoveryInstance;

  private MicroserviceInstanceStatus microserviceInstanceStatus = MicroserviceInstanceStatus.UP;

  private IsolationStatus isolationStatus = IsolationStatus.NORMAL;

  private PingStatus pingStatus = PingStatus.UNKNOWN;

  public StatefulDiscoveryInstance(DiscoveryInstance discoveryInstance) {
    this.discoveryInstance = discoveryInstance;
  }

  public DiscoveryInstance getDiscoveryInstance() {
    return discoveryInstance;
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
}
