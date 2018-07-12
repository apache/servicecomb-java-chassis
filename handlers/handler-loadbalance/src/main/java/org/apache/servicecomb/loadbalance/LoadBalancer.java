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

import com.netflix.loadbalancer.AbstractLoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.Server;

/**
 *  Robbin LoadBalancer implementation. Only support IRule and basic operations.
 */
public class LoadBalancer extends AbstractLoadBalancer {
  private List<Server> serverList = Collections.emptyList();

  private IRule rule;

  private LoadBalancerStats lbStats;

  private String microServiceName;

  public LoadBalancer(IRule rule, String microServiceName,
      LoadBalancerStats stats) {
    this.microServiceName = microServiceName;
    this.rule = rule;
    this.lbStats = stats;
  }

  public void setServerList(List<Server> serverList) {
    this.serverList = Collections.unmodifiableList(serverList);
  }

  @Override
  public void addServers(List<Server> newServers) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Server chooseServer(Object key) {
    // rule is shared across loadbalancers, so it will get concurrent access problems that it's owned loadbalancer is
    // not 'this', but this is fine to use other loadbalancer instances only when serverList is correctly set
    this.rule.setLoadBalancer(this);
    return rule.choose(key);
  }

  @Override
  public void markServerDown(Server server) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  @Deprecated
  public List<Server> getServerList(boolean availableOnly) {
    return getAllServers();
  }

  @Override
  public List<Server> getReachableServers() {
    return getAllServers();
  }

  @Override
  // Different types of Robin Component Rule have different usages for server status and list.
  // e.g. RoundRobinRule using getAllServers & alive & readyToServe
  // RandomRule using getReachableServers & alive
  // WeightedResponseTimeRule using getAllServers & alive
  // To make all rules work only on "how to choose a server from alive servers", we do not rely on Robbin getReachableServers.
  // We ensure getReachableServers & getAllServers work in the same way.
  public List<Server> getAllServers() {
    return serverList;
  }

  @Override
  public List<Server> getServerList(ServerGroup serverGroup) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public LoadBalancerStats getLoadBalancerStats() {
    return lbStats;
  }

  public String getMicroServiceName() {
    return microServiceName;
  }
}
