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
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicPropertyFactory;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;

/**
 * Provide a URL mapping based dispatcher. Users configure witch URL patterns dispatch to a target service.
 */
public class URLMappedEdgeDispatcher extends AbstractEdgeDispatcher {
  class ConfigurationItem {
    String microserviceName;

    String versionRule;

    int prefixSegmentCount;

    Pattern pattern;

    String stringPattern;
  }

  private static final Logger LOG = LoggerFactory.getLogger(URLMappedEdgeDispatcher.class);

  private static final String PATTERN_ANY = "/(.*)";

  private static final String KEY_ENABLED = "servicecomb.http.dispatcher.edge.url.enabled";

  private static final String KEY_MAPPING_PREIX = "servicecomb.http.dispatcher.edge.url.mappings";

  private static final String KEY_MAPPING_PATH = ".path";

  private static final String KEY_MAPPING_SERVICE_NAME = "servicecomb.http.dispatcher.edge.url.mappings.%s.microserviceName";

  private static final String KEY_MAPPING_VERSION_RULE = "servicecomb.http.dispatcher.edge.url.mappings.%s.versionRule";

  private static final String KEY_MAPPING_PREFIX_SEGMENT_COUNT = "servicecomb.http.dispatcher.edge.url.mappings.%s.prefixSegmentCount";

  private Map<String, ConfigurationItem> configurations = new HashMap<>();

  public URLMappedEdgeDispatcher() {
    if(this.enabled()) {
      loadConfigurations();
    }
  }

  @Override
  public int getOrder() {
    return 30000;
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(KEY_ENABLED, false).get();
  }

  @Override
  public void init(Router router) {
    router.routeWithRegex(PATTERN_ANY).handler(CookieHandler.create());
    router.routeWithRegex(PATTERN_ANY).handler(createBodyHandler());
    router.routeWithRegex(PATTERN_ANY).failureHandler(this::onFailure).handler(this::onRequest);
  }

  private void loadConfigurations() {
    ConcurrentCompositeConfiguration config = (ConcurrentCompositeConfiguration) DynamicPropertyFactory
        .getBackingConfigurationSource();
    loadConfigurations(config);
    config.addConfigurationListener(event -> {
      if (event.getPropertyName().startsWith(KEY_MAPPING_PREIX)) {
        LOG.info("Map rule have been changed. Reload configurations. Event=" + event.getType());
        loadConfigurations(config);
      }
    });
  }

  private void loadConfigurations(ConcurrentCompositeConfiguration config) {
    Map<String, ConfigurationItem> configurations = new HashMap<>();
    Iterator<String> configsItems = config.getKeys(KEY_MAPPING_PREIX);
    while (configsItems.hasNext()) {
      String pathKey = configsItems.next();
      if (pathKey.endsWith(KEY_MAPPING_PATH)) {
        ConfigurationItem configurationItem = new ConfigurationItem();
        String pattern = DynamicPropertyFactory.getInstance()
            .getStringProperty(pathKey, null).get();
        if (StringUtils.isEmpty(pattern)) {
          continue;
        }
        configurationItem.pattern = Pattern.compile(pattern);
        configurationItem.stringPattern = pattern;
        String pathKeyItem = pathKey
            .substring(KEY_MAPPING_PREIX.length() + 1, pathKey.length() - KEY_MAPPING_PATH.length());
        configurationItem.microserviceName = DynamicPropertyFactory.getInstance()
            .getStringProperty(String.format(KEY_MAPPING_SERVICE_NAME, pathKeyItem), null).get();
        if (StringUtils.isEmpty(configurationItem.microserviceName)) {
          continue;
        }
        configurationItem.prefixSegmentCount = DynamicPropertyFactory.getInstance()
            .getIntProperty(String.format(KEY_MAPPING_PREFIX_SEGMENT_COUNT, pathKeyItem), 0).get();
        configurationItem.versionRule = DynamicPropertyFactory.getInstance()
            .getStringProperty(String.format(KEY_MAPPING_VERSION_RULE, pathKeyItem), "0.0.0+").get();
        configurations.put(pathKeyItem, configurationItem);
      }
    }
    this.configurations = configurations;
    logConfigurations();
  }

  private void logConfigurations() {
    for (String key : this.configurations.keySet()) {
      ConfigurationItem item = this.configurations.get(key);
      LOG.info("config item: key=" + key + ";pattern=" + item.stringPattern + ";service=" + item.microserviceName
          + ";versionRule=" + item.versionRule);
    }
  }

  protected void onRequest(RoutingContext context) {
    ConfigurationItem configurationItem = findConfigurationItem(context.request().path());
    if (configurationItem == null) {
      context.next();
      return;
    }

    String path = Utils.findActualPath(context.request().path(), configurationItem.prefixSegmentCount);

    EdgeInvocation edgeInvocation = new EdgeInvocation();
    if (configurationItem.versionRule != null) {
      edgeInvocation.setVersionRule(configurationItem.versionRule);
    }
    edgeInvocation.init(configurationItem.microserviceName, context, path, httpServerFilters);
    edgeInvocation.edgeInvoke();
  }

  private ConfigurationItem findConfigurationItem(String path) {
    for (ConfigurationItem item : configurations.values()) {
      if (item.pattern.matcher(path).matches()) {
        return item;
      }
    }
    return null;
  }
}
