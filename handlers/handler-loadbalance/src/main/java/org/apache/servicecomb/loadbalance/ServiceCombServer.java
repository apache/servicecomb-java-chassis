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

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;

/**
 *  Server object used for transports and load balancer.
 *
 */
public class ServiceCombServer {
  private final Endpoint endpoint;

  private final String microserviceName;

  private final ServerMetrics serverMetrics;

  public ServiceCombServer(String microserviceName, Endpoint endpoint) {
    this.microserviceName = microserviceName;
    this.endpoint = endpoint;
    this.serverMetrics = new ServerMetrics();
  }

  public String getMicroserviceName() {
    return this.microserviceName;
  }

  public Endpoint getEndpoint() {
    return endpoint;
  }

  public DiscoveryInstance getInstance() {
    return endpoint.getMicroserviceInstance();
  }

  @Override
  public String toString() {
    return endpoint.getEndpoint();
  }

  public String getHost() {
    return endpoint.getEndpoint();
  }

  public ServerMetrics getServerMetrics() {
    return serverMetrics;
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
