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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceNameParser;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import io.swagger.models.Swagger;

public class SwaggerLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerLoader.class);

  private ServiceRegistry serviceRegistry;

  // first key : appId
  // second key: microservice short name
  // third key : schemaId
  private Map<String, Map<String, Map<String, Swagger>>> apps = new ConcurrentHashMapEx<>();

  ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

  public SwaggerLoader(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  /**
   * <pre>
   * register swaggers in the location to current microservice
   * Scenes for contract first mode:
   *  1.consumer
   *    manager manage some product, can only know product microservice names after deploy
   *    and can only register swagger after product registered
   *    in fact, consumers can load swagger from ServiceCenter
   *    so for consumer, this logic is not necessary, just keep it for compatible
   *  2.producer
   *    deploy to different microservice name in different product
   *    can register swaggers in BootListener.onBeforeProducerProvider
   * </pre>
   * @param swaggersLocation eg. "test/schemas", will load all test/schemas/*.yaml
   */
  public void registerSwaggersInLocation(String swaggersLocation) {
    String microserviceName = serviceRegistry.getMicroservice().getServiceName();
    registerSwaggersInLocation(microserviceName, swaggersLocation);
  }

  public void registerSwaggersInLocation(String microserviceName, String swaggersLocation) {
    LOGGER.info("register schemas in location [{}], microserviceName=[{}]", swaggersLocation, microserviceName);
    try {
      Resource[] resources = readSwaggerResources(microserviceName, swaggersLocation);
      for (Resource resource : resources) {
        registerSwagger(microserviceName, swaggersLocation, resource);
      }
    } catch (Throwable e) {
      throw new IllegalStateException(String.format(
          "failed to register swaggers, microserviceName=%s, location=%s.", microserviceName, swaggersLocation),
          e);
    }
  }

  private void registerSwagger(String microserviceName, String swaggersLocation, Resource resource) throws IOException {
    URL url = resource.getURL();
    String schemaId = FilenameUtils.getBaseName(url.getPath());
    Swagger swagger = SwaggerUtils.parseAndValidateSwagger(url);
    registerSwagger(microserviceName, schemaId, swagger);
  }

  private Resource[] readSwaggerResources(String microserviceName, String swaggersLocation) throws IOException {
    Resource[] resources = resourcePatternResolver.getResources(getLocationPattern(swaggersLocation));
    if (0 == resources.length) {
      LOGGER.warn("no schema file found");
    }
    return resources;
  }

  private String getLocationPattern(String swaggersLocation) {
    return "classpath*:" + swaggersLocation + "/*.yaml";
  }

  public void registerSwagger(String schemaId, Swagger swagger) {
    registerSwagger(serviceRegistry.getMicroservice().getServiceName(), schemaId, swagger);
  }

  public void registerSwagger(String microserviceName, String schemaId, String swaggerContent) {
    try {
      Swagger swagger = SwaggerUtils.parseAndValidateSwagger(swaggerContent);
      registerSwagger(microserviceName, schemaId, swagger);
    } catch (Throwable e) {
      throw new IllegalStateException(
          String.format("Parse the swagger for %s:%s failed", microserviceName, schemaId),
          e);
    }
  }

  public void registerSwagger(String microserviceName, String schemaId, Swagger swagger) {
    MicroserviceNameParser parser = new MicroserviceNameParser(serviceRegistry.getAppId(), microserviceName);
    registerSwagger(parser.getAppId(), parser.getShortName(), schemaId, swagger);
  }

  public Swagger registerSwagger(String appId, String shortName, String schemaId, Class<?> cls) {
    Swagger swagger = SwaggerGenerator.generate(cls);
    registerSwagger(appId, shortName, schemaId, swagger);
    return swagger;
  }

  public void registerSwagger(String appId, String shortName, String schemaId, Swagger swagger) {
    apps.computeIfAbsent(appId, k -> new ConcurrentHashMapEx<>())
        .computeIfAbsent(shortName, k -> new ConcurrentHashMapEx<>())
        .put(schemaId, swagger);
    LOGGER.info("register swagger appId={}, name={}, schemaId={}.", appId, shortName, schemaId);
  }

  public void unregisterSwagger(String appId, String shortName, String schemaId) {
    apps.getOrDefault(appId, Collections.emptyMap())
        .getOrDefault(shortName, Collections.emptyMap())
        .remove(schemaId);
  }

  public Swagger loadSwagger(Microservice microservice, String schemaId) {
    Swagger swagger = loadLocalSwagger(microservice.getAppId(), microservice.getServiceName(), schemaId);
    if (swagger != null) {
      return swagger;
    }

    return loadFromRemote(microservice, schemaId);
  }

  public Swagger loadLocalSwagger(String appId, String shortName, String schemaId) {
    LOGGER.info("try to load schema locally, appId=[{}], serviceName=[{}], schemaId=[{}]",
        appId, shortName, schemaId);
    Swagger swagger = loadFromMemory(appId, shortName, schemaId);
    if (swagger != null) {
      LOGGER.info("load schema from memory");
      return swagger;
    }

    return loadFromResource(appId, shortName, schemaId);
  }

  Swagger loadFromMemory(String appId, String shortName, String schemaId) {
    return Optional.ofNullable(apps.get(appId))
        .map(microservices -> microservices.get(shortName))
        .map(schemas -> schemas.get(schemaId))
        .orElse(null);
  }

  private Swagger loadFromResource(String appId, String shortName, String schemaId) {
    if (appId.equals(serviceRegistry.getAppId())) {
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

    LOGGER.info("load schema from path [{}]", path);
    return SwaggerUtils.parseAndValidateSwagger(url);
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
      return SwaggerUtils.parseAndValidateSwagger(schemaContent);
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
