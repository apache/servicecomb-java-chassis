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

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.CacheEndpoint;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.loadbalancer.Server;

/**
 * 服务器抽象，address只有transport识别， LB模块不识别
 * LB模块不提供服务器状态监测，这块功能是由注册中心进行处理的。
 *
 */
public class ServiceCombServer extends Server {
  private final Endpoint endpoint;

  // 所属服务实例
  private final MicroserviceInstance instance;

  @VisibleForTesting
  ServiceCombServer(Endpoint endpoint, MicroserviceInstance instance) {
    super(null);
    this.endpoint = endpoint;
    this.instance = instance;

    // Different types of Robin Component Rule have different usages for server status and list.
    // e.g. RoundRobinRule using getAllServers & alive & readyToServe
    // RandomRule using getReachableServers & alive
    // WeightedResponseTimeRule using getAllServers & alive
    // To make all rules work only on "how to choose a server from alive servers", we do not rely on Robbin defined status
    this.setAlive(true);
    this.setReadyToServe(true);
  }

  public ServiceCombServer(Transport transport, CacheEndpoint cacheEndpoint) {
    super(null);

    endpoint = new Endpoint(transport, cacheEndpoint.getEndpoint(), cacheEndpoint.getInstance());
    instance = cacheEndpoint.getInstance();

    // Different types of Robin Component Rule have different usages for server status and list.
    // e.g. RoundRobinRule using getAllServers & alive & readyToServe
    // RandomRule using getReachableServers & alive
    // WeightedResponseTimeRule using getAllServers & alive
    // To make all rules work only on "how to choose a server from alive servers", we do not rely on Robbin defined status
    this.setAlive(true);
    this.setReadyToServe(true);
  }

  public Endpoint getEndpoint() {
    return endpoint;
  }

  public MicroserviceInstance getInstance() {
    return instance;
  }

  public String toString() {
    return endpoint.getEndpoint();
  }

  // used in LoadBalancerContext
  public String getHost() {
    return endpoint.getEndpoint();
  }

  // take endpoints that belongs to same instance as same server
  public boolean equals(Object o) {
    if (o instanceof ServiceCombServer) {
      return this.instance.getInstanceId().equals(((ServiceCombServer) o).instance.getInstanceId());
    } else {
      return false;
    }
  }

  public int hashCode() {
    return this.instance.getInstanceId().hashCode();
  }
}
