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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.Server;

import io.servicecomb.loadbalance.ServerListCache;

public class ServiceCombServerList extends AbstractServerList<Server> {

  private static final Logger logger = LoggerFactory.getLogger(ServiceCombServerList.class);

  private final CseRoutesProperties config;

  private ServerListCache serverListCache;

  private String serviceId;

  public ServiceCombServerList(CseRoutesProperties config) {
    this.config = config;
  }

  @Override
  public List<Server> getInitialListOfServers() {
    return servers();
  }

  @Override
  public List<Server> getUpdatedListOfServers() {
    return servers();
  }

  private List<Server> servers() {
    if (serverListCache == null) {
      throw new ServiceCombDiscoveryException("Service list is not initialized");
    }

    logger.info("Looking for service with app id: {}, service id: {}, version rule: {}",
        config.getAppID(),
        serviceId,
        config.getVersionRule(serviceId));

    List<Server> endpoints = serverListCache.getLatestEndpoints();

    logger.info("Found service endpoints {}", endpoints);
    return endpoints;
  }

  @Override
  public void initWithNiwsConfig(IClientConfig iClientConfig) {
    serviceId = iClientConfig.getClientName();

    serverListCache = new CseServerListCacheWrapper(
        config.getAppID(),
        serviceId,
        config.getVersionRule(serviceId),
        "rest");
  }
}
