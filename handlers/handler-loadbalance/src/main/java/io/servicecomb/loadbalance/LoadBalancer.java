/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.loadbalance;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.netflix.loadbalancer.AbstractLoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerListFilter;

import io.servicecomb.core.Invocation;

/**
 * 实现不包含服务器状态监测的负载均衡器。（这些职责在注册中心客户端实现）
 *
 */
public class LoadBalancer extends AbstractLoadBalancer {
  private CseServerList serverList;

  private IRule rule;

  private LoadBalancerStats lbStats;

  // 以filter类名为Key
  private Map<String, ServerListFilterExt> filters;

  public LoadBalancer(CseServerList serverList, IRule rule) {
    this.serverList = serverList;
    this.rule = rule;
    this.rule.setLoadBalancer(this);
    this.lbStats = new LoadBalancerStats(null);
    this.filters = new ConcurrentHashMap<>();
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
    List<Server> servers = serverList.getInitialListOfServers();
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
}
