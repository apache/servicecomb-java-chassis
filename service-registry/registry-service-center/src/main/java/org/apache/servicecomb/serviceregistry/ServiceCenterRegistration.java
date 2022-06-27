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

package org.apache.servicecomb.serviceregistry;

import java.util.Collection;

import org.apache.servicecomb.foundation.common.concurrency.SuppressedRunnableWrapper;
import org.apache.servicecomb.registry.api.Registration;
import org.apache.servicecomb.registry.api.registry.BasePath;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstanceStatus;
import org.apache.servicecomb.serviceregistry.api.Const;

import com.netflix.config.DynamicPropertyFactory;

public class ServiceCenterRegistration implements Registration {
  public static final String NAME = "service center registration";

  @Override
  public void init() {
    RegistryUtils.init();
  }

  @Override
  public void run() {
    RegistryUtils.run();
  }

  @Override
  public void destroy() {
    RegistryUtils.destroy();
  }

  @Override
  public int getOrder() {
    return Const.SERVICE_CENTER_ORDER;
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public MicroserviceInstance getMicroserviceInstance() {
    return RegistryUtils.getMicroserviceInstance();
  }

  @Override
  public Microservice getMicroservice() {
    return RegistryUtils.getMicroservice();
  }

  @Override
  public String getAppId() {
    return RegistryUtils.getAppId();
  }

  @Override
  public boolean updateMicroserviceInstanceStatus(MicroserviceInstanceStatus status) {
    RegistryUtils.executeOnEachServiceRegistry(sr -> new SuppressedRunnableWrapper(() -> {
      MicroserviceInstance selfInstance = sr.getMicroserviceInstance();
      sr.getServiceRegistryClient().updateMicroserviceInstanceStatus(
          selfInstance.getServiceId(),
          selfInstance.getInstanceId(),
          status);
    }).run());
    return true;
  }

  @Override
  public void addSchema(String schemaId, String content) {
    RegistryUtils.executeOnEachServiceRegistry(sr -> sr.getMicroservice().addSchema(schemaId, content));
  }

  @Override
  public void addEndpoint(String endpoint) {
    RegistryUtils.executeOnEachServiceRegistry(sr -> {
      Microservice microservice = sr.getMicroservice();
      microservice.getInstance().getEndpoints().add(endpoint);
    });
  }

  @Override
  public void addBasePath(Collection<BasePath> basePaths) {
    RegistryUtils.executeOnEachServiceRegistry(sr -> sr.getMicroservice().getPaths().addAll(basePaths));
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty(Const.SERVICE_CENTER_ENABLED, true).get();
  }
}
