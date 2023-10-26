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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.servicecomb.foundation.common.utils.ResourceUtil;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * Register and load OpenAPI from various OpenAPIRegistry
 */
public class OpenAPIRegistryManager {
  public interface OpenAPIChangeListener {
    void onOpenAPIChanged(String application, String serviceName);
  }

  private List<OpenAPIRegistry> openAPIRegistries;

  private final List<OpenAPIChangeListener> changeListeners = new ArrayList<>();

  @Autowired
  public void setOpenAPIRegistries(List<OpenAPIRegistry> openAPIRegistries) {
    this.openAPIRegistries = openAPIRegistries;
    for (OpenAPIRegistry registry : this.openAPIRegistries) {
      registry.setOpenAPIChangeListener(this::onOpenAPIChanged);
    }
  }

  public void addOpenAPIChangeListener(OpenAPIChangeListener changeListener) {
    this.changeListeners.add(changeListener);
  }

  public void onOpenAPIChanged(String application, String serviceName) {
    for (OpenAPIChangeListener listener : this.changeListeners) {
      listener.onOpenAPIChanged(application, serviceName);
    }
  }

  public Set<String> getSchemaIds(String application, String serviceName) {
    Set<String> result = new HashSet<>();
    for (OpenAPIRegistry registry : this.openAPIRegistries) {
      result.addAll(registry.getSchemaIds(application, serviceName));
    }
    return result;
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

  public OpenAPI loadOpenAPI(String appId, String microserviceName, String schemaId) {
    for (OpenAPIRegistry registry : this.openAPIRegistries) {
      OpenAPI result = registry.loadOpenAPI(appId, microserviceName, schemaId);
      if (result != null) {
        return result;
      }
    }
    return null;
  }
}
