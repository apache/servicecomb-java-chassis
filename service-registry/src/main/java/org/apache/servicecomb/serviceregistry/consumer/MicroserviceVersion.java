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

package org.apache.servicecomb.serviceregistry.consumer;

import java.util.Collection;
import java.util.List;

import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceMeta;
import org.apache.servicecomb.serviceregistry.swagger.SwaggerLoader;
import org.apache.servicecomb.serviceregistry.version.Version;

import io.swagger.models.Swagger;

public class MicroserviceVersion {
  protected AppManager appManager;

  // because of cross app invoke
  // microserviceName not always equals microservice.serviceName
  protected String microserviceName;

  protected Version version;

  protected Microservice microservice;

  protected MicroserviceMeta microserviceMeta;

  protected Collection<MicroserviceInstance> instances;

  public MicroserviceVersion(AppManager appManager, String microserviceId, String microserviceName,
      Collection<MicroserviceInstance> instances) {
    Microservice microservice = appManager.getServiceRegistry().getAggregatedRemoteMicroservice(microserviceId);
    if (microservice == null) {
      throw new IllegalStateException(
          String.format("failed to query by microserviceId '%s' from ServiceCenter.", microserviceId));
    }

    init(appManager, microservice, microserviceName, instances);
  }

  public MicroserviceVersion(AppManager appManager, Microservice microservice, String microserviceName,
      Collection<MicroserviceInstance> instances) {
    init(appManager, microservice, microserviceName, instances);
  }

  protected void init(AppManager appManager, Microservice microservice, String microserviceName,
      Collection<MicroserviceInstance> instances) {
    this.appManager = appManager;
    this.microservice = microservice;
    this.microserviceName = microserviceName;
    this.instances = instances;
    this.version = new Version(microservice.getVersion());
    this.microserviceMeta = new MicroserviceMeta(microserviceName);
    // TODO: get schemas from instance
    SwaggerLoader swaggerLoader = appManager.getServiceRegistry().getSwaggerLoader();
    for (String schemaId : microservice.getSchemas()) {
      Swagger swagger = swaggerLoader.loadSwagger(microservice, microserviceName, schemaId);
      this.microserviceMeta.registerSchemaMeta(schemaId, swagger);
    }
  }

  public void setInstances(List<MicroserviceInstance> instances) {
    this.instances = instances;
  }

  public String getMicroserviceName() {
    return microserviceName;
  }

  public String getMicroserviceId() {
    return microservice.getServiceId();
  }

  public Microservice getMicroservice() {
    return microservice;
  }

  public MicroserviceMeta getMicroserviceMeta() {
    return microserviceMeta;
  }

  public Version getVersion() {
    return version;
  }
}
