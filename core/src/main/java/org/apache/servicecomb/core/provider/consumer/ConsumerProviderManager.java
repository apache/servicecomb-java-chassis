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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.core.provider.OpenAPIRegistryManager;
import org.apache.servicecomb.foundation.common.utils.ResourceUtil;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class ConsumerProviderManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerProviderManager.class);

  private final OpenAPIRegistryManager openAPIRegistryManager;

  private final Environment environment;

  public ConsumerProviderManager(Environment environment, OpenAPIRegistryManager openAPIRegistryManager) {
    this.environment = environment;
    this.openAPIRegistryManager = openAPIRegistryManager;
  }

  public void init() throws Exception {
    registerSwaggerFromApplications();
    registerSwaggerFromMicroservices();
  }

  private void registerSwaggerFromApplications() {
    try {
      List<URI> resourceUris = ResourceUtil.findResourcesBySuffix("applications", ".yaml");
      for (URI uri : resourceUris) {
        String path = uri.toURL().getPath();
        String[] segments = path.split("/");
        if (segments.length < 4 || !"applications".equals(segments[segments.length - 4])) {
          continue;
        }
        openAPIRegistryManager.registerOpenAPI(segments[segments.length - 3], segments[segments.length - 2],
            segments[segments.length - 1].substring(0, segments[segments.length - 1].indexOf(".yaml")),
            SwaggerUtils.parseAndValidateSwagger(uri.toURL()));
      }
    } catch (IOException | URISyntaxException e) {
      LOGGER.error("Load schema ids failed from applications. {}.", e.getMessage());
    }
  }

  private void registerSwaggerFromMicroservices() {
    try {
      List<URI> resourceUris = ResourceUtil.findResourcesBySuffix("microservices", ".yaml");
      for (URI uri : resourceUris) {
        String path = uri.toURL().getPath();
        String[] segments = path.split("/");
        if (segments.length < 3 || !"microservices".equals(segments[segments.length - 3])) {
          continue;
        }
        openAPIRegistryManager.registerOpenAPI(BootStrapProperties.readApplication(environment),
            segments[segments.length - 2],
            segments[segments.length - 1].substring(0, segments[segments.length - 1].indexOf(".yaml")),
            SwaggerUtils.parseAndValidateSwagger(uri.toURL()));
      }
    } catch (IOException | URISyntaxException e) {
      LOGGER.error("Load schema ids failed from microservices. {}.", e.getMessage());
    }
  }
}
