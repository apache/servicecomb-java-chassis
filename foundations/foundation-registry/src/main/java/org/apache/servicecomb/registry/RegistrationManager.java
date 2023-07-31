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

package org.apache.servicecomb.registry;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.servicecomb.registry.api.LifeCycle;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.registry.api.Registration;
import org.apache.servicecomb.registry.api.RegistrationInstance;
import org.springframework.util.CollectionUtils;

import io.vertx.core.json.jackson.JacksonFactory;

public class RegistrationManager {
  private final List<Registration<? extends RegistrationInstance>> registrationList;

  public RegistrationManager(List<Registration<? extends RegistrationInstance>> registrationList) {
    if (registrationList == null) {
      this.registrationList = Collections.emptyList();
      return;
    }
    this.registrationList = registrationList;
  }

  /**
   * For internal use. Only choose the first RegistrationInstance id.
   */
  public String getInstanceId() {
    if (CollectionUtils.isEmpty(registrationList)) {
      return "";
    }
    return registrationList.get(0).getMicroserviceInstance().getInstanceId();
  }

  /**
   * For internal use. Only choose the first RegistrationInstance id.
   */
  public String getServiceId() {
    if (CollectionUtils.isEmpty(registrationList)) {
      return "";
    }
    return registrationList.get(0).getMicroserviceInstance().getServiceId();
  }

  public void updateMicroserviceInstanceStatus(MicroserviceInstanceStatus status) {
    registrationList
        .forEach(registration -> registration.updateMicroserviceInstanceStatus(status));
  }

  public void addProperty(String key, String value) {
    registrationList
        .forEach(registration -> registration.addProperty(key, value));
  }

  public void addSchema(String schemaId, String content) {
    registrationList
        .forEach(registration -> registration.addSchema(schemaId, content));
  }

  public void addEndpoint(String endpoint) {
    registrationList
        .forEach(registration -> registration.addEndpoint(endpoint));
  }

  public void destroy() {
    registrationList.forEach(LifeCycle::destroy);
  }

  public void run() {
    registrationList.forEach(LifeCycle::run);
  }

  public void init() {
    registrationList.forEach(LifeCycle::init);
  }

  public String info() {
    StringBuilder result = new StringBuilder();
    AtomicBoolean first = new AtomicBoolean(true);
    registrationList.forEach(registration -> {
      if (first.getAndSet(false)) {
        result.append("App ID: ").append(registration.getMicroserviceInstance().getApplication()).append("\n");
        result.append("Service Name: ").append(registration.getMicroserviceInstance().getServiceName()).append("\n");
        result.append("Version: ").append(registration.getMicroserviceInstance().getVersion()).append("\n");
        result.append("Environment: ").append(registration.getMicroserviceInstance().getEnvironment()).append("\n");
        result.append("Endpoints: ").append(getEndpoints(registration.getMicroserviceInstance().getEndpoints()))
            .append("\n");
        result.append("Registration implementations:\n");
      }

      result.append("  name:").append(registration.name()).append("\n");
      result.append("    Instance ID: ").append(registration.getMicroserviceInstance().getInstanceId()).append("\n");
    });
    return result.toString();
  }

  private String getEndpoints(List<String> endpoints) {
    return JacksonFactory.CODEC.toString(endpoints);
  }
}
