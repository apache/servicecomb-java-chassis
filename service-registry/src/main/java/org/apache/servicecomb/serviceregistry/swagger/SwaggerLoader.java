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
package org.apache.servicecomb.serviceregistry.swagger;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceNameParser;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.models.Swagger;

public class SwaggerLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerLoader.class);

  private ServiceRegistry serviceRegistry;

  // first key : appId
  // second key: microservice short name
  // third key : schemaId
  private Map<String, Map<String, Map<String, Swagger>>> apps = new ConcurrentHashMapEx<>();

  public SwaggerLoader(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  public void registerSwagger(String microserviceName, String schemaId, Swagger swagger) {
    MicroserviceNameParser parser = new MicroserviceNameParser(microserviceName);
    registerSwagger(parser.getAppId(), parser.getShortName(), schemaId, swagger);
  }

  public void registerSwagger(String appId, String shortName, String schemaId, Class<?> cls) {
    Swagger swagger = SwaggerGenerator.generate(cls);
    registerSwagger(appId, shortName, schemaId, swagger);
  }

  public void registerSwagger(String appId, String shortName, String schemaId, Swagger swagger) {
    apps.computeIfAbsent(appId, k -> new ConcurrentHashMapEx<>())
        .computeIfAbsent(shortName, k -> new ConcurrentHashMapEx<>())
        .put(schemaId, swagger);
  }

  public void unregisterSwagger(String appId, String shortName, String schemaId) {
    apps.getOrDefault(appId, Collections.emptyMap())
        .getOrDefault(shortName, Collections.emptyMap())
        .remove(schemaId);
  }

  public Swagger loadSwagger(Microservice microservice, String microserviceName, String schemaId) {
    Swagger swagger = loadLocalSwagger(microservice.getAppId(), microserviceName, schemaId);
    if (swagger != null) {
      return swagger;
    }

    return loadFromRemote(microservice, schemaId);
  }

  private Swagger loadLocalSwagger(String appId, String shortName, String schemaId) {
    Swagger swagger = loadFromMemory(appId, shortName, schemaId);
    if (swagger != null) {
      return swagger;
    }

    return loadFromResource(appId, shortName, schemaId);
  }

  private Swagger loadFromMemory(String appId, String shortName, String schemaId) {
    return Optional.ofNullable(apps.get(appId))
        .map(microservices -> microservices.get(shortName))
        .map(schemas -> schemas.get(schemaId))
        .orElse(null);
  }

  private Swagger loadFromResource(String appId, String shortName, String schemaId) {
    if (appId.equals(serviceRegistry.getMicroservice().getAppId())) {
      Swagger swagger = loadFromResource(String.format("microservices/%s/%s.yaml", shortName, schemaId));
      if (swagger != null) {
        return swagger;
      }
    }

    return loadFromResource(String.format("applications/%s/%s/%s.yaml", appId, shortName, schemaId));
  }

  private Swagger loadFromResource(String path) {
    URL url = JvmUtils.findClassLoader().getResource(path);
    if (url == null) {
      return null;
    }

    return parseAndValidateSwagger(url);
  }

  private Swagger parseAndValidateSwagger(URL url) {
    Swagger swagger = SwaggerUtils.parseSwagger(url);
    SwaggerUtils.validateSwagger(swagger);
    return swagger;
  }

  private Swagger parseAndValidateSwagger(String swaggerContent) {
    Swagger swagger = SwaggerUtils.parseSwagger(swaggerContent);
    SwaggerUtils.validateSwagger(swagger);
    return swagger;
  }

  private Swagger loadFromRemote(Microservice microservice, String schemaId) {
    String schemaContent = serviceRegistry.getServiceRegistryClient()
        .getAggregatedSchema(microservice.getServiceId(), schemaId);
    if (schemaContent != null) {
      LOGGER.info(
          "load schema from service center, appId={}, microserviceName={}, version={}, serviceId={}, schemaId={}.",
          microservice.getAppId(),
          microservice.getServiceName(),
          microservice.getVersion(),
          microservice.getServiceId(),
          schemaId);
      LOGGER.debug(schemaContent);
      return parseAndValidateSwagger(schemaContent);
    }

    throw new IllegalStateException(
        String.format("no schema in local, and can not get schema from service center, "
                + "appId=%s, microserviceName=%s, version=%s, serviceId=%s, schemaId=%s.",
            microservice.getAppId(),
            microservice.getServiceName(),
            microservice.getVersion(),
            microservice.getServiceId(),
            schemaId));
  }
}
