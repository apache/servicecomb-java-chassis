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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.servicecomb.foundation.common.utils.ResourceUtil;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * Register and load OpenAPI from various OpenAPIRegistry
 */
public class OpenAPIRegistryManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenAPIRegistryManager.class);

  public interface OpenAPIChangeListener {
    void onOpenAPIChanged(String application, String serviceName);
  }

  private List<OpenAPIRegistry> openAPIRegistries;

  private final List<OpenAPIChangeListener> changeListeners = new ArrayList<>();

  @Autowired
  public void setOpenAPIRegistries(List<OpenAPIRegistry> openAPIRegistries) {
    List<OpenAPIRegistry> target = new ArrayList<>(openAPIRegistries.size());
    for (OpenAPIRegistry registry : openAPIRegistries) {
      if (registry.enabled()) {
        registry.setOpenAPIChangeListener(this::onOpenAPIChanged);
        target.add(registry);
      }
    }
    this.openAPIRegistries = target;
  }

  public void addOpenAPIChangeListener(OpenAPIChangeListener changeListener) {
    this.changeListeners.add(changeListener);
  }

  public void onOpenAPIChanged(String application, String serviceName) {
    for (OpenAPIChangeListener listener : this.changeListeners) {
      try {
        listener.onOpenAPIChanged(application, serviceName);
      } catch (Exception e) {
        LOGGER.warn("event process error {}/{}, {}", application, serviceName, e.getMessage());
      }
    }
  }

  public void registerOpenAPI(String application, String serviceName, String schemaId, OpenAPI api) {
    for (OpenAPIRegistry registry : this.openAPIRegistries) {
      registry.registerOpenAPI(application, serviceName, schemaId, api);
    }
  }

  public void registerOpenAPI(String application, String serviceName, String schemaId, Class<?> cls) {
    OpenAPI api = SwaggerGenerator.generate(cls);
    registerOpenAPI(application, serviceName, schemaId, api);
  }

  public void registerOpenAPIInLocation(String application, String serviceName, String swaggersLocation) {
    try {
      List<URI> resourceUris = ResourceUtil.findResourcesBySuffix(swaggersLocation, ".yaml");
      if (resourceUris.isEmpty()) {
        return;
      }
      for (URI uri : resourceUris) {
        URL url = uri.toURL();
        OpenAPI swagger = SwaggerUtils.parseAndValidateSwagger(url);
        String schemaId = FilenameUtils.getBaseName(url.getPath());
        registerOpenAPI(application, serviceName, schemaId, swagger);
      }
    } catch (Throwable e) {
      throw new IllegalStateException(String.format(
          "failed to register swaggers, microserviceName=%s, location=%s.",
          serviceName, swaggersLocation), e);
    }
  }

  public Map<String, OpenAPI> loadOpenAPI(String appId, String microserviceName) {
    for (OpenAPIRegistry registry : this.openAPIRegistries) {
      Map<String, OpenAPI> result = registry.loadOpenAPI(appId, microserviceName);
      if (!CollectionUtils.isEmpty(result)) {
        return result;
      }
    }
    return Collections.emptyMap();
  }
}
