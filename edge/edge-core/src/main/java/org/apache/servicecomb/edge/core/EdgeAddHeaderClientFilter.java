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
package org.apache.servicecomb.edge.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.filter.HttpClientFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicPropertyFactory;

public class EdgeAddHeaderClientFilter implements HttpClientFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(EdgeAddHeaderClientFilter.class);

  private static final String KEY_ENABLED = "servicecomb.edge.filter.addHeader.enabled";

  private static final String KEY_HEADERS = "servicecomb.edge.filter.addHeader.allowedHeaders";

  private List<String> publicHeaders = new ArrayList<>();

  private boolean enabled = false;

  public EdgeAddHeaderClientFilter() {
    init();
    ((ConcurrentCompositeConfiguration) DynamicPropertyFactory
        .getBackingConfigurationSource()).addConfigurationListener(event -> {
      if (event.getPropertyName().startsWith(KEY_HEADERS) || event.getPropertyName().startsWith(KEY_ENABLED)) {
        LOGGER.info("Public headers config have been changed. Event=" + event.getType());
        init();
      }
    });
  }

  private void init() {
    enabled = DynamicPropertyFactory.getInstance().getBooleanProperty(KEY_ENABLED, false).get();
    String publicHeaderStr = DynamicPropertyFactory.getInstance().getStringProperty(KEY_HEADERS, "").get();
    String[] split = publicHeaderStr.split(",");
    if (split.length > 0) {
      publicHeaders = Arrays.asList(split);
    }
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public boolean enabled() {
    return enabled;
  }

  @Override
  public void beforeSendRequest(Invocation invocation, HttpServletRequestEx requestEx) {
    if (!invocation.isEdge()) {
      return;
    }
    HttpServletRequestEx oldRequest = invocation.getRequestEx();
    publicHeaders.forEach(key -> {
      if (StringUtils.isEmpty(oldRequest.getHeader(key))) {
        return;
      }
      requestEx.addHeader(key, oldRequest.getHeader(key));
    });
  }

  @Override
  public Response afterReceiveResponse(Invocation invocation, HttpServletResponseEx responseEx) {
    return null;
  }
}
