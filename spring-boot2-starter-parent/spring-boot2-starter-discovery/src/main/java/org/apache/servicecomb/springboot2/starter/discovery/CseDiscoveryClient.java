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
package org.apache.servicecomb.springboot2.starter.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryContext;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryFilter;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTree;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

public class CseDiscoveryClient implements DiscoveryClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(CseDiscoveryClient.class);

  private Map<String, DiscoveryTree> discoveryTrees = new ConcurrentHashMapEx<>();

  @Override
  public String description() {
    return "Spring Cloud CSE Discovery Client";
  }

  @Override
  public List<ServiceInstance> getInstances(final String serviceId) {
    class InstanceDiscoveryFilter implements DiscoveryFilter {
      @Override
      public int getOrder() {
        return Short.MAX_VALUE;
      }

      @Override
      public DiscoveryTreeNode discovery(DiscoveryContext context, DiscoveryTreeNode parent) {
        return parent.children()
            .computeIfAbsent(context.getInputParameters(), etn -> createDiscoveryTreeNode(context, parent));
      }

      @SuppressWarnings("unchecked")
      protected DiscoveryTreeNode createDiscoveryTreeNode(DiscoveryContext context,
          DiscoveryTreeNode parent) {
        String serviceName = context.getInputParameters();
        List<ServiceInstance> instances = new ArrayList<>();
        for (MicroserviceInstance instance : ((Map<String, MicroserviceInstance>) parent.data()).values()) {
          for (String endpoint : instance.getEndpoints()) {
            String scheme = endpoint.split(":", 2)[0];
            if (!scheme.equalsIgnoreCase(Const.RESTFUL)) {
              LOGGER.info("Endpoint {} is not supported in Spring Cloud, ignoring.", endpoint);
              continue;
            }
            URIEndpointObject uri = new URIEndpointObject(endpoint);
            instances.add(new DefaultServiceInstance(serviceId, uri.getHostOrIp(), uri.getPort(), uri.isSslEnabled()));
          }
        }
        return new DiscoveryTreeNode()
            .subName(parent, serviceName)
            .data(instances);
      }
    };

    DiscoveryContext context = new DiscoveryContext();
    context.setInputParameters(serviceId);

    DiscoveryTree discoveryTree = discoveryTrees.computeIfAbsent(serviceId, key -> {
      DiscoveryTree tree =  new DiscoveryTree();
      tree.addFilter(new InstanceDiscoveryFilter());
      return tree;
    });

    VersionedCache serversVersionedCache = discoveryTree.discovery(context,
        RegistryUtils.getAppId(),
        serviceId,
        DefinitionConst.VERSION_RULE_ALL);

    return serversVersionedCache.data();
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
