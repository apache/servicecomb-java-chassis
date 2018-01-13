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

package org.apache.servicecomb.core.handler.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.ExceptionUtils;
import org.apache.servicecomb.core.filter.EndpointDiscoveryFilter;
import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryContext;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryFilter;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTree;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 内置轮询lb，方便demo之类的场景，不必去依赖lb包
 */
public class SimpleLoadBalanceHandler implements Handler {
  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleLoadBalanceHandler.class);

  private DiscoveryTree discoveryTree = new DiscoveryTree();

  // key为grouping filter qualified name
  private volatile Map<String, AtomicInteger> indexMap = new ConcurrentHashMapEx<>();

  public SimpleLoadBalanceHandler() {
    discoveryTree.loadFromSPI(DiscoveryFilter.class);
    discoveryTree.addFilter(new EndpointDiscoveryFilter());
    discoveryTree.sort();
  }

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    DiscoveryContext context = new DiscoveryContext();
    context.setInputParameters(invocation);
    VersionedCache endpointsVersionedCache = discoveryTree.discovery(context,
        invocation.getAppId(),
        invocation.getMicroserviceName(),
        invocation.getMicroserviceVersionRule());
    if (endpointsVersionedCache.isEmpty()) {
      asyncResp.consumerFail(ExceptionUtils.lbAddressNotFound(invocation.getMicroserviceName(),
          invocation.getMicroserviceVersionRule(),
          endpointsVersionedCache.name()));
      return;
    }

    List<Endpoint> endpoints = endpointsVersionedCache.data();
    AtomicInteger index = indexMap.computeIfAbsent(endpointsVersionedCache.name(), name -> {
      LOGGER.info("Create loadBalancer for {}.", name);
      return new AtomicInteger();
    });
    LOGGER.debug("invocation {} use discoveryGroup {}.",
        invocation.getMicroserviceQualifiedName(),
        endpointsVersionedCache.name());

    int idx = Math.abs(index.getAndIncrement());
    idx = idx % endpoints.size();
    Endpoint endpoint = endpoints.get(idx);

    invocation.setEndpoint(endpoint);

    invocation.next(asyncResp);
  }
}
