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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.core.filter.ProviderFilter;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

public class RouterAddHeaderFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RouterAddHeaderFilter.class);

  private static final String SERVICECOMB_ROUTER_HEADER = "servicecomb.router.header";

  private List<String> allHeader = new ArrayList<>();

  public RouterAddHeaderFilter() {
    loadHeaders();
  }

  /**
   * read config and get Header
   */
  private void loadHeaders() {
    addAllHeaders(LegacyPropertyFactory.getStringProperty(SERVICECOMB_ROUTER_HEADER));
  }

  private void addAllHeaders(String str) {
    if (StringUtils.isEmpty(str)) {
      return;
    }
    try {
      allHeader = Arrays.asList(str.split(","));
    } catch (Exception e) {
      LOGGER.error("route management Serialization failed: {}", e.getMessage());
    }
  }

  private Map<String, String> getHeaderMap(HttpServletRequestEx httpServletRequestEx) {
    Map<String, String> headerMap = new HashMap<>();
    allHeader.forEach(headerKey -> {
      String val = httpServletRequestEx.getHeader(headerKey);
      if (!StringUtils.isEmpty(val)) {
        headerMap.put(headerKey, httpServletRequestEx.getHeader(headerKey));
      }
    });
    return headerMap;
  }

  @Override
  public int getOrder(InvocationType invocationType, String application, String serviceName) {
    return ProviderFilter.PROVIDER_SCHEDULE_FILTER_ORDER - 1970;
  }

  @Override
  public boolean enabledForInvocationType(InvocationType invocationType) {
    // enable for both edge and producer
    return true;
  }

  @Override
  public String getName() {
    return "router-add-header";
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    if (!invocation.isEdge() && !invocation.isProducer()) {
      return nextNode.onFilter(invocation);
    }

    if (!StringUtils.isEmpty(invocation.getContext(RouterServerListFilter.ROUTER_HEADER))) {
      return nextNode.onFilter(invocation);
    }

    if (allHeader.isEmpty()) {
      return nextNode.onFilter(invocation);
    }

    Map<String, String> headerMap = getHeaderMap(invocation.getRequestEx());
    try {
      invocation.addContext(RouterServerListFilter.ROUTER_HEADER,
          JsonUtils.OBJ_MAPPER.writeValueAsString(headerMap));
    } catch (JsonProcessingException e) {
      LOGGER.error("canary context serialization failed");
    }
    return nextNode.onFilter(invocation);
  }
}
