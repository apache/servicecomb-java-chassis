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

package org.apache.servicecomb.registry.discovery;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEndpointDiscoveryFilter implements DiscoveryFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEndpointDiscoveryFilter.class);

  private static final String ALL_TRANSPORT = "";

  private static final String WEBSOCKET_TRANSPORT = "websocket";

  private static final String REST_TRANSPORT = "rest";

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

  protected DiscoveryTreeNode createDiscoveryTreeNode(String expectTransportName, DiscoveryContext context,
      DiscoveryTreeNode parent) {
    List<Object> endpoints = new ArrayList<>();
    List<StatefulDiscoveryInstance> instances = parent.data();
    for (StatefulDiscoveryInstance instance : instances) {
      for (String endpoint : instance.getEndpoints()) {
        try {
          URIEndpointObject endpointObject = new URIEndpointObject(endpoint);
          if (!isTransportNameMatch(endpointObject.getSchema(), expectTransportName,
              endpointObject.isWebsocketEnabled())) {
            continue;
          }

          Object objEndpoint = createEndpoint(context, expectTransportName, endpoint, instance);
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

  protected boolean isTransportNameMatch(String transportName, String expectTransportName, boolean isWebSocket) {
    if (ALL_TRANSPORT.equals(expectTransportName)) {
      return true;
    }
    if (WEBSOCKET_TRANSPORT.equals(expectTransportName)) {
      return isWebSocket;
    }
    return transportName.equals(expectTransportName);
  }

  protected abstract String findTransportName(DiscoveryContext context, DiscoveryTreeNode parent);

  protected abstract Object createEndpoint(DiscoveryContext context, String transportName, String endpoint,
      StatefulDiscoveryInstance instance);
}
