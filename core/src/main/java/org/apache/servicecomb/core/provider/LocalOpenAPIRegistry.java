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

import java.util.Collections;
import java.util.Map;

import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.core.provider.OpenAPIRegistryManager.OpenAPIChangeListener;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
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
  public void registerOpenAPI(String application, String serviceName, String schemaId, OpenAPI api) {
    apps.computeIfAbsent(application, k -> new ConcurrentHashMapEx<>())
        .computeIfAbsent(serviceName, k -> new ConcurrentHashMapEx<>())
        .put(schemaId, api);
    openAPIChangeListener.onOpenAPIChanged(application, serviceName);
    LOGGER.info("register swagger appId={}, name={}, schemaId={}.",
        application, serviceName, schemaId);
  }

  /**
   * Method for retrieve myself schema contents.
   */
  public Map<String, OpenAPI> loadOpenAPI() {
    return loadOpenAPI(BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment));
  }

  @Override
  public Map<String, OpenAPI> loadOpenAPI(String application, String serviceName) {
    if (apps.get(application) != null && apps.get(application).get(serviceName) != null) {
      return apps.get(application).get(serviceName);
    }
    return Collections.emptyMap();
  }

  @Override
  public void setOpenAPIChangeListener(OpenAPIChangeListener listener) {
    this.openAPIChangeListener = listener;
  }

  @Override
  public int getOrder() {
    return -10000;
  }
}
