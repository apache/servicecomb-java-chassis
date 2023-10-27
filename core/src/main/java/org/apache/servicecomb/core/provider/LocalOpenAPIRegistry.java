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
package org.apache.servicecomb.core.provider;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.core.provider.OpenAPIRegistryManager.OpenAPIChangeListener;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.apache.servicecomb.foundation.common.utils.ResourceUtil;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * register and load OpenAPI from local file store or memory
 */
public class LocalOpenAPIRegistry implements OpenAPIRegistry {
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalOpenAPIRegistry.class);

  // first key : appId
  // second key: microservice short name
  // third key : schemaId
  private final Map<String, Map<String, Map<String, OpenAPI>>> apps = new ConcurrentHashMapEx<>();

  private final Environment environment;

  private OpenAPIChangeListener openAPIChangeListener;

  public LocalOpenAPIRegistry(Environment environment) {
    this.environment = environment;
  }

  @Override
  public boolean enabled() {
    return true;
  }

  @Override
  public Set<String> getSchemaIds(String application, String serviceName) {
    Set<String> result = new HashSet<>();
    if (apps.get(application) != null && apps.get(application).get(serviceName) != null) {
      result.addAll(apps.get(application).get(serviceName).keySet());
    }

    String swaggersLocation;
    if (application.equals(BootStrapProperties.readApplication(environment))) {
      swaggersLocation = String.format("microservices/%s", serviceName);
    } else {
      swaggersLocation = String.format("applications/%s/%s", application, serviceName);
    }
    try {
      List<URI> resourceUris = ResourceUtil.findResourcesBySuffix(swaggersLocation, ".yaml");
      result.addAll(resourceUris.stream().map(item -> {
        String path = item.getPath();
        path = path.substring(path.lastIndexOf("/") + 1);
        path = path.substring(0, path.indexOf(".yaml"));
        return path;
      }).toList());
    } catch (Exception e) {
      LOGGER.error("Load schema ids failed from location {}.", swaggersLocation);
    }
    return result;
  }

  @Override
  public void registerOpenAPI(String application, String serviceName, String schemaId, OpenAPI api) {
    apps.computeIfAbsent(application, k -> new ConcurrentHashMapEx<>())
        .computeIfAbsent(serviceName, k -> new ConcurrentHashMapEx<>())
        .put(schemaId, api);
    openAPIChangeListener.onOpenAPIChanged(application, serviceName);
    LOGGER.info("register swagger appId={}, name={}, schemaId={}.",
        application, serviceName, schemaId);
  }

  @Override
  public Map<String, OpenAPI> loadOpenAPI(String application, String serviceName, Set<String> schemaIds) {
    Map<String, OpenAPI> result = new HashMap<>(schemaIds.size());
    for (String schemaId : schemaIds) {
      OpenAPI api = loadFromMemory(application, serviceName, schemaId);
      if (api == null) {
        api = loadFromResource(application, serviceName, schemaId);
      }
      if (api != null) {
        result.put(schemaId, api);
      }
    }
    return result;
  }

  @Override
  public void setOpenAPIChangeListener(OpenAPIChangeListener listener) {
    this.openAPIChangeListener = listener;
  }

  protected OpenAPI loadFromResource(String application, String serviceName, String schemaId) {
    if (application.equals(BootStrapProperties.readApplication(environment))) {
      OpenAPI swagger = loadFromResource(String.format("microservices/%s/%s.yaml", serviceName, schemaId));
      if (swagger != null) {
        return swagger;
      }
    }
    return loadFromResource(String.format("applications/%s/%s/%s.yaml", application, serviceName, schemaId));
  }

  protected OpenAPI loadFromResource(String path) {
    URL url = JvmUtils.findClassLoader().getResource(path);
    if (url == null) {
      return null;
    }

    return SwaggerUtils.parseAndValidateSwagger(url);
  }

  protected OpenAPI loadFromMemory(String application, String serviceName, String schemaId) {
    return Optional.ofNullable(apps.get(application))
        .map(microservices -> microservices.get(serviceName))
        .map(schemas -> schemas.get(schemaId))
        .orElse(null);
  }

  @Override
  public int getOrder() {
    return -10000;
  }
}
