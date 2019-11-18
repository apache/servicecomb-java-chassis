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
package org.apache.servicecomb.router;

import com.netflix.loadbalancer.Server;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.servicecomb.router.cache.RouterRuleCache;
import org.apache.servicecomb.router.distribute.RouterDistributor;
import org.apache.servicecomb.router.match.RouterRuleMatcher;
import org.apache.servicecomb.router.model.PolicyRuleItem;
import org.springframework.util.StringUtils;
import org.springframework.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @Author GuoYl123
 * @Date 2019/10/16
 **/
public class RouterFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RouterFilter.class);

  public static <T extends Server, E> List<T> getFilteredListOfServers(List<T> list,
      String targetServiceName, Map<String, String> headers, RouterDistributor<T, E> distributer) {
    if (CollectionUtils.isEmpty(list)) {
      return list;
    }
    if (StringUtils.isEmpty(targetServiceName)) {
      return list;
    }
    if (headers == null) {
      headers = new HashMap<>();
    }
    LOGGER.debug("route management headers:{}", headers);
    /**
     * 1.初始化--进行cache缓存
     */
    if (!RouterRuleCache.doInit(targetServiceName)) {
      LOGGER.debug("route management init failed");
      return list;
    }
    /**
     * 2.match--拿到invoke相关信息 (header),匹配到唯一的rule
     */
    PolicyRuleItem invokeRule = RouterRuleMatcher.getInstance().match(targetServiceName, headers);

    if (invokeRule == null) {
      LOGGER.debug("route management match rule failed");
      return list;
    }

    LOGGER.debug("route management match rule success: {}", invokeRule);

    /**
     * 3.distribute--拿到server list选择endpoint进行流量分配
     */
    List<T> resultList = distributer.distribute(targetServiceName, list, invokeRule);

    LOGGER.debug("route management distribute rule success: {}", resultList);

    return resultList;
  }
}
