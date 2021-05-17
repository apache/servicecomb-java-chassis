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
import org.apache.servicecomb.router.custom.MicroserviceCache;
import org.apache.servicecomb.router.model.PolicyRuleItem;
import org.apache.servicecomb.router.model.RouteItem;
import org.apache.servicecomb.router.model.TagItem;
import org.apache.servicecomb.router.util.VersionCompareUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.netflix.loadbalancer.Server;

import io.vertx.ext.web.Route;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public abstract class AbstractRouterDistributor<T extends Server, E> implements
    RouterDistributor<T, E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRouterDistributor.class);

  private Function<T, E> getIns;

  private Function<E, String> getVersion;

  private Function<E, String> getServerName;

  private Function<E, Map<String, String>> getProperties;

  @Override
  public List<T> distribute(String targetServiceName, List<T> list, PolicyRuleItem invokeRule) {
    //init LatestVersion
    initLatestVersion(targetServiceName, list);

    invokeRule.check(
        RouterRuleCache.getServiceInfoCacheMap().get(targetServiceName).getLatestVersionTag());

    // get tag list
    Map<TagItem, List<T>> versionServerMap = getDistributList(targetServiceName, list, invokeRule);

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
  public void init(Function<T, E> getIns,
      Function<E, String> getVersion,
      Function<E, String> getServerName,
      Function<E, Map<String, String>> getProperties) {
    this.getIns = getIns;
    this.getVersion = getVersion;
    this.getServerName = getServerName;
    this.getProperties = getProperties;
  }

  public TagItem getFiltedServerTagItem(PolicyRuleItem rule, String targetServiceName) {
    return RouterRuleCache.getServiceInfoCacheMap().get(targetServiceName)
        .getNextInvokeVersion(rule);
  }

  /**
   * 1.filter targetService
   * 2.establish map is a more complicate way than direct traversal， because of multiple matches.
   *
   * the method getProperties() contains other field that we don't need.
   *
   * @param serviceName
   * @param list
   * @return
   */
  private Map<TagItem, List<T>> getDistributList(String serviceName,
      List<T> list,
      PolicyRuleItem invokeRule) {
    String latestV = RouterRuleCache.getServiceInfoCacheMap().get(serviceName).getLatestVersionTag()
        .getVersion();
    Map<TagItem, List<T>> versionServerMap = new HashMap<>();
    for (T server : list) {
      //get server
      E ms = getIns.apply(server);
      if (getServerName.apply(ms).equals(serviceName)) {
        //most matching
        TagItem tagItem = new TagItem(getVersion.apply(ms), getProperties.apply(ms));
        TagItem targetTag = null;
        int maxMatch = 0;
        for (RouteItem entry : invokeRule.getRoute()) {
          int nowMatch = entry.getTagitem().matchNum(tagItem);
          if (nowMatch > maxMatch) {
            maxMatch = nowMatch;
            targetTag = entry.getTagitem();
          }
        }
        if (invokeRule.isWeightLess() && getVersion.apply(ms).equals(latestV)) {
          TagItem latestVTag = invokeRule.getRoute().get(invokeRule.getRoute().size() - 1)
              .getTagitem();
          if (!versionServerMap.containsKey(latestVTag)) {
            versionServerMap.put(latestVTag, new ArrayList<>());
          }
          versionServerMap.get(latestVTag).add(server);
        }
        if (targetTag != null) {
          if (!versionServerMap.containsKey(targetTag)) {
            versionServerMap.put(targetTag, new ArrayList<>());
          }
          versionServerMap.get(targetTag).add(server);
        }
      }
    }
    return versionServerMap;
  }


  public void initLatestVersion(String serviceName, List<T> list) {
    String latestVersion = null;
    for (T server : list) {
      E ms = getIns.apply(server);
      if (getServerName.apply(ms).equals(serviceName)) {
        if (latestVersion == null || VersionCompareUtil
            .compareVersion(latestVersion, getVersion.apply(ms)) == -1) {
          latestVersion = getVersion.apply(ms);
        }
      }
    }
    TagItem tagitem = new TagItem(latestVersion);
    RouterRuleCache.getServiceInfoCacheMap().get(serviceName).setLatestVersionTag(tagitem);
  }

  /**
   * @param list
   * @param targetServiceName
   * @return
   */
  public List<T> getLatestVersionList(List<T> list, String targetServiceName) {
    String latestV = RouterRuleCache.getServiceInfoCacheMap().get(targetServiceName)
        .getLatestVersionTag().getVersion();
    return list.stream().filter(server ->
        getVersion.apply(getIns.apply(server)).equals(latestV)
    ).collect(Collectors.toList());
  }
}
