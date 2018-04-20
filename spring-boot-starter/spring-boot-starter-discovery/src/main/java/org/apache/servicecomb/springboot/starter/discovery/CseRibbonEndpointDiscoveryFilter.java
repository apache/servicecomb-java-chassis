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

package org.apache.servicecomb.springboot.starter.discovery;

import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.discovery.AbstractEndpointDiscoveryFilter;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryContext;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTreeNode;

import com.netflix.loadbalancer.Server;

public class CseRibbonEndpointDiscoveryFilter extends AbstractEndpointDiscoveryFilter {
  @Override
  protected String findTransportName(DiscoveryContext context, DiscoveryTreeNode parent) {
    //only need rest endpoints
    return "rest";
  }

  @Override
  protected Object createEndpoint(String transportName, String endpoint, MicroserviceInstance instance) {
    URIEndpointObject uri = new URIEndpointObject(endpoint);
    return new Server(uri.getHostOrIp(), uri.getPort());
  }

  @Override
  public int getOrder() {
    return (int) Short.MAX_VALUE - 1;
  }
}
