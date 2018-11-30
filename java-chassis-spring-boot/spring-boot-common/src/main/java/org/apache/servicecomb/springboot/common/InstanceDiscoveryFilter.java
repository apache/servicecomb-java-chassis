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
package org.apache.servicecomb.springboot.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryContext;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryFilter;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceDiscoveryFilter implements DiscoveryFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceDiscoveryFilter.class);

  public interface InstanceFactory {
    Object createInstance(String name, URIEndpointObject uri);
  }

  InstanceFactory instanceFactory;

  public InstanceDiscoveryFilter(InstanceFactory factory){
    instanceFactory = factory;
  }

  @Override
  public int getOrder() {
    return Short.MAX_VALUE;
  }

  @Override
  public DiscoveryTreeNode discovery(DiscoveryContext context, DiscoveryTreeNode parent) {
    return parent.children()
        .computeIfAbsent(context.getInputParameters(), etn -> createDiscoveryTreeNode(context, parent));
  }

  @SuppressWarnings("unchecked")
  protected DiscoveryTreeNode createDiscoveryTreeNode(DiscoveryContext context,
      DiscoveryTreeNode parent) {
    String serviceName = context.getInputParameters();

    List<Object> instances = new ArrayList<>();
    for (MicroserviceInstance instance : ((Map<String, MicroserviceInstance>) parent.data()).values()) {
      for (String endpoint : instance.getEndpoints()) {
        String scheme = endpoint.split(":", 2)[0];
        if (!scheme.equalsIgnoreCase("rest")) {
          LOGGER.info("Endpoint {} is not supported in Spring Cloud, ignoring.", endpoint);
          continue;
        }
        URIEndpointObject uri = new URIEndpointObject(endpoint);
        instances.add(instanceFactory.createInstance(serviceName, uri));
      }
    }

    return new DiscoveryTreeNode()
        .subName(parent, serviceName)
        .data(instances);
  }
};