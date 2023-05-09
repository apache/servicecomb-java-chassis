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
import java.util.stream.Collectors;

import org.apache.servicecomb.router.cache.RouterRuleCache;
import org.apache.servicecomb.router.model.PolicyRuleItem;
import org.apache.servicecomb.router.model.RouteItem;
import org.apache.servicecomb.router.model.TagItem;
import org.apache.servicecomb.router.util.VersionCompareUtil;
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

  @Override
  public List<INSTANCE> distribute(String targetServiceName, List<INSTANCE> list, PolicyRuleItem invokeRule) {
    //init LatestVersion
    initLatestVersion(targetServiceName, list);

    invokeRule.check(
        routerRuleCache.getServiceInfoCacheMap().get(targetServiceName).getLatestVersionTag());

    // get tag list
    Map<TagItem, List<INSTANCE>> versionServerMap = getDistributList(targetServiceName, list, invokeRule);

    if (CollectionUtils.isEmpty(versionServerMap)) {
      LOGGER.debug("route management can not match any rule and route the latest version");
      return getLatestVersionList(list, targetServiceName);
    }

    TagItem targetTag = getFiltedServerTagItem(invokeRule, targetServiceName);
    if (versionServerMap.containsKey(targetTag)) {
      return versionServerMap.get(targetTag);
    }
    return getLatestVersionList(list, targetServiceName);
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

  /**
   * 1.filter targetService
   * 2.establish map is a more complicate way than direct traversal， because of multiple matches.
   *
   * the method getProperties() contains other field that we don't need.
   */
  private Map<TagItem, List<INSTANCE>> getDistributList(String serviceName,
      List<INSTANCE> list,
      PolicyRuleItem invokeRule) {
    String latestV = routerRuleCache.getServiceInfoCacheMap().get(serviceName).getLatestVersionTag()
        .getVersion();
    Map<TagItem, List<INSTANCE>> versionServerMap = new HashMap<>();
    for (INSTANCE instance : list) {
      //get server
      if (getServerName.apply(instance).equals(serviceName)) {
        //most matching
        TagItem tagItem = new TagItem(getVersion.apply(instance), getProperties.apply(instance));
        TagItem targetTag = null;
        int maxMatch = 0;
        for (RouteItem entry : invokeRule.getRoute()) {
          if (entry.getTagitem() == null){
            continue;
          }
          int nowMatch = entry.getTagitem().matchNum(tagItem);
          if (nowMatch > maxMatch) {
            maxMatch = nowMatch;
            targetTag = entry.getTagitem();
          }
        }
        if (invokeRule.isWeightLess() && getVersion.apply(instance).equals(latestV)) {
          TagItem latestVTag = invokeRule.getRoute().get(invokeRule.getRoute().size() - 1)
              .getTagitem();
          if (!versionServerMap.containsKey(latestVTag)) {
            versionServerMap.put(latestVTag, new ArrayList<>());
          }
          versionServerMap.get(latestVTag).add(instance);
        }
        if (targetTag != null) {
          if (!versionServerMap.containsKey(targetTag)) {
            versionServerMap.put(targetTag, new ArrayList<>());
          }
          versionServerMap.get(targetTag).add(instance);
        }
      }
    }
    return versionServerMap;
  }


  public void initLatestVersion(String serviceName, List<INSTANCE> list) {
    String latestVersion = null;
    for (INSTANCE instance : list) {
      if (getServerName.apply(instance).equals(serviceName)) {
        if (latestVersion == null || VersionCompareUtil
            .compareVersion(latestVersion, getVersion.apply(instance)) == -1) {
          latestVersion = getVersion.apply(instance);
        }
      }
    }
    TagItem tagitem = new TagItem(latestVersion);
    routerRuleCache.getServiceInfoCacheMap().get(serviceName).setLatestVersionTag(tagitem);
  }

  public List<INSTANCE> getLatestVersionList(List<INSTANCE> list, String targetServiceName) {
    String latestV = routerRuleCache.getServiceInfoCacheMap().get(targetServiceName)
        .getLatestVersionTag().getVersion();
    return list.stream().filter(instance ->
        getVersion.apply(instance).equals(latestV)
    ).collect(Collectors.toList());
  }
}
