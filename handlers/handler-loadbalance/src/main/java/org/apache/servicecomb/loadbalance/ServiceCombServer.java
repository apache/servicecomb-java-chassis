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

package org.apache.servicecomb.loadbalance;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;

import com.netflix.loadbalancer.Server;

/**
 * 服务器抽象，address只有transport识别， LB模块不识别
 * LB模块不提供服务器状态监测，这块功能是由注册中心进行处理的。
 *
 */
public class ServiceCombServer extends Server {
  private final Endpoint endpoint;

  private final String microserviceName;

  public ServiceCombServer(String microserviceName, Endpoint endpoint) {
    super(null);
    this.microserviceName = microserviceName;
    this.endpoint = endpoint;

    // Different types of Robin Component Rule have different usages for server status and list.
    // e.g. RoundRobinRule using getAllServers & alive & readyToServe
    // RandomRule using getReachableServers & alive
    // WeightedResponseTimeRule using getAllServers & alive
    // To make all rules work only on "how to choose a server from alive servers", we do not rely on Robbin defined status
    this.setAlive(true);
    this.setReadyToServe(true);
    try {
      URI endpointURI = new URI(endpoint.getEndpoint());
      setHost(endpointURI.getHost());
      setPort(endpointURI.getPort());
    } catch (URISyntaxException ignored) {
    }
  }

  public String getMicroserviceName() {
    return this.microserviceName;
  }

  public Endpoint getEndpoint() {
    return endpoint;
  }

  public StatefulDiscoveryInstance getInstance() {
    return endpoint.getMicroserviceInstance();
  }

  @Override
  public String toString() {
    return endpoint.getEndpoint();
  }

  // used in LoadBalancerContext
  @Override
  public String getHost() {
    return endpoint.getEndpoint();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ServiceCombServer) {
      return this.getInstance().getInstanceId()
          .equals(((ServiceCombServer) o).getInstance().getInstanceId())
          && StringUtils.equals(endpoint.getEndpoint(), ((ServiceCombServer) o).getEndpoint().getEndpoint());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getInstance().getInstanceId(), this.endpoint);
  }
}
