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

import io.servicecomb.core.Endpoint;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.endpoint.EndpointsCache;
import io.servicecomb.core.exception.ExceptionUtils;
import io.servicecomb.swagger.invocation.AsyncResponse;

/**
 * 内置轮询lb，方便demo之类的场景，不必去依赖lb包
 */
public class SimpleLoadBalanceHandler extends AbstractHandler {
  private AtomicInteger index = new AtomicInteger();

  // key为transportName
  private volatile Map<String, EndpointsCache> endpointsCacheMap = new ConcurrentHashMap<>();

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    // 调用者未指定transport时，这里得到的是""，也直接使用，不必特殊处理
    String transportName = invocation.getConfigTransportName();

    EndpointsCache endpointsCache = endpointsCacheMap.get(transportName);
    if (endpointsCache == null) {
      synchronized (this) {
        endpointsCache = endpointsCacheMap.get(invocation.getConfigTransportName());
        if (endpointsCache == null) {
          endpointsCache = new EndpointsCache(invocation.getAppId(), invocation.getMicroserviceName(),
              invocation.getMicroserviceVersionRule(), transportName);
          endpointsCacheMap.put(transportName, endpointsCache);
        }
      }
    }
    List<Endpoint> endpoints = endpointsCache.getLatestEndpoints();

    if (endpoints == null || endpoints.isEmpty()) {
      asyncResp.consumerFail(ExceptionUtils.lbAddressNotFound(invocation.getMicroserviceName(),
          invocation.getMicroserviceVersionRule(),
          transportName));
      return;
    }

    int idx = Math.abs(index.getAndIncrement());
    idx = idx % endpoints.size();
    Endpoint endpoint = endpoints.get(idx);

    invocation.setEndpoint(endpoint);

    invocation.next(asyncResp);
  }
}
