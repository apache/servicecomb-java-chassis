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

package org.apache.servicecomb.loadbalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.loadbalancer.LoadBalancerStats;

/**
 *  A load balancer with RuleExt and ServerListFilterExt
 */
public class LoadBalancer {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancer.class);

  private static AtomicInteger id = new AtomicInteger(0);

  private RuleExt rule;

  private LoadBalancerStats lbStats;

  private String microServiceName;

  private List<ServerListFilterExt> filters;

  public LoadBalancer(RuleExt rule, String microServiceName) {
    this.microServiceName = microServiceName;
    this.rule = rule;
    this.lbStats = new LoadBalancerStats(microServiceName + id.getAndDecrement());
    // load new instances, because filters work on service information
    this.filters = SPIServiceUtils.loadSortedService(ServerListFilterExt.class);
    this.rule.setLoadBalancer(this);
    this.filters.forEach((item) -> item.setLoadBalancer(this));
  }

  public ServiceCombServer chooseServer(Invocation invocation) {
    List<ServiceCombServer> servers = invocation.getLocalContext(LoadbalanceHandler.CONTEXT_KEY_SERVER_LIST);
    int serversCount = servers.size();
    for (ServerListFilterExt filterExt : filters) {
      if(!filterExt.enabled()) {
        continue;
      }
      servers = filterExt.getFilteredListOfServers(servers, invocation);
      if (servers.isEmpty() && serversCount > 0) {
        LOGGER.warn("There are not servers exist after filtered by {}.", filterExt.getClass());
        break;
      }
    }
    ServiceCombServer server = rule.choose(servers, invocation);
    if (null == server) {
      return null;
    }
    ServiceCombServerStats serverStats = ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(server);
    if (serverStats.isIsolated()) {
      LOGGER.info("The Service {}'s instance {} has been isolated for a while, give a single test opportunity.",
          invocation.getMicroserviceName(),
          server.getInstance().getInstanceId());
    }
    return server;
  }

  public LoadBalancerStats getLoadBalancerStats() {
    return lbStats;
  }

  public String getMicroServiceName() {
    return microServiceName;
  }

  @VisibleForTesting
  void setFilters(List<ServerListFilterExt> filters) {
    this.filters = filters;
  }
}
