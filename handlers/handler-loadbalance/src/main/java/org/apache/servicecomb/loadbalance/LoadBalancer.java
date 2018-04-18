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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.core.Invocation;

import com.netflix.loadbalancer.AbstractLoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerListFilter;
import com.netflix.loadbalancer.WeightedResponseTimeRule;

/**
 * 实现不包含服务器状态监测的负载均衡器。（这些职责在注册中心客户端实现）
 *
 */
public class LoadBalancer extends AbstractLoadBalancer {
  private String name;

  private List<Server> serverList = Collections.emptyList();

  private IRule rule;

  private LoadBalancerStats lbStats;

  // 以filter类名为Key
  private Map<String, ServerListFilterExt> filters;

  private String microServiceName;

  public LoadBalancer(String name, IRule rule, String microServiceName) {
    this.name = name;
    this.rule = rule;
    this.microServiceName = microServiceName;
    this.lbStats = new LoadBalancerStats(null);
    this.filters = new ConcurrentHashMap<>();
    this.rule.setLoadBalancer(this);
  }

  public String getName() {
    return name;
  }

  public void shutdown() {
    // netflix components does not have a property way to shutdown laodbalancers so we do it in a not quite elegant way.
    if (this.rule instanceof WeightedResponseTimeRule) {
      ((WeightedResponseTimeRule) this.rule).shutdown();
    }
  }

  // every filter group has a loadBalancer instance
  // serverList almost not changed for different invocation
  // so every invocation will call setServerList, this is no problem
  public void setServerList(List<Server> serverList) {
    this.serverList = Collections.unmodifiableList(serverList);
  }

  @Override
  public void addServers(List<Server> newServers) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Server chooseServer(Object key) {
    return rule.choose(key);
  }

  @Override
  public void markServerDown(Server server) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public List<Server> getServerList(boolean availableOnly) {
    return getAllServers();
  }

  @Override
  public List<Server> getReachableServers() {
    return getAllServers();
  }

  @Override
  public List<Server> getAllServers() {
    List<Server> servers = serverList;
    for (ServerListFilter<Server> filter : filters.values()) {
      servers = filter.getFilteredListOfServers(servers);
    }
    return servers;
  }

  @Override
  public List<Server> getServerList(ServerGroup serverGroup) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public LoadBalancerStats getLoadBalancerStats() {
    return lbStats;
  }

  public void setInvocation(Invocation invocation) {
    for (ServerListFilterExt filter : filters.values()) {
      filter.setInvocation(invocation);
    }
  }

  public void putFilter(String name, ServerListFilterExt filter) {
    filters.put(name, filter);
  }

  public int getFilterSize() {
    return filters.size();
  }

  public String getMicroServiceName() {
    return microServiceName;
  }
}
