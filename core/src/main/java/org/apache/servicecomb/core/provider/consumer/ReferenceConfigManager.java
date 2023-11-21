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
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.SCBEngine.CreateMicroserviceMetaEvent;
import org.apache.servicecomb.core.definition.ConsumerMicroserviceVersionsMeta;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.provider.OpenAPIRegistryManager;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.vertx.executor.VertxWorkerExecutor;
import org.apache.servicecomb.registry.definition.MicroserviceNameParser;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.models.OpenAPI;
import jakarta.ws.rs.core.Response.Status;

public class ReferenceConfigManager {
  // application -> microservice name
  private final Map<String, Map<String, MicroserviceReferenceConfig>> referenceConfigs = new ConcurrentHashMapEx<>();

  private final Map<String, Map<String, Object>> referenceConfigsLocks = new ConcurrentHashMapEx<>();

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

  public CompletableFuture<MicroserviceReferenceConfig> getOrCreateReferenceConfigAsync
      (SCBEngine scbEngine, String qualifiedName) {
    MicroserviceNameParser parser = parseMicroserviceName(scbEngine, qualifiedName);
    MicroserviceReferenceConfig config = referenceConfigs.computeIfAbsent(parser.getAppId(),
            app -> new ConcurrentHashMapEx<>())
        .get(parser.getMicroserviceName());

    if (config != null) {
      return CompletableFuture.completedFuture(config);
    }

    if (InvokerUtils.isInEventLoop()) {
      CompletableFuture<MicroserviceReferenceConfig> result = new CompletableFuture<>();
      VertxWorkerExecutor executor = new VertxWorkerExecutor();
      executor.execute(() -> {
        synchronized (referenceConfigsLocks.computeIfAbsent(parser.getAppId(), key -> new ConcurrentHashMapEx<>())
            .computeIfAbsent(parser.getMicroserviceName(), key -> new Object())) {
          try {
            MicroserviceReferenceConfig temp = referenceConfigs.get(parser.getAppId())
                .get(parser.getMicroserviceName());
            if (temp != null) {
              result.complete(temp);
              return;
            }
            temp = buildMicroserviceReferenceConfig(scbEngine, parser.getAppId(),
                parser.getMicroserviceName());
            referenceConfigs.get(parser.getAppId()).put(parser.getMicroserviceName(), temp);
            result.complete(temp);
          } catch (Exception e) {
            result.completeExceptionally(e);
          }
        }
      });
      return result;
    } else {
      synchronized (referenceConfigsLocks.computeIfAbsent(parser.getAppId(), key -> new ConcurrentHashMapEx<>())
          .computeIfAbsent(parser.getMicroserviceName(), key -> new Object())) {
        MicroserviceReferenceConfig temp = referenceConfigs.get(parser.getAppId())
            .get(parser.getMicroserviceName());
        if (temp != null) {
          return CompletableFuture.completedFuture(temp);
        }
        temp = buildMicroserviceReferenceConfig(scbEngine, parser.getAppId(),
            parser.getMicroserviceName());
        referenceConfigs.get(parser.getAppId()).put(parser.getMicroserviceName(), temp);
        return CompletableFuture.completedFuture(temp);
      }
    }
  }

  public MicroserviceReferenceConfig getOrCreateReferenceConfig(SCBEngine scbEngine, String qualifiedName) {
    MicroserviceNameParser parser = parseMicroserviceName(scbEngine, qualifiedName);
    MicroserviceReferenceConfig config = referenceConfigs.computeIfAbsent(parser.getAppId(),
            app -> new ConcurrentHashMapEx<>())
        .get(parser.getMicroserviceName());
    if (config == null) {
      synchronized (referenceConfigsLocks.computeIfAbsent(parser.getAppId(), key -> new ConcurrentHashMapEx<>())
          .computeIfAbsent(parser.getMicroserviceName(), key -> new Object())) {
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
    microserviceMeta.setConsumerFilterChain(engine.getFilterChainsManager()
        .findConsumerChain(application, microserviceName));
    microserviceMeta.setEdgeFilterChain(engine.getFilterChainsManager()
        .findEdgeChain(application, microserviceName));
    microserviceMeta.setMicroserviceVersionsMeta(microserviceVersionsMeta);

    Map<String, OpenAPI> schemas = this.openAPIRegistryManager.loadOpenAPI(application, microserviceName);
    for (Entry<String, OpenAPI> entry : schemas.entrySet()) {
      OpenAPI swagger = entry.getValue();
      if (swagger != null) {
        microserviceMeta.registerSchemaMeta(entry.getKey(), entry.getValue());
        continue;
      }
      throw new InvocationException(Status.INTERNAL_SERVER_ERROR,
          String.format("Swagger %s/%s/%s can not be empty or load swagger failed.",
              application, microserviceName, entry.getKey()));
    }

    EventManager.getEventBus().post(new CreateMicroserviceMetaEvent(microserviceMeta));
    return new MicroserviceReferenceConfig(application,
        microserviceName, microserviceMeta);
  }
}
