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

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.Server;

import io.servicecomb.foundation.common.cache.VersionedCache;
import io.servicecomb.foundation.common.net.URIEndpointObject;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.definition.DefinitionConst;
import io.servicecomb.serviceregistry.discovery.DiscoveryContext;
import io.servicecomb.serviceregistry.discovery.DiscoveryTree;

public class ServiceCombServerList extends AbstractServerList<Server> {

  private DiscoveryTree discoveryTree = new DiscoveryTree();

  private String serviceId;

  public ServiceCombServerList() {
  }

  @Override
  public List<Server> getInitialListOfServers() {
    DiscoveryContext context = new DiscoveryContext();
    context.setInputParameters(serviceId);
    VersionedCache serversVersionedCache = discoveryTree.discovery(context,
        RegistryUtils.getAppId(),
        serviceId,
        DefinitionConst.VERSION_RULE_ALL);
    Map<String, MicroserviceInstance> servers = serversVersionedCache.data();
    List<Server> instances = new ArrayList<>(servers.size());
    for (MicroserviceInstance s : servers.values()) {
      for (String endpoint : s.getEndpoints()) {
        URIEndpointObject uri = new URIEndpointObject(endpoint);
        instances.add(new Server(uri.getHostOrIp(), uri.getPort()));
      }
    }
    return instances;
  }

  @Override
  public List<Server> getUpdatedListOfServers() {
    return getInitialListOfServers();
  }

  @Override
  public void initWithNiwsConfig(IClientConfig iClientConfig) {
    this.serviceId = iClientConfig.getClientName();
  }
}
