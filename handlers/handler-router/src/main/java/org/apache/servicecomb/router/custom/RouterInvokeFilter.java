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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.router.cache.RouterRuleCache;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

public class RouterInvokeFilter implements HttpServerFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RouterInvokeFilter.class);

  private static final String SERVICECOMB_ROUTER_HEADER = "servicecomb.router.header";

  private static final String ROUTER_HEADER = "X-RouterContext";

  private static List<String> allHeader = new ArrayList<>();


  @Override
  public int getOrder() {
    return -90;
  }

  @Override
  public boolean enabled() {
    return true;
  }

  /**
   * pass through headers
   *
   * @param invocation
   * @param httpServletRequestEx
   * @return
   */
  @Override
  public Response afterReceiveRequest(Invocation invocation,
      HttpServletRequestEx httpServletRequestEx) {
    if (!isHaveHeadersRule()) {
      return null;
    }
    if (!RouterRuleCache.isServerContainRule(invocation.getMicroserviceName())) {
      return null;
    }
    if (loadHeaders()) {
      Map<String, String> headerMap = getHeaderMap(httpServletRequestEx);
      try {
        invocation.addContext(ROUTER_HEADER, JsonUtils.OBJ_MAPPER.writeValueAsString(headerMap));
      } catch (JsonProcessingException e) {
        LOGGER.error("canary context serialization failed");
      }
    }
    return null;
  }

  /**
   * read config and get Header
   */
  private boolean loadHeaders() {
    if (!CollectionUtils.isEmpty(allHeader)) {
      return true;
    }
    DynamicStringProperty headerStr = DynamicPropertyFactory.getInstance()
        .getStringProperty(SERVICECOMB_ROUTER_HEADER, null, () -> {
          DynamicStringProperty temHeader = DynamicPropertyFactory.getInstance()
              .getStringProperty(SERVICECOMB_ROUTER_HEADER, null);
          if (!addAllHeaders(temHeader.get())) {
            allHeader = new ArrayList<>();
          }
        });
    return addAllHeaders(headerStr.get());
  }

  /**
   * if don't have headers rule , avoid registered too many callback
   */
  private boolean isHaveHeadersRule() {
    DynamicStringProperty headerStr = DynamicPropertyFactory.getInstance()
        .getStringProperty(SERVICECOMB_ROUTER_HEADER, null);
    return !StringUtils.isEmpty(headerStr.get());
  }

  private boolean addAllHeaders(String str) {
    if (StringUtils.isEmpty(str)) {
      return false;
    }
    try {
      if (CollectionUtils.isEmpty(allHeader)) {
        Yaml yaml = new Yaml();
        allHeader = yaml.load(str);
      }
    } catch (Exception e) {
      LOGGER.error("route management Serialization failed: {}", e.getMessage());
      return false;
    }
    return true;
  }

  /**
   * get header from request
   *
   * @param httpServletRequestEx
   * @return
   */
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
}
