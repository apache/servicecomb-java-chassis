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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.registry.StaticMicroservice;
import org.apache.servicecomb.serviceregistry.version.Version;

public class StaticMicroserviceVersions extends MicroserviceVersions {

  private Class<?> schemaIntfCls;

  private String environment;

  public StaticMicroserviceVersions(AppManager appManager, String appId, String microserviceName,
      Class<?> schemaIntfCls) {
    super(appManager, appId, microserviceName);

    validated = true;
    this.schemaIntfCls = schemaIntfCls;
    this.environment = RegistryUtils.getMicroservice().getEnvironment();
  }

  @Override
  public void pullInstances() {
    // instance information is stored locally, do not pull from sc
  }

  public void addInstances(String version, List<MicroserviceInstance> addedInstances) {
    Version parsedVersion = new Version(version);
    String serviceId = computeServiceId(parsedVersion);

    for (MicroserviceInstance instance : addedInstances) {
      instance.setServiceId(serviceId);
      instance.setInstanceId(serviceId + "-" + UUID.randomUUID());
    }

    mergeInstances(addedInstances);

    // ensure microserviceVersion exists
    versions.computeIfAbsent(serviceId, microserviceId -> {
      StaticMicroservice microservice = createMicroservice(parsedVersion, serviceId);
      MicroserviceVersion microserviceVersion = appManager.getStaticMicroserviceVersionFactory().create(microservice);
      for (MicroserviceVersionRule microserviceVersionRule : versionRules.values()) {
        microserviceVersionRule.addMicroserviceVersion(microserviceVersion);
      }
      return microserviceVersion;
    });

    for (MicroserviceVersionRule microserviceVersionRule : versionRules.values()) {
      microserviceVersionRule.setInstances(this.instances);
    }
  }

  private void mergeInstances(List<MicroserviceInstance> instances) {
    if (null == this.instances) {
      this.instances = new ArrayList<>(instances.size());
    }
    this.instances.addAll(instances);
  }

  private StaticMicroservice createMicroservice(Version parsedVersion, String serviceId) {
    StaticMicroservice microservice = new StaticMicroservice();
    microservice.setAppId(this.getAppId());
    microservice.setServiceId(serviceId);
    microservice.setServiceName(this.getMicroserviceName());
    microservice.setVersion(parsedVersion.getVersion());
    microservice.setEnvironment(RegistryUtils.getMicroservice().getEnvironment());
    microservice.setSchemaIntfCls(this.schemaIntfCls);
    return microservice;
  }

  private String computeServiceId(Version parsedVersion) {
    return this.getAppId() + "-"
        + this.environment + "-"
        + this.getMicroserviceName() + "-"
        + parsedVersion.getVersion();
  }
}
