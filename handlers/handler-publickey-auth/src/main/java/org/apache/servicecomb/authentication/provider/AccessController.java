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
package org.apache.servicecomb.authentication.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.ConfigurationChangedEvent;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.Subscribe;

/**
 * Add black / white list control to service access
 */
public class AccessController {
  static class ConfigurationItem {
    static final String CATEGORY_PROPERTY = "property";

    String category;

    String propertyName;

    String rule;
  }

  private static final Logger LOG = LoggerFactory.getLogger(AccessController.class);

  private static final String KEY_WHITE_LIST_PREFIX = "servicecomb.publicKey.accessControl.white";

  private static final String KEY_BLACK_LIST_PREFIX = "servicecomb.publicKey.accessControl.black";

  private static final String KEY_PROPERTY_NAME = "%s.%s.propertyName";

  private static final String KEY_CATEGORY = "%s.%s.category";

  private static final String KEY_RULE_POSTFIX = ".rule";

  private final Environment environment;

  private Map<String, ConfigurationItem> whiteList = new HashMap<>();

  private Map<String, ConfigurationItem> blackList = new HashMap<>();

  public AccessController(Environment environment) {
    this.environment = environment;
    loadConfigurations(KEY_BLACK_LIST_PREFIX);
    loadConfigurations(KEY_WHITE_LIST_PREFIX);
    EventManager.register(this);
  }

  public boolean isAllowed(DiscoveryInstance microservice) {
    return whiteAllowed(microservice) && !blackDenied(microservice);
  }

  private boolean whiteAllowed(DiscoveryInstance microservice) {
    if (whiteList.isEmpty()) {
      return true;
    }
    return matchFound(microservice, whiteList);
  }

  private boolean blackDenied(DiscoveryInstance microservice) {
    if (blackList.isEmpty()) {
      return false;
    }
    return matchFound(microservice, blackList);
  }

  private boolean matchFound(DiscoveryInstance microservice, Map<String, ConfigurationItem> ruleList) {
    boolean matched = false;
    for (ConfigurationItem item : ruleList.values()) {
      if (ConfigurationItem.CATEGORY_PROPERTY.equals(item.category)) {
        // we support to configure properties, e.g. serviceName, appId, environment, alias, version and so on, also support key in properties.
        if (matchMicroserviceField(microservice, item) || matchMicroserviceProperties(microservice, item)) {
          return true;
        }
      }
    }
    return matched;
  }

  private boolean matchMicroserviceProperties(DiscoveryInstance microservice, ConfigurationItem item) {
    Map<String, String> properties = microservice.getProperties();
    for (Entry<String, String> entry : properties.entrySet()) {
      if (!entry.getKey().equals(item.propertyName)) {
        continue;
      }
      return isPatternMatch(entry.getValue(), item.rule);
    }
    return false;
  }

  private boolean matchMicroserviceField(DiscoveryInstance microservice, ConfigurationItem item) {
    String fieldValue;
    if ("version".equals(item.propertyName)) {
      fieldValue = microservice.getVersion();
    } else if ("serviceName".equals(item.propertyName)) {
      fieldValue = microservice.getServiceName();
    } else {
      fieldValue = microservice.getProperties().get(item.propertyName);
    }
    return isPatternMatch(fieldValue, item.rule);
  }

  private boolean isPatternMatch(String value, String pattern) {
    if (pattern.startsWith("*")) {
      return value.endsWith(pattern.substring(1));
    }
    if (pattern.endsWith("*")) {
      return value.startsWith(pattern.substring(0, pattern.length() - 1));
    }
    return value.equals(pattern);
  }

  @Subscribe
  public void onConfigurationChangedEvent(ConfigurationChangedEvent event) {
    Map<String, Object> changed = new HashMap<>();
    changed.putAll(event.getDeleted());
    changed.putAll(event.getAdded());
    changed.putAll(event.getUpdated());

    for (Entry<String, Object> entry : changed.entrySet()) {
      if (entry.getKey().startsWith(KEY_WHITE_LIST_PREFIX)) {
        loadConfigurations(KEY_WHITE_LIST_PREFIX);
        break;
      }
    }
    for (Entry<String, Object> entry : changed.entrySet()) {
      if (entry.getKey().startsWith(KEY_BLACK_LIST_PREFIX)) {
        loadConfigurations(KEY_BLACK_LIST_PREFIX);
        break;
      }
    }
  }

  private void loadConfigurations(String prefix) {
    Map<String, ConfigurationItem> configurations = new HashMap<>();
    Set<String> configsItems = ConfigUtil.propertiesWithPrefix((ConfigurableEnvironment) environment, prefix);
    for (String pathKey : configsItems) {
      if (pathKey.endsWith(KEY_RULE_POSTFIX)) {
        ConfigurationItem configurationItem = new ConfigurationItem();
        String rule = environment.getProperty(pathKey);
        if (StringUtils.isEmpty(rule)) {
          continue;
        }
        configurationItem.rule = rule;
        String pathKeyItem = pathKey
            .substring(prefix.length() + 1, pathKey.length() - KEY_RULE_POSTFIX.length());
        configurationItem.propertyName = environment.getProperty(String.format(KEY_PROPERTY_NAME, prefix, pathKeyItem));
        if (StringUtils.isEmpty(configurationItem.propertyName)) {
          continue;
        }
        configurationItem.category = environment.getProperty(String.format(KEY_CATEGORY, prefix, pathKeyItem));
        if (StringUtils.isEmpty(configurationItem.category)) {
          continue;
        }
        configurations.put(pathKeyItem, configurationItem);
      }
    }

    if (KEY_WHITE_LIST_PREFIX.equals(prefix)) {
      this.whiteList = configurations;
      logConfigurations(configurations, true);
    } else {
      this.blackList = configurations;
      logConfigurations(configurations, false);
    }
  }

  private void logConfigurations(Map<String, ConfigurationItem> configurations, boolean isWhite) {
    configurations.forEach((key, item) -> LOG.info((isWhite ? "White list " : "Black list ") + "config item: key=" + key
        + ";category=" + item.category
        + ";propertyName=" + item.propertyName
        + ";rule=" + item.rule));
  }
}
