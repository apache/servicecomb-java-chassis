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
import java.util.Map;

import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.RegistrationInstance;

public class ZeroConfigRegistrationInstance implements RegistrationInstance {
  private final Self self;

  public ZeroConfigRegistrationInstance(Self self) {
    this.self = self;
  }

  @Override
  public String getEnvironment() {
    return self.getMicroservice().getEnvironment();
  }

  @Override
  public String getApplication() {
    return self.getMicroservice().getAppId();
  }

  @Override
  public String getServiceName() {
    return self.getMicroservice().getServiceName();
  }

  @Override
  public String getAlias() {
    return self.getMicroservice().getAlias();
  }

  @Override
  public String getVersion() {
    return self.getMicroservice().getVersion();
  }

  @Override
  public DataCenterInfo getDataCenterInfo() {
    return self.getInstance().getDataCenterInfo();
  }

  @Override
  public String getDescription() {
    return self.getMicroservice().getDescription();
  }

  @Override
  public Map<String, String> getProperties() {
    return self.getInstance().getProperties();
  }

  @Override
  public Map<String, String> getSchemas() {
    return this.self.getMicroservice().getSchemaMap();
  }

  @Override
  public List<String> getEndpoints() {
    return this.self.getInstance().getEndpoints();
  }

  @Override
  public String getInstanceId() {
    return this.self.getInstanceId();
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
    this.self.getMicroservice().addSchema(schemaId, content);
  }

  public void addEndpoint(String endpoint) {
    this.self.getInstance().getEndpoints().add(endpoint);
  }

  public void addProperty(String key, String value) {
    this.self.getInstance().getProperties().put(key, value);
  }
}
