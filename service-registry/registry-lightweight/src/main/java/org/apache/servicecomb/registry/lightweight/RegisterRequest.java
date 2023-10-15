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

package org.apache.servicecomb.registry.lightweight;

import java.util.List;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.invocation.endpoint.EndpointCacheUtils;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;

public class RegisterRequest {
  private String appId;

  private String serviceId;

  private boolean crossApp;

  private String schemasSummary;

  private String instanceId;

  private MicroserviceInstanceStatus status;

  private List<String> endpoints;

  public String getAppId() {
    return appId;
  }

  public RegisterRequest setAppId(String appId) {
    this.appId = appId;
    return this;
  }

  public String getServiceId() {
    return serviceId;
  }

  public RegisterRequest setServiceId(String serviceId) {
    this.serviceId = serviceId;
    return this;
  }

  public boolean isCrossApp() {
    return crossApp;
  }

  public RegisterRequest setCrossApp(boolean crossApp) {
    this.crossApp = crossApp;
    return this;
  }

  public String getSchemasSummary() {
    return schemasSummary;
  }

  public RegisterRequest setSchemasSummary(String schemasSummary) {
    this.schemasSummary = schemasSummary;
    return this;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public RegisterRequest setInstanceId(String instanceId) {
    this.instanceId = instanceId;
    return this;
  }

  public MicroserviceInstanceStatus getStatus() {
    return status;
  }

  public RegisterRequest setStatus(MicroserviceInstanceStatus status) {
    this.status = status;
    return this;
  }

  public List<String> getEndpoints() {
    return endpoints;
  }

  public RegisterRequest setEndpoints(List<String> endpoints) {
    this.endpoints = endpoints;
    return this;
  }

  public Endpoint selectFirstEndpoint() {
    return endpoints.stream()
        .findFirst()
        .map(EndpointCacheUtils::getOrCreate)
        .orElse(null);
  }
}
