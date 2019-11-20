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
package org.apache.servicecomb.router.constom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.loadbalance.ServerListFilterExt;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import com.netflix.config.DynamicPropertyFactory;

import org.apache.servicecomb.router.RouterFilter;
import org.apache.servicecomb.router.distribute.RouterDistributor;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CanaryServerListFilter implements ServerListFilterExt {

  private static final Logger LOGGER = LoggerFactory.getLogger(CanaryServerListFilter.class);

  private static final String ENABLE = "servicecomb.router.type";

  private static final String TYPE_ROUTER = "router";

  private static final String ROUTER_HEADER = "X-RouterContext";

  RouterDistributor<ServiceCombServer, Microservice> distributer = new ServiceCombCanaryDistributer();

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance().getStringProperty(ENABLE, "").get()
        .equals("TYPE_ROUTER");
  }

  @Override
  public List<ServiceCombServer> getFilteredListOfServers(List<ServiceCombServer> list,
      Invocation invocation) {
    String targetServiceName = invocation.getMicroserviceName();
    Map<String, String> headers = addHeaders(invocation);
    return RouterFilter
        .getFilteredListOfServers(list, targetServiceName, headers,
            distributer);
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
    for (int i = 0; i < invocation.getArgs().length; i++) {
      if (invocation.getOperationMeta().getParamName(i) != null &&
          invocation.getArgs()[i] != null) {
        headers
            .put(invocation.getOperationMeta().getParamName(i), invocation.getArgs()[i].toString());
      }
    }
    headers.putAll(invocation.getContext());
    return headers;
  }
}
