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
package org.apache.servicecomb.solution.basic.integration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.core.provider.LocalOpenAPIRegistry;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import io.swagger.v3.oas.models.OpenAPI;
import jakarta.ws.rs.core.Response.Status;

@RestSchema(schemaId = ManagementEndpoint.NAME, schemaInterface = ManagementEndpoint.class)
public class ManagementEndpointImpl implements ManagementEndpoint {
  private RegistrationManager registrationManager;

  private LocalOpenAPIRegistry localOpenAPIRegistry;

  private Environment environment;

  @Autowired
  public void setRegistrationManager(RegistrationManager registrationManager) {
    this.registrationManager = registrationManager;
  }

  @Autowired
  public void setLocalOpenAPIRegistry(LocalOpenAPIRegistry localOpenAPIRegistry) {
    this.localOpenAPIRegistry = localOpenAPIRegistry;
  }

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public boolean health(String instanceId, String registryName) {
    String mySelf = registrationManager.getInstanceId(registryName);
    if (StringUtils.isEmpty(mySelf)) {
      return false;
    }
    return mySelf.equals(instanceId);
  }

  @Override
  public Set<String> schemaIds() {
    return localOpenAPIRegistry.getSchemaIds(BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment));
  }

  @Override
  public Map<String, String> schemaContents(Set<String> schemaIds) {
    if (CollectionUtils.isEmpty(schemaIds)) {
      throw new InvocationException(Status.BAD_REQUEST, "invalid schemaIds parameter.");
    }
    checkValid(schemaIds);

    Map<String, OpenAPI> apis = localOpenAPIRegistry.loadOpenAPI(BootStrapProperties.readApplication(environment),
        BootStrapProperties.readServiceName(environment), schemaIds);
    if (apis.size() != schemaIds.size()) {
      throw new InvocationException(Status.BAD_REQUEST, "Not exists schemaIds parameter.");
    }
    Map<String, String> result = new HashMap<>(apis.size());
    apis.forEach((k, v) -> result.put(k, SwaggerUtils.swaggerToString(v)));
    return result;
  }

  private void checkValid(Set<String> schemaIds) {
    for (String schemaId : schemaIds) {
      if (schemaId.contains("/") || schemaId.contains("\\") || schemaId.contains("..")) {
        throw new InvocationException(Status.BAD_REQUEST, "invalid schemaIds parameter.");
      }
    }
  }
}
