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
package io.servicecomb.springboot.starter.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import io.servicecomb.foundation.common.cache.VersionedCache;
import io.servicecomb.foundation.common.net.URIEndpointObject;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.client.ServiceRegistryClient;
import io.servicecomb.serviceregistry.definition.DefinitionConst;
import io.servicecomb.serviceregistry.discovery.DiscoveryContext;
import io.servicecomb.serviceregistry.discovery.DiscoveryTree;

public class CseDiscoveryClient implements DiscoveryClient {
  private DiscoveryTree discoveryTree = new DiscoveryTree();

  @Override
  public String description() {
    return "Spring Cloud CSE Discovery Client";
  }

  @Override
  public List<ServiceInstance> getInstances(final String serviceId) {
    DiscoveryContext context = new DiscoveryContext();
    context.setInputParameters(serviceId);
    VersionedCache serversVersionedCache = discoveryTree.discovery(context,
        RegistryUtils.getAppId(),
        serviceId,
        DefinitionConst.VERSION_RULE_ALL);
    Map<String, MicroserviceInstance> servers = serversVersionedCache.data();
    List<ServiceInstance> instances = new ArrayList<>(servers.size());
    for (MicroserviceInstance s : servers.values()) {
      for (String endpoint : s.getEndpoints()) {
        URIEndpointObject uri = new URIEndpointObject(endpoint);
        instances.add(new DefaultServiceInstance(serviceId, uri.getHostOrIp(), uri.getPort(), uri.isSslEnabled()));
      }
    }
    return instances;
  }

  @Override
  public ServiceInstance getLocalServiceInstance() {
    return null;
  }

  @Override
  public List<String> getServices() {
    ServiceRegistryClient client = RegistryUtils.getServiceRegistryClient();
    List<Microservice> services = client.getAllMicroservices();
    List<String> serviceIDList = new ArrayList<>();
    if (null != services && !services.isEmpty()) {
      for (Microservice service : services) {
        serviceIDList.add(service.getServiceName());
      }
    }
    return serviceIDList;
  }
}
