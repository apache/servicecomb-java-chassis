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

import java.util.Collection;

import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.serviceregistry.Registration;
import org.apache.servicecomb.serviceregistry.api.registry.BasePath;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.serviceregistry.event.MicroserviceInstanceRegisteredEvent;

import com.netflix.config.DynamicPropertyFactory;

public class LocalRegistration implements Registration {
  public static final String NAME = "local registration";

  public static final String ENABLED = "servicecomb.local.registry.registration.enabled";

  private LocalRegistryStore localRegistrationStore = LocalRegistryStore.INSTANCE;

  @Override
  public void init() {
    localRegistrationStore.init();
  }

  @Override
  public void run() {
    localRegistrationStore.run();
    EventManager.getEventBus().post(new MicroserviceInstanceRegisteredEvent());
  }

  @Override
  public void destroy() {

  }

  @Override
  public int getOrder() {
    return 100;
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public MicroserviceInstance getMicroserviceInstance() {
    return localRegistrationStore.getSelfMicroserviceInstance();
  }

  @Override
  public Microservice getMicroservice() {
    return localRegistrationStore.getSelfMicroservice();
  }

  @Override
  public String getAppId() {
    return localRegistrationStore.getSelfMicroservice().getAppId();
  }

  @Override
  public boolean updateMicroserviceInstanceStatus(MicroserviceInstanceStatus status) {
    localRegistrationStore.getSelfMicroserviceInstance().setStatus(status);
    return true;
  }

  @Override
  public void addSchema(String schemaId, String content) {
    localRegistrationStore.getSelfMicroservice().addSchema(schemaId, content);
  }

  @Override
  public void addEndpoint(String endpoint) {
    localRegistrationStore.getSelfMicroserviceInstance().getEndpoints().add(endpoint);
  }

  @Override
  public void addBasePath(Collection<BasePath> basePaths) {
    localRegistrationStore.getSelfMicroservice().getPaths().addAll(basePaths);
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty(ENABLED, true).get();
  }
}
