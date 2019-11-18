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
   * 每次序列化额外缓存，配置更新时触发回调函数 返回false即初始化规则失败： 1. 规则解析错误 2. 规则为空
   *
   * @param targetServiceName
   * @return
   */
  public static boolean doInit(String targetServiceName) {
    if (serviceInfoCacheMap.containsKey(targetServiceName)) {
      return true;
    }
    //这里使用guava包装String.intern():因为String.intern()分配在Old Generation，容易引发fullgc
    synchronized (servicePool.intern(targetServiceName)) {
      //Yaml not thread-safe
      Yaml yaml = new Yaml();
      DynamicStringProperty ruleStr = DynamicPropertyFactory.getInstance().getStringProperty(
          String.format(ROUTE_RULE, targetServiceName), null, () -> {
            refresh(targetServiceName);
            DynamicStringProperty tepRuleStr = DynamicPropertyFactory.getInstance()
                .getStringProperty(String.format(ROUTE_RULE, targetServiceName), null);
            if (tepRuleStr.get() == null) {
              return;
            }
            try {
              List<PolicyRuleItem> temList = Arrays
                  .asList(yaml.loadAs(tepRuleStr.get(), PolicyRuleItem[].class));
              RouterRuleCache.addAllRule(targetServiceName, temList);
            } catch (Exception e) {
              LOGGER.error("route management Serialization failed {}", e.getMessage());
              return;
            }
          });
      if (ruleStr.get() == null) {
        return false;
      }
      try {
        addAllRule(targetServiceName,
            Arrays.asList(yaml.loadAs(ruleStr.get(), PolicyRuleItem[].class)));
      } catch (Exception e) {
        LOGGER.error("route management Serialization failed: {}", e.getMessage());
        return false;
      }
      return true;
    }
  }

  private static void addAllRule(String targetServiceName,
      List<PolicyRuleItem> policyRuleItemList) {
    if (CollectionUtils.isEmpty(policyRuleItemList)) {
      return;
    }
    if (serviceInfoCacheMap.get(targetServiceName) == null) {
      serviceInfoCacheMap.put(targetServiceName, new ServiceInfoCache());
    }
    serviceInfoCacheMap.get(targetServiceName).setAllrule(policyRuleItemList);
    // 这里初始化tagitem
    serviceInfoCacheMap.get(targetServiceName).getAllrule().forEach(a ->
        a.getRoute().forEach(b -> b.initTagItem())
    );
    // 按照优先级排序
    serviceInfoCacheMap.get(targetServiceName).sortRule();
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
