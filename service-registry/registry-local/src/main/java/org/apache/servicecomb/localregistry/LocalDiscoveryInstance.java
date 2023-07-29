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

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.localregistry.RegistryBean.Instance;
import org.apache.servicecomb.registry.api.AbstractDiscoveryInstance;
import org.apache.servicecomb.registry.api.DataCenterInfo;
import org.apache.servicecomb.registry.api.MicroserviceInstanceStatus;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;

import io.swagger.v3.oas.models.OpenAPI;

public class LocalDiscoveryInstance extends AbstractDiscoveryInstance {
  private static final AtomicLong INSTANCE_ID = new AtomicLong();

  private final RegistryBean registryBean;

  private final LocalRegistrationInstance localRegistrationInstance;

  private final Instance instance;

  private final String instanceId;

  private final Map<String, String> schemas = new HashMap<>();

  public LocalDiscoveryInstance(RegistryBean registryBean, Instance instance,
      LocalRegistrationInstance localRegistrationInstance) {
    this.registryBean = registryBean;
    this.localRegistrationInstance = localRegistrationInstance;
    this.instance = instance;
    this.instanceId = System.currentTimeMillis() + "-" +
        ManagementFactory.getRuntimeMXBean().getPid() + "-" + INSTANCE_ID.getAndIncrement();

    registryBean.getSchemaInterfaces().forEach((k, v) -> {
      SwaggerGenerator generator = SwaggerGenerator.create(v);
      OpenAPI openAPI = generator.generate();
      if (openAPI == null) {
        throw new IllegalStateException(String.format("Generate schema for %s/%s/%s faild.",
            registryBean.getAppId(), registryBean.getServiceName(), k));
      }
      String schemaContent = SwaggerUtils.swaggerToString(openAPI);
      if (StringUtils.isEmpty(schemaContent)) {
        throw new IllegalStateException(String.format("Generate schema for %s/%s/%s faild.",
            registryBean.getAppId(), registryBean.getServiceName(), k));
      }
      schemas.put(k, schemaContent);
    });

    for (String schemaId : registryBean.getSchemaIds()) {
      OpenAPI openAPI = SCBEngine.getInstance().getSwaggerLoader().loadLocalSwagger(
          registryBean.getAppId(), registryBean.getServiceName(), schemaId);
      if (openAPI == null) {
        // can be null, and will get it in SwaggerLoader in rpc.
        schemas.put(schemaId, "");
        continue;
      }
      String schemaContent = SwaggerUtils.swaggerToString(openAPI);
      if (StringUtils.isEmpty(schemaContent)) {
        // can be null, and will get it in SwaggerLoader in rpc.
        schemas.put(schemaId, "");
        continue;
      }
      schemas.put(schemaId, schemaContent);
    }
  }

  public LocalDiscoveryInstance(LocalRegistrationInstance registrationInstance) {
    this.registryBean = new RegistryBean();
    this.registryBean.setAppId(registrationInstance.getApplication());
    this.registryBean.setServiceName(registrationInstance.getServiceName());
    this.registryBean.setVersion(registrationInstance.getVersion());
    this.localRegistrationInstance = registrationInstance;
    this.instance = new Instance();
    this.instance.setEndpoints(registrationInstance.getEndpoints());
    this.instanceId = registrationInstance.getInstanceId();
    this.schemas.putAll(registrationInstance.getSchemas());
  }

  @Override
  public MicroserviceInstanceStatus getStatus() {
    return MicroserviceInstanceStatus.UP;
  }

  @Override
  public String getEnvironment() {
    return localRegistrationInstance.getEnvironment();
  }

  @Override
  public String getApplication() {
    return registryBean.getAppId();
  }

  @Override
  public String getServiceName() {
    return registryBean.getServiceName();
  }

  @Override
  public String getAlias() {
    return null;
  }

  @Override
  public String getVersion() {
    return registryBean.getVersion();
  }

  @Override
  public DataCenterInfo getDataCenterInfo() {
    return null;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public Map<String, String> getProperties() {
    return null;
  }

  @Override
  public Map<String, String> getSchemas() {
    return schemas;
  }

  @Override
  public List<String> getEndpoints() {
    return instance.getEndpoints();
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }
}
