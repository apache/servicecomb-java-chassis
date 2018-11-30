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

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicPropertyFactory;

/**
 * Add black / white list control to service access
 */
public class AccessController {
  class ConfigurationItem {
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

  private Map<String, ConfigurationItem> whiteList = new HashMap<>();

  private Map<String, ConfigurationItem> blackList = new HashMap<>();

  public AccessController() {
    loadConfigurations(KEY_BLACK_LIST_PREFIX);
    loadConfigurations(KEY_WHITE_LIST_PREFIX);
  }

  public boolean isAllowed(Microservice microservice) {
    return whiteAllowed(microservice) && !blackDenied(microservice);
  }

  private boolean whiteAllowed(Microservice microservice) {
    if(whiteList.isEmpty()) {
      return true;
    }
    return matchFound(microservice, whiteList);
  }

  private boolean blackDenied(Microservice microservice) {
    if(blackList.isEmpty()) {
      return false;
    }
    return matchFound(microservice, blackList);
  }

  private boolean matchFound(Microservice microservice, Map<String, ConfigurationItem> ruleList) {
    boolean matched = false;
    for (ConfigurationItem item : ruleList.values()) {
      if (ConfigurationItem.CATEGORY_PROPERTY.equals(item.category)) {
        // we support to configure properties, e.g. serviceName, appId, environment, alias, version and so on, also support key in properties.
        if (matchMicroserviceField(microservice, item) || matchMicroserviceProperties(microservice, item))
          return true;
      }
    }
    return matched;
  }

  private boolean matchMicroserviceProperties(Microservice microservice, ConfigurationItem item) {
    Map<String, String> properties = microservice.getProperties();
    for (Entry<String, String> entry : properties.entrySet()) {
      if (!entry.getKey().equals(item.propertyName))
        continue;
      return isPatternMatch(entry.getValue(), item.rule);
    }
    return false;
  }

  private boolean matchMicroserviceField(Microservice microservice, ConfigurationItem item) {
    Object fieldValue = null;
    try {
      fieldValue = new PropertyDescriptor(item.propertyName, Microservice.class).getReadMethod().invoke(microservice);
    } catch (Exception e) {
      LOG.warn("can't find propertyname: {} in microservice field, will search in microservice properties.", item.propertyName);
      return false;
    }
    if (fieldValue.getClass().getName().equals(String.class.getName()))
      return isPatternMatch((String) fieldValue, item.rule);
    return false;
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

  private void loadConfigurations(String prefix) {
    ConcurrentCompositeConfiguration config = (ConcurrentCompositeConfiguration) DynamicPropertyFactory
        .getBackingConfigurationSource();
    loadConfigurations(config, prefix);
    config.addConfigurationListener(event -> {
      if (event.getPropertyName().startsWith(prefix)) {
        LOG.info("Access rule have been changed. Reload configurations. Event=" + event.getType());
        loadConfigurations(config, prefix);
      }
    });
  }

  private void loadConfigurations(ConcurrentCompositeConfiguration config, String prefix) {
    Map<String, ConfigurationItem> configurations = new HashMap<>();
    Iterator<String> configsItems = config.getKeys(prefix);
    while (configsItems.hasNext()) {
      String pathKey = configsItems.next();
      if (pathKey.endsWith(KEY_RULE_POSTFIX)) {
        ConfigurationItem configurationItem = new ConfigurationItem();
        String rule = DynamicPropertyFactory.getInstance()
            .getStringProperty(pathKey, null).get();
        if (StringUtils.isEmpty(rule)) {
          continue;
        }
        configurationItem.rule = rule;
        String pathKeyItem = pathKey
            .substring(prefix.length() + 1, pathKey.length() - KEY_RULE_POSTFIX.length());
        configurationItem.propertyName = DynamicPropertyFactory.getInstance()
            .getStringProperty(String.format(KEY_PROPERTY_NAME, prefix, pathKeyItem), null).get();
        if (StringUtils.isEmpty(configurationItem.propertyName)) {
          continue;
        }
        configurationItem.category = DynamicPropertyFactory.getInstance()
            .getStringProperty(String.format(KEY_CATEGORY, prefix, pathKeyItem), null).get();
        if (StringUtils.isEmpty(configurationItem.category)) {
          continue;
        }
        configurations.put(pathKeyItem, configurationItem);
      }
    }

    if (KEY_WHITE_LIST_PREFIX.equals(prefix)) {
      this.whiteList = configurations;
      logConfigurations(prefix, configurations, true);
    } else {
      this.blackList = configurations;
      logConfigurations(prefix, configurations, false);
    }
  }

  private void logConfigurations(String prefix, Map<String, ConfigurationItem> configurations, boolean isWhite) {
    for (String key : configurations.keySet()) {
      ConfigurationItem item = configurations.get(key);
      LOG.info((isWhite ? "White list " : "Black list ") + "config item: key=" + key + ";category=" + item.category
          + ";propertyName=" + item.propertyName
          + ";rule=" + item.rule);
    }
  }
}
