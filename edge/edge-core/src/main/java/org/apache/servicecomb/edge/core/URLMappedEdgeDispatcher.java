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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicPropertyFactory;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Provide a URL mapping based dispatcher. Users configure witch URL patterns dispatch to a target service.
 */
public class URLMappedEdgeDispatcher extends AbstractEdgeDispatcher {
  private static final Logger LOG = LoggerFactory.getLogger(URLMappedEdgeDispatcher.class);

  private static final String PATTERN_ANY = "/(.*)";

  private static final String KEY_ORDER = "servicecomb.http.dispatcher.edge.url.order";

  private static final String KEY_ENABLED = "servicecomb.http.dispatcher.edge.url.enabled";

  private static final String KEY_PATTERN = "servicecomb.http.dispatcher.edge.url.pattern";

  private static final String KEY_MAPPING_PREIX = "servicecomb.http.dispatcher.edge.url.mappings";

  private Map<String, URLMappedConfigurationItem> configurations = new HashMap<>();

  public URLMappedEdgeDispatcher() {
    if (this.enabled()) {
      loadConfigurations();
    }
  }

  @Override
  public int getOrder() {
    return DynamicPropertyFactory.getInstance().getIntProperty(KEY_ORDER, 30_000).get();
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(KEY_ENABLED, false).get();
  }

  @Override
  public void init(Router router) {
    // cookies handler are enabled by default start from 3.8.3
    String pattern = DynamicPropertyFactory.getInstance().getStringProperty(KEY_PATTERN, PATTERN_ANY).get();
    router.routeWithRegex(pattern).handler(createBodyHandler());
    router.routeWithRegex(pattern).failureHandler(this::onFailure).handler(this::onRequest);
  }

  private void loadConfigurations() {
    ConcurrentCompositeConfiguration config = (ConcurrentCompositeConfiguration) DynamicPropertyFactory
        .getBackingConfigurationSource();
    configurations = URLMappedConfigurationLoader.loadConfigurations(config, KEY_MAPPING_PREIX);
    config.addConfigurationListener(event -> {
      if (event.getPropertyName().startsWith(KEY_MAPPING_PREIX)) {
        LOG.info("Map rule have been changed. Reload configurations. Event=" + event.getType());
        configurations = URLMappedConfigurationLoader.loadConfigurations(config, KEY_MAPPING_PREIX);
      }
    });
  }


  protected void onRequest(RoutingContext context) {
    URLMappedConfigurationItem configurationItem = findConfigurationItem(context.request().path());
    if (configurationItem == null) {
      context.next();
      return;
    }

    String path = Utils.findActualPath(context.request().path(), configurationItem.getPrefixSegmentCount());

    EdgeInvocation edgeInvocation = createEdgeInvocation();
    if (configurationItem.getVersionRule() != null) {
      edgeInvocation.setVersionRule(configurationItem.getVersionRule());
    }
    edgeInvocation.init(configurationItem.getMicroserviceName(), context, path, httpServerFilters);
    edgeInvocation.edgeInvoke();
  }

  private URLMappedConfigurationItem findConfigurationItem(String path) {
    for (URLMappedConfigurationItem item : configurations.values()) {
      if (item.getPattern().matcher(path).matches()) {
        return item;
      }
    }
    return null;
  }
}
