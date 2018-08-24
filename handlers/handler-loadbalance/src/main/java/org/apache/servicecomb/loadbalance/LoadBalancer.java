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

import org.apache.servicecomb.core.Invocation;

import com.netflix.loadbalancer.LoadBalancerStats;

/**
 *  A load balancer with RuleExt and ServerListFilterExt
 */
public class LoadBalancer {
  private RuleExt rule;

  private LoadBalancerStats lbStats;

  private String microServiceName;

  private List<ServerListFilterExt> filters;

  private List<ServiceCombServer> servers;

  public LoadBalancer(RuleExt rule, String microServiceName,
      LoadBalancerStats stats, List<ServerListFilterExt> filters, List<ServiceCombServer> servers) {
    this.microServiceName = microServiceName;
    this.rule = rule;
    this.lbStats = stats;
    this.filters = filters;
    this.servers = servers;
    this.rule.setLoadBalancer(this);
    this.filters.forEach((item) -> item.setLoadBalancer(this));
  }

  public ServiceCombServer chooseServer(Invocation invocation) {
    List<ServiceCombServer> temp = this.servers;
    for (ServerListFilterExt filterExt : filters) {
      temp = filterExt.getFilteredListOfServers(temp, invocation);
    }
    return rule.choose(temp, invocation);
  }

  public LoadBalancerStats getLoadBalancerStats() {
    return lbStats;
  }

  public String getMicroServiceName() {
    return microServiceName;
  }
}
