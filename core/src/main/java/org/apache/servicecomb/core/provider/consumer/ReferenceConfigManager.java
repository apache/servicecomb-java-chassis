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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.SCBEngine.CreateMicroserviceMetaEvent;
import org.apache.servicecomb.core.definition.ConsumerMicroserviceVersionsMeta;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.definition.MicroserviceNameParser;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.CollectionUtils;

import io.swagger.v3.oas.models.OpenAPI;
import jakarta.ws.rs.core.Response.Status;

public class ReferenceConfigManager {
  // application -> microservice name
  private final Map<String, Map<String, MicroserviceReferenceConfig>> referenceConfigs = new ConcurrentHashMapEx<>();

  private final Object referenceConfigsLock = new Object();

  private SCBEngine engine;

  private DiscoveryManager discoveryManager;

  @Autowired
  @Lazy
  public void setEngine(SCBEngine engine) {
    this.engine = engine;
  }

  @Autowired
  @Lazy
  public void setDiscoveryManager(DiscoveryManager discoveryManager) {
    this.discoveryManager = discoveryManager;
  }

  public MicroserviceReferenceConfig getOrCreateReferenceConfig(String qualifiedName) {
    MicroserviceNameParser parser = parseMicroserviceName(qualifiedName);
    MicroserviceReferenceConfig config = referenceConfigs.computeIfAbsent(parser.getAppId(),
            app -> new ConcurrentHashMapEx<>())
        .get(parser.getMicroserviceName());
    if (config == null) {
      synchronized (referenceConfigsLock) {
        config = referenceConfigs.get(parser.getAppId()).get(parser.getMicroserviceName());
        if (config != null) {
          return config;
        }

        List<? extends DiscoveryInstance> discoveryInstances = discoveryManager.findServiceInstances(
            parser.getAppId(),
            parser.getMicroserviceName());
        if (CollectionUtils.isEmpty(discoveryInstances)) {
          return null;
        }
        config = buildMicroserviceReferenceConfig(parser.getAppId(), parser.getMicroserviceName(),
            discoveryInstances);
        referenceConfigs.get(parser.getAppId()).put(parser.getMicroserviceName(), config);
        return config;
      }
    }
    return config;
  }

  private MicroserviceNameParser parseMicroserviceName(String microserviceName) {
    return new MicroserviceNameParser(engine.getAppId(), microserviceName);
  }

  private MicroserviceReferenceConfig buildMicroserviceReferenceConfig(String application,
      String microserviceName, List<? extends DiscoveryInstance> instances) {
    ConsumerMicroserviceVersionsMeta microserviceVersionsMeta = new ConsumerMicroserviceVersionsMeta(this.engine);
    MicroserviceMeta microserviceMeta = new MicroserviceMeta(this.engine, application, microserviceName, true);
    microserviceMeta.setFilterChain(engine.getFilterChainsManager().findConsumerChain(application, microserviceName));
    microserviceMeta.setMicroserviceVersionsMeta(microserviceVersionsMeta);

    Map<String, String> schemas = new HashMap<>();
    for (DiscoveryInstance instance : instances) {
      instance.getSchemas().forEach(schemas::putIfAbsent);
    }
    for (Entry<String, String> schema : schemas.entrySet()) {
      OpenAPI swagger = this.engine.getSwaggerLoader()
          .loadSwagger(application, microserviceName, schema.getKey(), schema.getValue());
      if (swagger != null) {
        microserviceMeta.registerSchemaMeta(schema.getKey(), swagger);
        continue;
      }
      throw new InvocationException(Status.INTERNAL_SERVER_ERROR,
          String.format("Swagger %s/%s/%s can not be empty or load swagger failed.",
              application, microserviceName, schema.getKey()));
    }

    this.engine.getEventBus().post(new CreateMicroserviceMetaEvent(microserviceMeta));
    return new MicroserviceReferenceConfig(application,
        microserviceName, microserviceMeta);
  }
}
