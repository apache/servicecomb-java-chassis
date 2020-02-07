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
package org.apache.servicecomb.springboot.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryContext;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryFilter;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTree;

public abstract class AbstractDiscoveryClient {

  private Map<String, DiscoveryTree> discoveryTrees = new ConcurrentHashMapEx<>();
  private DiscoveryFilter filter = null;

  public AbstractDiscoveryClient(DiscoveryFilter filter){
    this.filter = filter;
  }

  public <T> List<T> doGetInstances(final String serviceId) {
    DiscoveryContext context = new DiscoveryContext();
    context.setInputParameters(serviceId);

    DiscoveryTree discoveryTree = discoveryTrees.computeIfAbsent(serviceId, key -> {
      DiscoveryTree tree =  new DiscoveryTree();
      tree.addFilter(filter);
      return tree;
    });

    VersionedCache serversVersionedCache = discoveryTree.discovery(context,
        RegistryUtils.getAppId(),
        serviceId,
        DefinitionConst.VERSION_RULE_ALL);

    return serversVersionedCache.data();
  }

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
