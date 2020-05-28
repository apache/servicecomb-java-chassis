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

package org.apache.servicecomb.schemadiscovery;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.base.ServiceCombConstants;
import org.apache.servicecomb.loadbalance.LoadbalanceHandler;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.registry.api.Discovery;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstances;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

public class SchemaDiscovery implements Discovery {
  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaDiscovery.class);

  public static final String NAME = "schema discovery";

  public static final String ENABLED = "servicecomb.schema.registry.discovery.enabled";

  private SchemaDiscoveryService schemaDiscoveryService = null;

  @Override
  public Microservice getMicroservice(String microserviceId) {
    return null;
  }

  @Override
  public String getSchema(String microserviceId, Collection<MicroserviceInstance> instances, String schemaId) {
    if (instances == null || instances.isEmpty()) {
      return null;
    }

    for (MicroserviceInstance instance : instances) {
      if (!microserviceId.equals(instance.getServiceId())) {
        // ensure the same version
        continue;
      }

      List<String> endpoints = instance.getEndpoints();
      if (endpoints == null || endpoints.isEmpty()) {
        continue;
      }

      for (String endpoint : endpoints) {
        InvocationContext invocationContext = new InvocationContext();
        invocationContext.addLocalContext(LoadbalanceHandler.SERVICECOMB_SERVER_ENDPOINT, endpoint);
        SchemaDiscoveryService schemaDiscoveryService = getOrCreateSchemaDiscoveryService();
        try {
          String schema = schemaDiscoveryService.getSchema(invocationContext, schemaId);
          if (!StringUtils.isEmpty(schema)) {
            return schema;
          }
        } catch (Exception e) {
          LOGGER.warn("failed query schema from endpoint {}, msg {}", endpoint, e.getMessage());
          continue;
        }
      }
    }

    return null;
  }

  private SchemaDiscoveryService getOrCreateSchemaDiscoveryService() {
    if (this.schemaDiscoveryService == null) {
      // For schema discovery, assume all instances of different microservices
      // are instances of this microservice.
      String serviceName = DynamicPropertyFactory.getInstance()
          .getStringProperty(ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY,
              ServiceCombConstants.DEFAULT_MICROSERVICE_NAME).get();

      schemaDiscoveryService = Invoker
          .createProxy(serviceName, SchemaDiscoveryService.SCHEMA_ID,
              SchemaDiscoveryService.class);
    }
    return schemaDiscoveryService;
  }

  @Override
  public MicroserviceInstance getMicroserviceInstance(String serviceId, String instanceId) {
    return null;
  }

  @Override
  public MicroserviceInstances findServiceInstances(String appId, String serviceName, String versionRule) {
    MicroserviceInstances microserviceInstances = new MicroserviceInstances();
    microserviceInstances.setMicroserviceNotExist(true);
    return microserviceInstances;
  }

  @Override
  public String getRevision() {
    return null;
  }

  @Override
  public void setRevision(String revision) {

  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty(ENABLED, true).get();
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
  public int getOrder() {
    return 1000;
  }
}
