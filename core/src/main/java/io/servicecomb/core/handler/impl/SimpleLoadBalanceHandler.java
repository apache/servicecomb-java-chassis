/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.core.handler.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.core.Endpoint;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.exception.ExceptionUtils;
import io.servicecomb.core.filter.TransportEndpointDiscoveryFilter;
import io.servicecomb.foundation.common.cache.VersionedCache;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.filter.DiscoveryFilter;
import io.servicecomb.serviceregistry.filter.DiscoveryFilterContext;
import io.servicecomb.serviceregistry.filter.DiscoveryFilterManager;
import io.servicecomb.swagger.invocation.AsyncResponse;

/**
 * 内置轮询lb，方便demo之类的场景，不必去依赖lb包
 */
public class SimpleLoadBalanceHandler extends AbstractHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleLoadBalanceHandler.class);

  private DiscoveryFilterManager filterManager = new DiscoveryFilterManager();

  // key为grouping filter qualified name
  private volatile Map<String, AtomicInteger> indexMap = new ConcurrentHashMap<>();

  public SimpleLoadBalanceHandler() {
    filterManager.loadFromSPI(DiscoveryFilter.class);
    filterManager.addFilter(new TransportEndpointDiscoveryFilter());
    filterManager.sort();
  }

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    VersionedCache instanceVersionedCache = RegistryUtils
        .getServiceRegistry()
        .getInstanceCacheManager()
        .getOrCreateVersionedCache(invocation.getAppId(),
            invocation.getMicroserviceName(),
            invocation.getMicroserviceVersionRule());

    DiscoveryFilterContext context = new DiscoveryFilterContext();
    context.setInputParameters(invocation);
    VersionedCache endpointsVersionedCache = filterManager.filter(context, instanceVersionedCache);
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
    int idx = Math.abs(index.getAndIncrement());
    idx = idx % endpoints.size();
    Endpoint endpoint = endpoints.get(idx);

    invocation.setEndpoint(endpoint);

    invocation.next(asyncResp);
  }
}
