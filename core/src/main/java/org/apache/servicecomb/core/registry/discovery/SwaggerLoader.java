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
package org.apache.servicecomb.core.registry.discovery;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.servicecomb.config.MicroserviceProperties;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.apache.servicecomb.foundation.common.utils.ResourceUtil;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.definition.MicroserviceNameParser;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * Loading swaggers for microservices.
 *
 * 1. Memory: Registered by apis, like registerSwaggersInLocation, registerSwagger, etc
 * 2. Local: From local resource, like microservice/{microservice name}/{schema id}.yaml
 * 3. Remote: From DiscoveryInstances getSchemas method
 */
public class SwaggerLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerLoader.class);

  // first key : appId
  // second key: microservice short name
  // third key : schemaId
  private final Map<String, Map<String, Map<String, OpenAPI>>> apps = new ConcurrentHashMapEx<>();

  private final MicroserviceProperties microserviceProperties;

  public SwaggerLoader(MicroserviceProperties microserviceProperties) {
    this.microserviceProperties = microserviceProperties;
  }

  public void registerSwaggersInLocation(String microserviceName, String swaggersLocation) {
    LOGGER.info("register schemas in location [{}], microserviceName=[{}]", swaggersLocation, microserviceName);
    try {
      List<URI> resourceUris = ResourceUtil.findResourcesBySuffix(swaggersLocation, ".yaml");
      if (resourceUris.isEmpty()) {
        LOGGER.error("register swagger in not exist location: \"{}\".", swaggersLocation);
        return;
      }
      for (URI uri : resourceUris) {
        URL url = uri.toURL();
        OpenAPI swagger = SwaggerUtils.parseAndValidateSwagger(url);
        String schemaId = FilenameUtils.getBaseName(url.getPath());
        registerSwagger(microserviceName, schemaId, swagger);
      }
    } catch (Throwable e) {
      throw new IllegalStateException(String.format(
          "failed to register swaggers, microserviceName=%s, location=%s.", microserviceName, swaggersLocation),
          e);
    }
  }

  public void registerSwagger(String microserviceName, String schemaId, OpenAPI swagger) {
    MicroserviceNameParser parser = new MicroserviceNameParser(microserviceProperties.getApplication(),
        microserviceName);
    registerSwagger(parser.getAppId(), parser.getShortName(), schemaId, swagger);
  }

  public void registerSwagger(String appId, String shortName, String schemaId, Class<?> cls) {
    OpenAPI swagger = SwaggerGenerator.generate(cls);
    registerSwagger(appId, shortName, schemaId, swagger);
  }

  public void registerSwagger(String appId, String shortName, String schemaId, OpenAPI swagger) {
    apps.computeIfAbsent(appId, k -> new ConcurrentHashMapEx<>())
        .computeIfAbsent(shortName, k -> new ConcurrentHashMapEx<>())
        .put(schemaId, swagger);
    LOGGER.info("register swagger appId={}, name={}, schemaId={}.", appId, shortName, schemaId);
  }

  /**
   * Load swaggers: first from memory, then local resource and last instance.
   */
  public OpenAPI loadSwagger(String appId, String microserviceName,
      DiscoveryInstance instance, String schemaId) {
    OpenAPI swagger = loadLocalSwagger(appId, microserviceName, schemaId);
    if (swagger != null) {
      return swagger;
    }

    return loadFromRemote(appId, microserviceName, instance, schemaId);
  }

  /**
   * Load swaggers: first from memory, then local resource.
   */
  public OpenAPI loadLocalSwagger(String appId, String shortName, String schemaId) {
    LOGGER.info("try to load schema locally, appId=[{}], serviceName=[{}], schemaId=[{}]",
        appId, shortName, schemaId);
    OpenAPI swagger = loadFromMemory(appId, shortName, schemaId);
    if (swagger != null) {
      LOGGER.info("load schema from memory");
      return swagger;
    }

    return loadFromResource(appId, shortName, schemaId);
  }

  protected OpenAPI loadFromMemory(String appId, String shortName, String schemaId) {
    return Optional.ofNullable(apps.get(appId))
        .map(microservices -> microservices.get(shortName))
        .map(schemas -> schemas.get(schemaId))
        .orElse(null);
  }

  protected OpenAPI loadFromResource(String appId, String shortName, String schemaId) {
    if (appId.equals(microserviceProperties.getApplication())) {
      OpenAPI swagger = loadFromResource(String.format("microservices/%s/%s.yaml", shortName, schemaId));
      if (swagger != null) {
        return swagger;
      }
    }

    return loadFromResource(String.format("applications/%s/%s/%s.yaml", appId, shortName, schemaId));
  }

  protected OpenAPI loadFromResource(String path) {
    URL url = JvmUtils.findClassLoader().getResource(path);
    if (url == null) {
      return null;
    }

    LOGGER.info("load schema from path [{}]", path);
    return SwaggerUtils.parseAndValidateSwagger(url);
  }

  protected OpenAPI loadFromRemote(String appId, String microserviceName,
      DiscoveryInstance instances,
      String schemaId) {
    String schemaContent = instances.getSchemas().get(schemaId);
    if (schemaContent != null) {
      LOGGER.info(
          "load schema from service center, appId={}, microserviceName={}, schemaId={}.",
          appId,
          microserviceName,
          schemaId);
      LOGGER.debug(schemaContent);
      return SwaggerUtils.parseAndValidateSwagger(appId, microserviceName, schemaId, schemaContent);
    }

    LOGGER.warn("no schema in local, and can not get schema from service center, "
            + "appId={}, microserviceName={}, schemaId={}.",
        appId,
        microserviceName,
        schemaId);

    return null;
  }
}
