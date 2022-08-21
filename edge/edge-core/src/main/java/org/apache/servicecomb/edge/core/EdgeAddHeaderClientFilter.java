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
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.filter.HttpClientFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;

public class EdgeAddHeaderClientFilter implements HttpClientFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(EdgeAddHeaderClientFilter.class);

  private static final String PREFIX = "servicecomb.edge.filter.addHeader";

  private static final String KEY_ENABLED = PREFIX + ".enabled";

  private static final String KEY_HEADERS = PREFIX + ".allowedHeaders";

  private List<String> publicHeaders = new ArrayList<>();

  private boolean enabled = false;

  public EdgeAddHeaderClientFilter() {
    init();
    ConfigurationManager.getConfigInstance()
        .addConfigurationListener(event -> {
          if (StringUtils.startsWith(event.getPropertyName(), PREFIX)) {
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
  public CompletableFuture<Void> beforeSendRequestAsync(Invocation invocation, HttpServletRequestEx requestEx) {
    addHeaders(invocation, requestEx::addHeader);
    return CompletableFuture.completedFuture(null);
  }

  public void addHeaders(Invocation invocation, BiConsumer<String, String> headerAdder) {
    if (!invocation.isEdge()) {
      return;
    }

    HttpServletRequestEx oldRequest = invocation.getRequestEx();
    publicHeaders.forEach(key -> {
      String value = oldRequest.getHeader(key);
      if (StringUtils.isNotEmpty(value)) {
        headerAdder.accept(key, value);
      }
    });
  }

  @Override
  public Response afterReceiveResponse(Invocation invocation, HttpServletResponseEx responseEx) {
    return null;
  }
}
