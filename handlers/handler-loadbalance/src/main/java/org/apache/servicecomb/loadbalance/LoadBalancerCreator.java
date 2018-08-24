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

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.loadbalancer.LoadBalancerStats;

/**
 *  Create a suitable load balancer for each invocation.
 *
 *  Robin components work good in service level state, and we want to reuse its IRule components and other
 *  facilities, but they are not good for operation
 *  level filters, so write a custom load balance process.
 *
 *  Load balance instance is created for each microservice(plus version rule), thus it is service level,
 *  it only can contains stateful information with service. e.g. LoadBalancerStats.
 *
 *  ServerListFilter may choose available servers according to invocation information, IRule will work
 *  on the result of ServerListFilter, they should not contain operation level state information in instance fields.
 */
public class LoadBalancerCreator {
  private RuleExt rule;

  private LoadBalancerStats lbStats;

  private List<ServerListFilterExt> filters;

  private String microServiceName;

  public LoadBalancerCreator(RuleExt rule, String microServiceName) {
    this.rule = rule;
    this.microServiceName = microServiceName;
    this.lbStats = new LoadBalancerStats(null);
    // load new instances, because filters work on service information
    this.filters = SPIServiceUtils.loadSortedService(ServerListFilterExt.class);
  }

  public void shutdown() {
    // nothing to do now
  }

  @VisibleForTesting
  void setFilters(List<ServerListFilterExt> filters) {
    this.filters = filters;
  }

  public LoadBalancer createLoadBalancer(List<ServiceCombServer> servers) {
    return new LoadBalancer(rule, microServiceName, lbStats, filters, servers);
  }
}
