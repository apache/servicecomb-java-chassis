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
package org.apache.servicecomb.core.filter.impl;

import static org.apache.servicecomb.core.exception.ExceptionCodes.LB_ADDRESS_NOT_FOUND;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.Exceptions;
import org.apache.servicecomb.core.filter.ConsumerFilter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.governance.RetryContext;
import org.apache.servicecomb.core.handler.impl.SimpleLoadBalanceHandler;
import org.apache.servicecomb.core.registry.discovery.EndpointDiscoveryFilter;
import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryFilter;
import org.apache.servicecomb.registry.discovery.DiscoveryTree;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * build-in round robin LB, for demo scenes
 */
@Component
public class SimpleLoadBalanceFilter implements ConsumerFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleLoadBalanceHandler.class);

  public static final String NAME = "simple-load-balance";

  private static class Service {
    public static final String CONTEXT_KEY_LAST_SERVER = "x-context-last-server";

    // Enough times to make sure to choose a different server in high volume.
    private static final int COUNT = 17;

    private final String name;

    private final DiscoveryTree discoveryTree = new DiscoveryTree();

    // key is grouping filter qualified name
    private final Map<String, AtomicInteger> indexMap = new ConcurrentHashMapEx<>();

    public Service(String name) {
      this.name = name;
      discoveryTree.loadFromSPI(DiscoveryFilter.class);
      discoveryTree.addFilter(new EndpointDiscoveryFilter());
      discoveryTree.sort();
    }

    public String getName() {
      return name;
    }

    public Endpoint selectEndpoint(Invocation invocation) {
      DiscoveryContext context = new DiscoveryContext();
      context.setInputParameters(invocation);
      VersionedCache endpointsVersionedCache = discoveryTree.discovery(context,
          invocation.getAppId(),
          invocation.getMicroserviceName(),
          invocation.getMicroserviceVersionRule());
      if (endpointsVersionedCache.isEmpty()) {
        String msg = "No available address found.";
        LOGGER.error("{} microserviceName={}, version={}, discoveryGroupName={}",
            msg,
            invocation.getMicroserviceName(),
            invocation.getMicroserviceVersionRule(),
            endpointsVersionedCache.name());
        throw Exceptions.consumer(LB_ADDRESS_NOT_FOUND, msg);
      }

      return selectEndpoint(invocation, endpointsVersionedCache.name(), endpointsVersionedCache.data());
    }

    private Endpoint selectEndpoint(Invocation invocation, String key, List<Endpoint> endpoints) {
      RetryContext retryContext = invocation.getLocalContext(RetryContext.RETRY_CONTEXT);
      if (retryContext == null) {
        return chooseEndpoint(invocation, key, endpoints);
      }

      if (!retryContext.isRetry()) {
        Endpoint server = chooseEndpoint(invocation, key, endpoints);
        invocation.addLocalContext(CONTEXT_KEY_LAST_SERVER, server);
        return server;
      }

      Endpoint lastServer = invocation.getLocalContext(CONTEXT_KEY_LAST_SERVER);
      Endpoint nextServer = lastServer;
      if (!retryContext.trySameServer()) {
        for (int i = 0; i < COUNT; i++) {
          Endpoint s = chooseEndpoint(invocation, key, endpoints);
          if (s == null) {
            break;
          }
          if (!s.equals(nextServer)) {
            nextServer = s;
            break;
          }
        }
      }

      LOGGER.info("operation failed {}, retry to instance [{}], last instance [{}], trace id {}",
          invocation.getMicroserviceQualifiedName(),
          nextServer == null ? "" : nextServer.getEndpoint(),
          lastServer == null ? "" : lastServer.getEndpoint(),
          invocation.getTraceId());
      invocation.addLocalContext(CONTEXT_KEY_LAST_SERVER, nextServer);
      return nextServer;
    }

    private Endpoint chooseEndpoint(Invocation invocation, String key, List<Endpoint> endpoints) {
      AtomicInteger index = indexMap.computeIfAbsent(key, name -> {
        LOGGER.info("Create loadBalancer for {}.", name);
        return new AtomicInteger();
      });
      LOGGER.debug("invocation {} use discoveryGroup {}.",
          invocation.getMicroserviceQualifiedName(),
          key);

      int idx = Math.abs(index.getAndIncrement());
      idx = idx % endpoints.size();

      return endpoints.get(idx);
    }
  }


  private final Map<String, Service> servicesByName = new ConcurrentHashMapEx<>();

  @Nonnull
  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    if (invocation.getEndpoint() != null) {
      invocation.addLocalContext(RetryContext.RETRY_LOAD_BALANCE, false);
      return nextNode.onFilter(invocation);
    }
    invocation.addLocalContext(RetryContext.RETRY_LOAD_BALANCE, true);
    Service service = servicesByName.computeIfAbsent(invocation.getMicroserviceName(), Service::new);
    Endpoint endpoint = service.selectEndpoint(invocation);
    invocation.setEndpoint(endpoint);
    return nextNode.onFilter(invocation);
  }
}
