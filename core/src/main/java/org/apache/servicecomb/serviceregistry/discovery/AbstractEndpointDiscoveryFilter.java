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

package org.apache.servicecomb.serviceregistry.discovery;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEndpointDiscoveryFilter implements DiscoveryFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEndpointDiscoveryFilter.class);

  private static final String ALL_TRANSPORT = "";

  @Override
  public boolean isGroupingFilter() {
    return true;
  }

  @Override
  public DiscoveryTreeNode discovery(DiscoveryContext context, DiscoveryTreeNode parent) {
    String expectTransportName = findTransportName(context, parent);
    return parent.children()
        .computeIfAbsent(expectTransportName, etn -> createDiscoveryTreeNode(expectTransportName, context, parent));
  }

  @SuppressWarnings("unchecked")
  protected DiscoveryTreeNode createDiscoveryTreeNode(String expectTransportName, DiscoveryContext context,
      DiscoveryTreeNode parent) {
    List<Object> endpoints = new ArrayList<>();
    for (MicroserviceInstance instance : ((Map<String, MicroserviceInstance>) parent.data()).values()) {
      for (String endpoint : instance.getEndpoints()) {
        try {
          URI uri = URI.create(endpoint);
          String transportName = uri.getScheme();
          if (!isTransportNameMatch(transportName, expectTransportName)) {
            continue;
          }

          Object objEndpoint = createEndpoint(transportName, endpoint, instance);
          if (objEndpoint == null) {
            continue;
          }

          endpoints.add(objEndpoint);
        } catch (Exception e) {
          LOGGER.warn("unrecognized address find, ignore {}.", endpoint);
        }
      }
    }

    return new DiscoveryTreeNode()
        .subName(parent, expectTransportName)
        .data(endpoints);
  }

  protected boolean isTransportNameMatch(String transportName, String expectTransportName) {
    return ALL_TRANSPORT.equals(expectTransportName) || transportName.equals(expectTransportName);
  }

  protected abstract String findTransportName(DiscoveryContext context, DiscoveryTreeNode parent);

  protected abstract Object createEndpoint(String transportName, String endpoint, MicroserviceInstance instance);
}
