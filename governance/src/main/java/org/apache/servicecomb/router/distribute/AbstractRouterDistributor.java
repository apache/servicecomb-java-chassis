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
package org.apache.servicecomb.router.distribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.servicecomb.router.cache.RouterRuleCache;
import org.apache.servicecomb.router.model.PolicyRuleItem;
import org.apache.servicecomb.router.model.RouteItem;
import org.apache.servicecomb.router.model.TagItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

public abstract class AbstractRouterDistributor<INSTANCE> implements
    RouterDistributor<INSTANCE> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRouterDistributor.class);

  private Function<INSTANCE, String> getVersion;

  private Function<INSTANCE, String> getServerName;

  private Function<INSTANCE, Map<String, String>> getProperties;

  private RouterRuleCache routerRuleCache;

  @Autowired
  public void setRouterRuleCache(RouterRuleCache routerRuleCache) {
    this.routerRuleCache = routerRuleCache;
  }

  protected AbstractRouterDistributor() {
  }

  /**
   * distribute logic:
   * 1、First according to the set route rules to choose target instances, if have just return.
   * 2、if route rules not match instance, check if fallback rules are set, if set and match instances then return.
   * 3、if route and fallback routes all have not match instance, then if route rules weight count less 100, return
   * unset instances, otherwise return all instances.
   * @param targetServiceName
   * @param list
   * @param invokeRule
   * @return
   */
  @Override
  public List<INSTANCE> distribute(String targetServiceName, List<INSTANCE> list, PolicyRuleItem invokeRule) {
    invokeRule.check();

    // unSetTags instance list
    List<INSTANCE> unSetTagInstances = new ArrayList<>();

    // record fallback router targItem instance
    Map<TagItem, List<INSTANCE>> fallbackVersionServerMap = new HashMap<>();

    // get tag instance map, fallbackVersionServerMap, unSetTagInstances
    Map<TagItem, List<INSTANCE>> versionServerMap = getDistributList(targetServiceName, list, invokeRule,
        unSetTagInstances, fallbackVersionServerMap);

    if (CollectionUtils.isEmpty(versionServerMap) && CollectionUtils.isEmpty(fallbackVersionServerMap)) {
      LOGGER.debug("route management can not match any rule and route the latest version");
      // no rule matched instance babel, all instance return, select instance for load balancing later
      return list;
    }

    // weight calculation to obtain the next tags instance
    TagItem targetTag = getFiltedServerTagItem(invokeRule, targetServiceName);
    if (targetTag != null && versionServerMap.containsKey(targetTag)) {
      return versionServerMap.get(targetTag);
    }

    if (!fallbackVersionServerMap.isEmpty()) {
      // weight calculation to obtain the next fallback tags instance
      TagItem fallbackTargetTag = getFallbackFiltedServerTagItem(invokeRule.getFallback(), targetServiceName);
      if (fallbackTargetTag != null && fallbackVersionServerMap.containsKey(fallbackTargetTag)) {
        return fallbackVersionServerMap.get(fallbackTargetTag);
      }
    }

    // has weightLess situation and unSetTagInstances has values
    if (invokeRule.isWeightLess() && !unSetTagInstances.isEmpty()) {
      return unSetTagInstances;
    }
    return list;
  }

  @Override
  public void init(Function<INSTANCE, String> getVersion,
      Function<INSTANCE, String> getServerName,
      Function<INSTANCE, Map<String, String>> getProperties) {
    this.getVersion = getVersion;
    this.getServerName = getServerName;
    this.getProperties = getProperties;
  }

  public TagItem getFiltedServerTagItem(PolicyRuleItem rule, String targetServiceName) {
    return routerRuleCache.getServiceInfoCacheMap().get(targetServiceName)
        .getNextInvokeVersion(rule);
  }

  public TagItem getFallbackFiltedServerTagItem(List<RouteItem> fallback, String targetServiceName) {
    return routerRuleCache.getServiceInfoCacheMap().get(targetServiceName)
        .getFallbackNextInvokeVersion(fallback);
  }

  /**
   * 1.filter set route rules targetService, build fallback targetService map and unSetTagInstances list.
   * 2.establish map is a more complicate way than direct traversal， because of multiple matches.
   *
   * the method getProperties() contains other field that we don't need.
   */
  private Map<TagItem, List<INSTANCE>> getDistributList(String serviceName, List<INSTANCE> list,
      PolicyRuleItem invokeRule, List<INSTANCE> unSetTagInstances, Map<TagItem, List<INSTANCE>> fallbackVersionMap) {
    Map<TagItem, List<INSTANCE>> versionServerMap = new HashMap<>();
    for (INSTANCE instance : list) {
      //get server
      if (getServerName.apply(instance).equals(serviceName)) {
        TagItem tagItem = new TagItem(getVersion.apply(instance), getProperties.apply(instance));
        // route most matching TagItem
        TagItem targetTag = buildTargetTag(invokeRule.getRoute(), tagItem);
        TagItem targetTagFallback = null;
        if (!CollectionUtils.isEmpty(invokeRule.getFallback())) {
          // fallback most matching TagItem
          targetTagFallback = buildTargetTag(invokeRule.getFallback(), tagItem);
        }
        if (targetTag != null) {
          if (!versionServerMap.containsKey(targetTag)) {
            versionServerMap.put(targetTag, new ArrayList<>());
          }
          versionServerMap.get(targetTag).add(instance);
        } else if (targetTagFallback != null) {
          if (!fallbackVersionMap.containsKey(targetTagFallback)) {
            fallbackVersionMap.put(targetTagFallback, new ArrayList<>());
          }
          fallbackVersionMap.get(targetTagFallback).add(instance);
        } else {
          // not matched, placed in the unset tag instances collection
          unSetTagInstances.add(instance);
        }
      }
    }
    return versionServerMap;
  }

  private TagItem buildTargetTag(List<RouteItem> route, TagItem tagItem) {
    int maxMatch = 0;
    TagItem targetTag = null;
    // obtain the rule with the most parameter matches
    for (RouteItem entry : route) {
      if (entry.getTagitem() == null){
        continue;
      }
      int nowMatch = entry.getTagitem().matchNum(tagItem);
      if (nowMatch > maxMatch) {
        maxMatch = nowMatch;
        targetTag = entry.getTagitem();
      }
    }
    return targetTag;
  }
}
