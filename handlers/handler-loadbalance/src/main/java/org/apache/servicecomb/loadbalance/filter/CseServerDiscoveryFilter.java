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

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.filter.EndpointDiscoveryFilter;
import org.apache.servicecomb.loadbalance.CseServer;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.cache.CacheEndpoint;

public class CseServerDiscoveryFilter extends EndpointDiscoveryFilter {
  @Override
  protected Object createEndpoint(String transportName, String endpoint, MicroserviceInstance instance) {
    Transport transport = CseContext.getInstance().getTransportManager().findTransport(transportName);
    if (transport == null) {
      return null;
    }

    return new CseServer(transport, new CacheEndpoint(endpoint, instance));
  }
}
