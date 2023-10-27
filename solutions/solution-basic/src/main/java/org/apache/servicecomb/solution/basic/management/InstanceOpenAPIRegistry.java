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

package org.apache.servicecomb.solution.basic.management;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.provider.OpenAPIRegistry;
import org.apache.servicecomb.core.provider.OpenAPIRegistryManager.OpenAPIChangeListener;
import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.core.transport.TransportManager;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.router.util.VersionCompareUtil;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import io.swagger.v3.oas.models.OpenAPI;
import jakarta.ws.rs.core.Response.Status;

public class InstanceOpenAPIRegistry implements OpenAPIRegistry {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceOpenAPIRegistry.class);

  private DiscoveryManager discoveryManager;

  private Environment environment;

  private TransportManager transportManager;

  @Autowired
  public void setDiscoveryManager(DiscoveryManager discoveryManager) {
    this.discoveryManager = discoveryManager;
  }

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Autowired
  public void setTransportManager(TransportManager transportManager) {
    this.transportManager = transportManager;
  }

  @Override
  public boolean enabled() {
    return true;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<String> getSchemaIds(String application, String serviceName) {
    List<? extends DiscoveryInstance> discoveryInstances =
        discoveryManager.findServiceInstances(application, serviceName);
    if (discoveryInstances.isEmpty()) {
      throw new InvocationException(Status.INTERNAL_SERVER_ERROR, "no instances");
    }
    discoveryInstances.sort((a, b) -> VersionCompareUtil.compareVersion(b.getVersion(), a.getVersion()));

    String version = null;
    for (DiscoveryInstance instance : discoveryInstances) {
      if (version != null && !version.equals(instance.getVersion())) {
        break;
      }
      version = instance.getVersion();
      for (String endpoint : instance.getEndpoints()) {
        URI uri = URI.create(endpoint);
        String transportName = uri.getScheme();
        Transport transport = transportManager.findTransport(transportName);
        if (transport == null) {
          continue;
        }
        // Use myself service name instead of the target. Because can avoid create
        // MicroserviceReferenceConfig for the target.
        Invocation invocation = InvokerUtils.createInvocation(BootStrapProperties.readServiceName(environment),
            transportName,
            ManagementEndpoint.NAME, "schemaIds",
            new HashMap<>(), new TypeReference<Set<String>>() {
            }.getType());
        invocation.setEndpoint(new Endpoint(transport, endpoint, discoveryInstances.get(0)));
        try {
          return (Set<String>) InvokerUtils.syncInvoke(invocation);
        } catch (InvocationException e) {
          LOGGER.warn("Get schema ids {}/{}/{} from endpoint {} failed. {}",
              instance.getApplication(),
              instance.getServiceName(),
              instance.getInstanceId(), endpoint, e.getMessage());
        }
      }
    }
    throw new InvocationException(Status.INTERNAL_SERVER_ERROR, "Get schema ids fail from all latest version.");
  }

  @Override
  public void registerOpenAPI(String application, String serviceName, String schemaId, OpenAPI api) {
    // do nothing
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, OpenAPI> loadOpenAPI(String application, String serviceName, Set<String> schemaIds) {
    List<? extends DiscoveryInstance> discoveryInstances =
        discoveryManager.findServiceInstances(application, serviceName);
    if (discoveryInstances.isEmpty()) {
      throw new InvocationException(Status.INTERNAL_SERVER_ERROR, "no instances");
    }
    discoveryInstances.sort((a, b) -> VersionCompareUtil.compareVersion(b.getVersion(), a.getVersion()));

    String version = null;
    for (DiscoveryInstance instance : discoveryInstances) {
      if (version != null && !version.equals(instance.getVersion())) {
        break;
      }
      version = instance.getVersion();
      for (String endpoint : instance.getEndpoints()) {
        URI uri = URI.create(endpoint);
        String transportName = uri.getScheme();
        Transport transport = transportManager.findTransport(transportName);
        if (transport == null) {
          continue;
        }
        // Use myself service name instead of the target. Because can avoid create
        // MicroserviceReferenceConfig for the target.
        Map<String, Object> args = new HashMap<>();
        args.put("schemaIds", schemaIds);
        Invocation invocation = InvokerUtils.createInvocation(BootStrapProperties.readServiceName(environment),
            transportName,
            ManagementEndpoint.NAME, "schemaContents",
            args, new TypeReference<Map<String, String>>() {
            }.getType());
        invocation.setEndpoint(new Endpoint(transport, endpoint, discoveryInstances.get(0)));
        try {
          Map<String, String> contents = (Map<String, String>) InvokerUtils.syncInvoke(invocation);
          Map<String, OpenAPI> result = new HashMap<>(contents.size());
          contents.forEach((k, v) -> result.put(k, SwaggerUtils.parseSwagger(v)));
          return result;
        } catch (InvocationException e) {
          LOGGER.warn("Get schema contents {}/{}/{} from endpoint {} failed. {}",
              instance.getApplication(),
              instance.getServiceName(),
              instance.getInstanceId(), endpoint, e.getMessage());
        }
      }
    }
    throw new InvocationException(Status.INTERNAL_SERVER_ERROR, "Get schema contents fail from all latest version.");
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
    return -9000;
  }
}
