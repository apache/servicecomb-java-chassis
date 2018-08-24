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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.ServerStats;

/**
 * 会话保持策略：优先选择上一次选中的服务器，保证请求都发送到同一个服务器上去。
 * 提供当会话过期或者失败次数超过限制后，轮询选择其他服务器的能力。
 *
 */
public class SessionStickinessRule implements RuleExt {
  private static final Logger LOG = LoggerFactory.getLogger(SessionStickinessRule.class);

  private final Object lock = new Object();

  private LoadBalancer loadBalancer;

  // use random rule as the trigger rule, to prevent consumer instance select the same producer instance.
  private RuleExt triggerRule;

  private volatile ServiceCombServer lastServer = null;

  private long lastAccessedTime = 0;

  private volatile boolean errorThresholdMet = false;

  private static final int MILLI_COUNT_IN_SECOND = 1000;

  private String microserviceName;

  public SessionStickinessRule() {
    triggerRule = new RoundRobinRuleExt();
  }

  public void setLoadBalancer(LoadBalancer loadBalancer) {
    this.loadBalancer = loadBalancer;
  }

  private ServiceCombServer chooseNextServer(List<ServiceCombServer> servers, Invocation invocation) {
    lastServer = triggerRule.choose(servers, invocation);
    lastAccessedTime = System.currentTimeMillis();
    return lastServer;
  }

  private ServiceCombServer chooseInitialServer(List<ServiceCombServer> servers, Invocation invocation) {
    synchronized (lock) {
      if (lastServer == null) {
        chooseNextServer(servers, invocation);
      }
    }
    return lastServer;
  }

  private ServiceCombServer chooseServerWhenTimeout(List<ServiceCombServer> servers, Invocation invocation) {
    synchronized (lock) {
      if (isTimeOut()) {
        chooseNextServer(servers, invocation);
      }
    }
    return lastServer;
  }

  private ServiceCombServer chooseServerErrorThresholdMet(List<ServiceCombServer> servers, Invocation invocation) {
    synchronized (lock) {
      if (errorThresholdMet) {
        chooseNextServer(servers, invocation);
        errorThresholdMet = false;
      }
    }
    return lastServer;
  }

  private boolean isTimeOut() {
    return Configuration.INSTANCE.getSessionTimeoutInSeconds(microserviceName) > 0
        && System.currentTimeMillis()
        - this.lastAccessedTime > ((long) Configuration.INSTANCE.getSessionTimeoutInSeconds(microserviceName)
        * MILLI_COUNT_IN_SECOND);
  }

  private boolean isErrorThresholdMet() {
    LoadBalancerStats stats = loadBalancer.getLoadBalancerStats();

    if (stats != null && stats.getServerStats() != null && stats.getServerStats().size() > 0) {
      ServerStats serverStats = stats.getSingleServerStat(lastServer);
      int successiveFaildCount = serverStats.getSuccessiveConnectionFailureCount();
      if (Configuration.INSTANCE.getSuccessiveFailedTimes(microserviceName) > 0
          && successiveFaildCount >= Configuration.INSTANCE.getSuccessiveFailedTimes(microserviceName)) {
        serverStats.clearSuccessiveConnectionFailureCount();
        return true;
      }
    }
    return false;
  }

  @Override
  public ServiceCombServer choose(List<ServiceCombServer> servers, Invocation invocation) {
    if (lastServer == null) {
      return chooseInitialServer(servers, invocation);
    }

    if (isTimeOut()) {
      LOG.warn("session timeout. choose another server.");
      return chooseServerWhenTimeout(servers, invocation);
    } else {
      this.lastAccessedTime = System.currentTimeMillis();
    }

    if (isErrorThresholdMet()) {
      LOG.warn("reached max error. choose another server.");
      errorThresholdMet = true;
      return chooseServerErrorThresholdMet(servers, invocation);
    }

    if (!servers.contains(lastServer)) {
      return chooseNextServer(servers, invocation);
    }

    return lastServer;
  }
}
