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
import java.util.UUID;

import org.apache.servicecomb.registry.api.registry.FindInstancesResponse;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.version.Version;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.models.Swagger;

public class StaticMicroserviceVersions extends MicroserviceVersions {

  private static final Logger LOGGER = LoggerFactory.getLogger(StaticMicroserviceVersions.class);

  private Class<?> schemaIntfCls;

  private Microservice microservice = new Microservice();

  private MicroserviceInstances microserviceInstances = new MicroserviceInstances();

  public StaticMicroserviceVersions(AppManager appManager, String appId, String microserviceName) {
    super(appManager, appId, microserviceName);
  }

  public StaticMicroserviceVersions init(Class<?> schemaIntfCls, String version,
      List<MicroserviceInstance> addedInstances) {
    this.schemaIntfCls = schemaIntfCls;
    Swagger swagger = RegistrationManager.INSTANCE.getSwaggerLoader()
        .registerSwagger(appId, shortName, shortName, schemaIntfCls);
    String swaggerContent = SwaggerUtils.swaggerToString(swagger);
    LOGGER.info("generate swagger for 3rd party service [{}]/[{}], swagger: {}",
        getMicroserviceName(), version, swaggerContent);
    microservice.addSchema(shortName, swaggerContent);

    createMicroservice(version);

    for (MicroserviceInstance instance : addedInstances) {
      instance.setServiceId(microservice.getServiceId());
      instance.setInstanceId(microservice.getServiceId() + "-" + UUID.randomUUID());
    }
    microserviceInstances.setMicroserviceNotExist(false);
    microserviceInstances.setInstancesResponse(new FindInstancesResponse());
    microserviceInstances.getInstancesResponse().setInstances(addedInstances);

    pullInstances();

    return this;
  }

  public Class<?> getSchemaIntfCls() {
    return schemaIntfCls;
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

  private void createMicroservice(String version) {
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
}
