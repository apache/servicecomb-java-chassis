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

    invokeRule.check();

    // 未设置tag实例集合
    List<INSTANCE> unSetTagInstances = new ArrayList<>();

    // get tag list
    Map<TagItem, List<INSTANCE>> versionServerMap = getDistributList(targetServiceName, list, invokeRule, unSetTagInstances);

    if (CollectionUtils.isEmpty(versionServerMap)) {
      LOGGER.debug("route management can not match any rule and route the latest version");
      // 规则未匹配到实例标签，全量返回，后面负载均衡选择实例
      return list;
    }

    // 权重计算获取下一个tag实例
    TagItem targetTag = getFiltedServerTagItem(invokeRule, targetServiceName);
    if (targetTag != null && versionServerMap.containsKey(targetTag)) {
      return versionServerMap.get(targetTag);
    }

    // 存在缺省权重情况
    if (invokeRule.isWeightLess()) {
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

  /**
   * 1.filter targetService
   * 2.establish map is a more complicate way than direct traversal， because of multiple matches.
   *
   * the method getProperties() contains other field that we don't need.
   */
  private Map<TagItem, List<INSTANCE>> getDistributList(String serviceName,
      List<INSTANCE> list, PolicyRuleItem invokeRule, List<INSTANCE> unSetTagInstances) {
    Map<TagItem, List<INSTANCE>> versionServerMap = new HashMap<>();
    for (INSTANCE instance : list) {
      //get server
      if (getServerName.apply(instance).equals(serviceName)) {
        //most matching
        TagItem tagItem = new TagItem(getVersion.apply(instance), getProperties.apply(instance));
        TagItem targetTag = null;
        int maxMatch = 0;
        // 得到参数匹配最多的一条规则
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
        if (targetTag != null) {
          if (!versionServerMap.containsKey(targetTag)) {
            versionServerMap.put(targetTag, new ArrayList<>());
          }
          versionServerMap.get(targetTag).add(instance);
        } else {
          // 未匹配上，放入未设置tag实例集合中
          unSetTagInstances.add(instance);
        }
      }
    }
    return versionServerMap;
  }
}
