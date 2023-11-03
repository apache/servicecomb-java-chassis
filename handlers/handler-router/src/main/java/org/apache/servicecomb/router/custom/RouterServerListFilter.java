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
package org.apache.servicecomb.router.custom;

import java.util.List;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.governance.MatchType;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.loadbalance.ServerListFilterExt;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.router.RouterFilter;
import org.apache.servicecomb.router.distribute.RouterDistributor;

public class RouterServerListFilter implements ServerListFilterExt {

  private static final String ENABLE = "servicecomb.router.type";

  private static final String TYPE_ROUTER = "router";

  @SuppressWarnings("unchecked")
  private final RouterDistributor<ServiceCombServer> routerDistributor = BeanUtils
      .getBean(RouterDistributor.class);

  private final RouterFilter routerFilter = BeanUtils.getBean(RouterFilter.class);

  @Override
  public boolean enabled() {
    return LegacyPropertyFactory.getStringProperty(ENABLE, "")
        .equals(TYPE_ROUTER);
  }

  @Override
  public List<ServiceCombServer> getFilteredListOfServers(List<ServiceCombServer> list,
      Invocation invocation) {
    String targetServiceName = invocation.getMicroserviceName();
    return routerFilter.getFilteredListOfServers(list, targetServiceName,
        MatchType.createGovHttpRequest(invocation), routerDistributor);
  }
}
