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
package org.apache.servicecomb.router.cache;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.governance.event.GovernanceConfigurationChangedEvent;
import org.apache.servicecomb.governance.event.GovernanceEventManager;
import org.apache.servicecomb.router.model.PolicyRuleItem;
import org.apache.servicecomb.router.model.ServiceInfoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.Subscribe;

@Component
public class RouterRuleCache {
  private static final Logger LOGGER = LoggerFactory.getLogger(RouterRuleCache.class);

  public static final String ROUTE_RULE_PREFIX = "servicecomb.routeRule.";

  private static final String ROUTE_RULE = "servicecomb.routeRule.%s";

  private final Environment environment;

  private final ConcurrentHashMap<String, ServiceInfoCache> serviceInfoCacheMap = new ConcurrentHashMap<>();

  private final Object lock = new Object();

  private final Representer representer = new Representer(new DumperOptions());

  @Autowired
  public RouterRuleCache(Environment environment) {
    this.environment = environment;
    representer.getPropertyUtils().setSkipMissingProperties(true);
    GovernanceEventManager.register(this);
  }

  /**
   * cache and register callback
   *
   * return false when: 1. parsing error 2. rule is null
   */
  public boolean doInit(String targetServiceName) {
    if (!isServerContainRule(targetServiceName)) {
      return false;
    }
    if (!serviceInfoCacheMap.containsKey(targetServiceName)) {
      synchronized (lock) {
        if (serviceInfoCacheMap.containsKey(targetServiceName)) {
          return true;
        }
        return addAllRule(targetServiceName, environment.getProperty(String.format(ROUTE_RULE, targetServiceName), ""));
      }
    }
    return true;
  }

  @Subscribe
  public void onConfigurationChangedEvent(GovernanceConfigurationChangedEvent event) {
    for (String key : event.getChangedConfigurations()) {
      if (key.startsWith(ROUTE_RULE_PREFIX)) {
        serviceInfoCacheMap.remove(key.substring(ROUTE_RULE_PREFIX.length()));
      }
    }
  }

  private boolean addAllRule(String targetServiceName, String ruleStr) {
    if (StringUtils.isEmpty(ruleStr)) {
      return false;
    }
    List<PolicyRuleItem> policyRuleItemList;
    try {
      Yaml entityParser = new Yaml(
          new Constructor(new TypeDescription(PolicyRuleItem[].class, PolicyRuleItem[].class), new LoaderOptions()),
          representer);
      policyRuleItemList = Arrays
          .asList(entityParser.loadAs(ruleStr, PolicyRuleItem[].class));
    } catch (Exception e) {
      LOGGER.warn("Route management serialization for service {} failed: {}", targetServiceName, e.getMessage());
      return false;
    }
    if (CollectionUtils.isEmpty(policyRuleItemList)) {
      LOGGER.warn("Route management serialization for service {} is empty", targetServiceName);
      return false;
    }
    ServiceInfoCache serviceInfoCache = new ServiceInfoCache(policyRuleItemList);
    serviceInfoCacheMap.put(targetServiceName, serviceInfoCache);
    return true;
  }

  /**
   * if a server don't have rule , avoid registered too many callback , it may cause memory leak
   */
  private boolean isServerContainRule(String targetServiceName) {
    return !StringUtils.isEmpty(environment.getProperty(String.format(ROUTE_RULE, targetServiceName), ""));
  }

  public ConcurrentHashMap<String, ServiceInfoCache> getServiceInfoCacheMap() {
    return serviceInfoCacheMap;
  }

  @VisibleForTesting
  void refresh() {
    serviceInfoCacheMap.clear();
  }

  public void refresh(String targetServiceName) {
    serviceInfoCacheMap.remove(targetServiceName);
  }
}
