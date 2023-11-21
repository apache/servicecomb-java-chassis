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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.provider.OpenAPIRegistryManager.OpenAPIChangeListener;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.router.util.VersionCompareUtil;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import io.swagger.v3.oas.models.OpenAPI;
import jakarta.ws.rs.core.Response.Status;

/**
 * register and load OpenAPI from registration and discovery registry.
 */
public class RegistryOpenAPIRegistry implements OpenAPIRegistry {
  private DiscoveryManager discoveryManager;

  private Environment environment;

  @Autowired
  public void setDiscoveryManager(DiscoveryManager discoveryManager) {
    this.discoveryManager = discoveryManager;
  }

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }


  @Override
  public boolean enabled() {
    return environment.getProperty(OpenAPIRegistry.CONFIG_PREFIX + ".registry.enabled", boolean.class, false);
  }

  @Override
  public void registerOpenAPI(String application, String serviceName, String schemaId, OpenAPI api) {
    // do noting
  }

  @Override
  public Map<String, OpenAPI> loadOpenAPI(String application, String serviceName) {
    List<? extends DiscoveryInstance> discoveryInstances =
        discoveryManager.findServiceInstances(application, serviceName);
    if (discoveryInstances.isEmpty()) {
      throw new InvocationException(Status.INTERNAL_SERVER_ERROR, "no instances");
    }
    discoveryInstances.sort((a, b) -> VersionCompareUtil.compareVersion(b.getVersion(), a.getVersion()));

    Map<String, OpenAPI> result = new HashMap<>();
    String version = null;
    for (DiscoveryInstance instance : discoveryInstances) {
      if (version != null && !version.equals(instance.getVersion())) {
        break;
      }
      version = instance.getVersion();
      instance.getSchemas().forEach((k, v) -> result.computeIfAbsent(k, (key) -> SwaggerUtils.parseSwagger(v)));
    }
    return result;
  }

  @Override
  public void setOpenAPIChangeListener(OpenAPIChangeListener listener) {
    this.discoveryManager.addInstanceChangeListener(
        (registryName, application, serviceName, instances) -> {
          if (CollectionUtils.isEmpty(instances)) {
            return;
          }
          listener.onOpenAPIChanged(application, serviceName);
        });
  }

  @Override
  public int getOrder() {
    return -8000;
  }
}
