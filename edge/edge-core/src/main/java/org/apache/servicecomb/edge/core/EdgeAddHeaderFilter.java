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

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.ConfigurationChangedEvent;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.filter.AbstractFilter;
import org.apache.servicecomb.core.filter.EdgeFilter;
import org.apache.servicecomb.core.filter.Filter;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.transport.rest.client.RestClientTransportContext;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.Subscribe;

public class EdgeAddHeaderFilter extends AbstractFilter implements EdgeFilter {
  public static final String NAME = "edge-add-headers";

  private static final String PREFIX = "servicecomb.edge.filter.addHeader";

  private static final String KEY_ENABLED = PREFIX + ".enabled";

  private static final String KEY_HEADERS = PREFIX + ".allowedHeaders";

  private final Environment environment;

  private List<String> publicHeaders = new ArrayList<>();

  private boolean enabled = false;

  public EdgeAddHeaderFilter(Environment environment) {
    this.environment = environment;
    init();
    EventManager.register(this);
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onConfigurationChangedEvent(ConfigurationChangedEvent event) {
    for (String changed : event.getChanged()) {
      if (changed.startsWith(PREFIX)) {
        init();
        break;
      }
    }
  }

  private void init() {
    enabled = environment.getProperty(KEY_ENABLED, boolean.class, true);
    String publicHeaderStr = environment.getProperty(KEY_HEADERS, "");
    String[] split = publicHeaderStr.split(",");
    if (split.length > 0) {
      publicHeaders = Arrays.asList(split);
    }
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean enabledForTransport(String transport) {
    return enabled;
  }

  @Override
  public int getOrder() {
    return Filter.CONSUMER_LOAD_BALANCE_ORDER + 1991;
  }

  @Override
  public CompletableFuture<Response> onFilter(Invocation invocation, FilterNode nextNode) {
    if (publicHeaders.isEmpty()) {
      return nextNode.onFilter(invocation);
    }

    RestClientTransportContext transportContext = invocation.getTransportContext();
    HttpServletRequestEx oldRequest = invocation.getRequestEx();
    publicHeaders.forEach(key -> {
      String value = oldRequest.getHeader(key);
      if (StringUtils.isNotEmpty(value)) {
        transportContext.getHttpClientRequest().putHeader(key, value);
      }
    });

    return nextNode.onFilter(invocation);
  }
}
