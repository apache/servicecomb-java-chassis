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

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.servicecomb.router.model.PolicyRuleItem;
import org.apache.servicecomb.router.model.ServiceInfoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class RouterRuleCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(RouterRuleCache.class);

  private static ConcurrentHashMap<String, ServiceInfoCache> serviceInfoCacheMap = new ConcurrentHashMap<>();

  private static final String ROUTE_RULE = "servicecomb.routeRule.%s";

  private static Interner<String> servicePool = Interners.newWeakInterner();

  /**
   * cache and register callback return false when: 1. parsing error 2. rule is null
   *
   * @param targetServiceName
   * @return
   */
  public static boolean doInit(String targetServiceName) {
    if (!serviceInfoCacheMap.containsKey(targetServiceName)) {
      synchronized (servicePool.intern(targetServiceName)) {
        if (!serviceInfoCacheMap.containsKey(targetServiceName)) {
          return true;
        }
        //Yaml not thread-safe
        Yaml yaml = new Yaml();
        DynamicStringProperty ruleStr = DynamicPropertyFactory.getInstance().getStringProperty(
            String.format(ROUTE_RULE, targetServiceName), null, () -> {
              refresh(targetServiceName);
              DynamicStringProperty tepRuleStr = DynamicPropertyFactory.getInstance()
                  .getStringProperty(String.format(ROUTE_RULE, targetServiceName), null);
              addAllRule(targetServiceName, yaml, tepRuleStr);
            });
        return addAllRule(targetServiceName, yaml, ruleStr);
      }
    }
    return true;
  }

  private static boolean addAllRule(String targetServiceName, Yaml yaml,
      DynamicStringProperty ruleStr) {
    if (ruleStr.get() == null) {
      return false;
    }
    List<PolicyRuleItem> policyRuleItemList = Arrays
        .asList(yaml.loadAs(ruleStr.get(), PolicyRuleItem[].class));
    if (CollectionUtils.isEmpty(policyRuleItemList)) {
      return false;
    }
    try {
      if (serviceInfoCacheMap.get(targetServiceName) == null) {
        serviceInfoCacheMap.put(targetServiceName, new ServiceInfoCache());
      }
      serviceInfoCacheMap.get(targetServiceName).setAllrule(policyRuleItemList);
      // init tagitem
      serviceInfoCacheMap.get(targetServiceName).getAllrule().forEach(a ->
          a.getRoute().forEach(b -> b.initTagItem())
      );
      // sort by precedence
      serviceInfoCacheMap.get(targetServiceName).sortRule();
    } catch (Exception e) {
      LOGGER.error("route management Serialization failed: {}", e.getMessage());
      return false;
    }
    return true;
  }

  public static ConcurrentHashMap<String, ServiceInfoCache> getServiceInfoCacheMap() {
    return serviceInfoCacheMap;
  }

  public static void refresh() {
    serviceInfoCacheMap = new ConcurrentHashMap<>();
    servicePool = Interners.newWeakInterner();
  }

  public static void refresh(String targetServiceName) {
    serviceInfoCacheMap.remove(targetServiceName);
  }
}
