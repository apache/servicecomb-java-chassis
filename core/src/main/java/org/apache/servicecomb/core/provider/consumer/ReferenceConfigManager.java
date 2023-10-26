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
package org.apache.servicecomb.core.provider.consumer;

import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.SCBEngine.CreateMicroserviceMetaEvent;
import org.apache.servicecomb.core.definition.ConsumerMicroserviceVersionsMeta;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.provider.OpenAPIRegistryManager;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.registry.definition.MicroserviceNameParser;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.models.OpenAPI;
import jakarta.ws.rs.core.Response.Status;

public class ReferenceConfigManager {
  // application -> microservice name
  private final Map<String, Map<String, MicroserviceReferenceConfig>> referenceConfigs = new ConcurrentHashMapEx<>();

  private final Object referenceConfigsLock = new Object();

  private OpenAPIRegistryManager openAPIRegistryManager;

  @Autowired
  public void setOpenAPIRegistryManager(OpenAPIRegistryManager openAPIRegistryManager) {
    this.openAPIRegistryManager = openAPIRegistryManager;
    this.openAPIRegistryManager.addOpenAPIChangeListener(this::onOpenAPIChanged);
  }

  private void onOpenAPIChanged(String application, String serviceName) {
    if (referenceConfigs.get(application) != null && referenceConfigs.get(application).get(serviceName) != null) {
      MicroserviceReferenceConfig config = buildMicroserviceReferenceConfig(SCBEngine.getInstance(), application,
          serviceName);
      referenceConfigs.get(application).put(serviceName, config);
    }
  }

  public MicroserviceReferenceConfig getOrCreateReferenceConfig(SCBEngine scbEngine, String qualifiedName) {
    MicroserviceNameParser parser = parseMicroserviceName(scbEngine, qualifiedName);
    MicroserviceReferenceConfig config = referenceConfigs.computeIfAbsent(parser.getAppId(),
            app -> new ConcurrentHashMapEx<>())
        .get(parser.getMicroserviceName());
    if (config == null) {
      synchronized (referenceConfigsLock) {
        config = referenceConfigs.get(parser.getAppId()).get(parser.getMicroserviceName());
        if (config != null) {
          return config;
        }
        config = buildMicroserviceReferenceConfig(scbEngine, parser.getAppId(), parser.getMicroserviceName());
        referenceConfigs.get(parser.getAppId()).put(parser.getMicroserviceName(), config);
        return config;
      }
    }
    return config;
  }

  private MicroserviceNameParser parseMicroserviceName(SCBEngine scbEngine, String microserviceName) {
    return new MicroserviceNameParser(scbEngine.getAppId(), microserviceName);
  }

  private MicroserviceReferenceConfig buildMicroserviceReferenceConfig(SCBEngine engine,
      String application, String microserviceName) {
    ConsumerMicroserviceVersionsMeta microserviceVersionsMeta = new ConsumerMicroserviceVersionsMeta(engine);
    MicroserviceMeta microserviceMeta = new MicroserviceMeta(engine, application, microserviceName, true);
    microserviceMeta.setFilterChain(engine.getFilterChainsManager().findConsumerChain(application, microserviceName));
    microserviceMeta.setMicroserviceVersionsMeta(microserviceVersionsMeta);

    Set<String> schemaIds = this.openAPIRegistryManager.getSchemaIds(application, microserviceName);
    for (String schemaId : schemaIds) {
      OpenAPI swagger = this.openAPIRegistryManager
          .loadOpenAPI(application, microserviceName, schemaId);
      if (swagger != null) {
        microserviceMeta.registerSchemaMeta(schemaId, swagger);
        continue;
      }
      throw new InvocationException(Status.INTERNAL_SERVER_ERROR,
          String.format("Swagger %s/%s/%s can not be empty or load swagger failed.",
              application, microserviceName, schemaId));
    }

    EventManager.getEventBus().post(new CreateMicroserviceMetaEvent(microserviceMeta));
    return new MicroserviceReferenceConfig(application,
        microserviceName, microserviceMeta);
  }
}
