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

package org.apache.servicecomb.registry.consumer;

import java.util.List;
import java.util.Map;

import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.FindInstancesResponse;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.foundation.common.Version;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import io.swagger.models.Swagger;

public class StaticMicroserviceVersions extends MicroserviceVersions {
  private static final Logger LOGGER = LoggerFactory.getLogger(StaticMicroserviceVersions.class);

  protected final Microservice microservice = new Microservice();

  protected final MicroserviceInstances microserviceInstances = new MicroserviceInstances();

  public StaticMicroserviceVersions(AppManager appManager, String appId, String microserviceName) {
    super(appManager, appId, microserviceName);
  }

  public StaticMicroserviceVersions init(Class<?> schemaIntfCls, String version,
      List<MicroserviceInstance> instances) {
    return init(ImmutableMap.of(microserviceName, schemaIntfCls), version, instances);
  }

  public StaticMicroserviceVersions init(Map<String, Class<?>> schemaByIdMap, String version,
      List<MicroserviceInstance> instances) {
    createMicroservice(version);
    addSchemas(schemaByIdMap);
    addInstances(instances);
    pullInstances();

    return this;
  }

  protected void createMicroservice(String version) {
    String environment = RegistrationManager.INSTANCE.getMicroservice().getEnvironment();

    microservice.setAppId(this.getAppId());
    microservice.setServiceName(this.getShortName());
    microservice.setVersion(new Version(version).getVersion());
    microservice.setServiceId(this.getAppId() + "-"
        + environment + "-"
        + this.getMicroserviceName() + "-"
        + microservice.getVersion());
    microservice.setEnvironment(environment);
  }

  protected void addSchemas(Map<String, Class<?>> schemaByIdMap) {
    schemaByIdMap.forEach(this::addSchema);
  }

  protected void addSchema(String schemaId, Class<?> schemaClass) {
    Swagger swagger = RegistrationManager.INSTANCE.getSwaggerLoader()
        .registerSwagger(appId, shortName, schemaId, schemaClass);
    String swaggerContent = SwaggerUtils.swaggerToString(swagger);
    LOGGER.debug("generate swagger for 3rd party service [{}], swagger: {}", microserviceName, swaggerContent);
    microservice.addSchema(schemaId, swaggerContent);
  }

  protected void addInstances(List<MicroserviceInstance> instances) {
    for (int idx = 0; idx < instances.size(); idx++) {
      MicroserviceInstance instance = instances.get(idx);
      instance.setServiceId(microservice.getServiceId());
      instance.setInstanceId(microservice.getServiceId() + "-" + idx);
    }
    microserviceInstances.setMicroserviceNotExist(false);
    microserviceInstances.setInstancesResponse(new FindInstancesResponse());
    microserviceInstances.getInstancesResponse().setInstances(instances);
  }

  @Override
  protected MicroserviceInstances findServiceInstances() {
    // Only refreshed for the first time
    microserviceInstances.setNeedRefresh(revision == null);
    microserviceInstances.setRevision("1");
    return microserviceInstances;
  }

  @Override
  protected MicroserviceVersion createMicroserviceVersion(String microserviceId, List<MicroserviceInstance> instances) {
    return new MicroserviceVersion(this, microservice, microserviceName, instances);
  }
}
