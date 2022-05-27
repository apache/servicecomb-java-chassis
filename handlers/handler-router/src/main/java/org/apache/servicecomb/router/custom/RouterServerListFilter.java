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
package org.apache.servicecomb.router.custom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.loadbalance.ServerListFilterExt;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.router.RouterFilter;
import org.apache.servicecomb.router.distribute.RouterDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.netflix.config.DynamicPropertyFactory;

public class RouterServerListFilter implements ServerListFilterExt {

  private static final Logger LOGGER = LoggerFactory.getLogger(RouterServerListFilter.class);

  private static final String ENABLE = "servicecomb.router.type";

  private static final String TYPE_ROUTER = "router";

  public static final String ROUTER_HEADER = "X-RouterContext";

  @SuppressWarnings("unchecked")
  private final RouterDistributor<ServiceCombServer, Microservice> routerDistributor = BeanUtils
      .getBean(RouterDistributor.class);

  private final RouterFilter routerFilter = BeanUtils.getBean(RouterFilter.class);

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance().getStringProperty(ENABLE, "").get()
        .equals(TYPE_ROUTER);
  }

  @Override
  public List<ServiceCombServer> getFilteredListOfServers(List<ServiceCombServer> list,
      Invocation invocation) {
    String targetServiceName = invocation.getMicroserviceName();
    Map<String, String> headers = addHeaders(invocation);
    headers = filterHeaders(headers);
    return routerFilter
        .getFilteredListOfServers(list, targetServiceName, headers,
            routerDistributor);
  }

  private Map<String, String> filterHeaders(Map<String, String> headers) {
    List<RouterHeaderFilterExt> filters = SPIServiceUtils
        .getOrLoadSortedService(RouterHeaderFilterExt.class);
    for (RouterHeaderFilterExt filterExt : filters) {
      if (filterExt.enabled()) {
        headers = filterExt.doFilter(headers);
      }
    }
    return headers;
  }

  private Map<String, String> addHeaders(Invocation invocation) {
    Map<String, String> headers = new HashMap<>();
    if (invocation.getContext(ROUTER_HEADER) != null) {
      Map<String, String> canaryContext = null;
      try {
        canaryContext = JsonUtils.OBJ_MAPPER
            .readValue(invocation.getContext(ROUTER_HEADER),
                new TypeReference<Map<String, String>>() {
                });
      } catch (JsonProcessingException e) {
        LOGGER.error("canary context serialization failed");
      }
      if (canaryContext != null) {
        headers.putAll(canaryContext);
      }
    }
    invocation.getInvocationArguments().forEach((k, v) -> headers.put(k, v == null ? null : v.toString()));
    headers.putAll(invocation.getContext());
    return headers;
  }
}
