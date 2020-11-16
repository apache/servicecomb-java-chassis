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

package org.apache.servicecomb.loadbalance.filter;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.registry.discovery.EndpointDiscoveryFilter;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.cache.CacheEndpoint;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerDiscoveryFilter extends EndpointDiscoveryFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerDiscoveryFilter.class);

  @Override
  protected Object createEndpoint(DiscoveryContext context, String transportName, String endpoint,
      MicroserviceInstance instance) {
    Transport transport = SCBEngine.getInstance().getTransportManager().findTransport(transportName);
    if (transport == null) {
      LOGGER.info("not deployed transport {}, ignore {}.", transportName, endpoint);
      return null;
    }
    Invocation invocation = context.getInputParameters();
    return new ServiceCombServer(invocation.getMicroserviceName(), transport, new CacheEndpoint(endpoint, instance));
  }
}
