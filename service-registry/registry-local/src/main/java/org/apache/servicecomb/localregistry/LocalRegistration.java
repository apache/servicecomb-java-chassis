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

package org.apache.servicecomb.localregistry;

import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class LocalRegistration implements Registration<LocalRegistrationInstance> {
  private final LocalRegistrationInstance localRegistrationInstance;

  private Environment environment;

  public LocalRegistration(LocalRegistrationInstance localRegistrationInstance) {
    this.localRegistrationInstance = localRegistrationInstance;
  }

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public void init() {

  }

  @Override
  public void run() {

  }

  @Override
  public void destroy() {

  }

  @Override
  public String name() {
    return LocalRegistryConst.LOCAL_REGISTRY_NAME;
  }

  @Override
  public LocalRegistrationInstance getMicroserviceInstance() {
    return localRegistrationInstance;
  }

  @Override
  public boolean updateMicroserviceInstanceStatus(MicroserviceInstanceStatus status) {
    return true;
  }

  @Override
  public void addSchema(String schemaId, String content) {
    localRegistrationInstance.addSchema(schemaId, content);
  }

  @Override
  public void addEndpoint(String endpoint) {
    localRegistrationInstance.addEndpoint(endpoint);
  }

  @Override
  public void addProperty(String key, String value) {
    localRegistrationInstance.addProperty(key, value);
  }

  @Override
  public boolean enabled() {
    return this.environment.getProperty(LocalRegistryConst.LOCAL_REGISTRY_ENABLED, Boolean.class, true);
  }
}
