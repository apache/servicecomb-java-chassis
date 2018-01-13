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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.loadbalancer.AbstractLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerStats;

/**
 * 会话保持策略：优先选择上一次选中的服务器，保证请求都发送到同一个服务器上去。
 * 提供当会话过期或者失败次数超过限制后，轮询选择其他服务器的能力。
 *
 */
public class SessionStickinessRule implements IRule {
  private static final Logger LOG = LoggerFactory.getLogger(SessionStickinessRule.class);

  private final Object lock = new Object();

  private ILoadBalancer lb;

  // use random rule as the trigger rule, to prevent consumer instance select the same producer instance.
  private IRule triggerRule;

  private volatile Server lastServer = null;

  private long lastAccessedTime = 0;

  private volatile boolean errorThresholdMet = false;

  private static final int MILLI_COUNT_IN_SECOND = 1000;

  public SessionStickinessRule() {
    triggerRule = new RoundRobinRule();
  }

  private Server chooseNextServer(Object key) {
    AbstractLoadBalancer lb = (AbstractLoadBalancer) getLoadBalancer();
    triggerRule.setLoadBalancer(lb);
    lastServer = triggerRule.choose(key);
    lastAccessedTime = System.currentTimeMillis();
    return lastServer;
  }

  private Server chooseInitialServer(Object key) {
    synchronized (lock) {
      if (lastServer == null) {
        chooseNextServer(key);
      }
    }
    return lastServer;
  }

  private Server chooseServerWhenTimeout(Object key) {
    synchronized (lock) {
      if (isTimeOut()) {
        chooseNextServer(key);
      }
    }
    return lastServer;
  }

  private Server chooseServerErrorThresholdMet(Object key) {
    synchronized (lock) {
      if (errorThresholdMet) {
        chooseNextServer(key);
        errorThresholdMet = false;
      }
    }
    return lastServer;
  }

  private boolean isTimeOut() {
    return Configuration.INSTANCE.getSessionTimeoutInSeconds() > 0
        && System.currentTimeMillis()
            - this.lastAccessedTime > ((long) Configuration.INSTANCE.getSessionTimeoutInSeconds()
                * MILLI_COUNT_IN_SECOND);
  }

  private boolean isErrorThresholdMet() {
    AbstractLoadBalancer lb = (AbstractLoadBalancer) getLoadBalancer();
    LoadBalancerStats stats = lb.getLoadBalancerStats();

    if (stats != null && stats.getServerStats() != null && stats.getServerStats().size() > 0) {
      ServerStats serverStats = stats.getSingleServerStat(lastServer);
      int successiveFaildCount = serverStats.getSuccessiveConnectionFailureCount();
      if (Configuration.INSTANCE.getSuccessiveFailedTimes() > 0
          && successiveFaildCount >= Configuration.INSTANCE.getSuccessiveFailedTimes()) {
        serverStats.clearSuccessiveConnectionFailureCount();
        return true;
      }
    }
    return false;
  }

  @Override
  public Server choose(Object key) {
    if (lastServer == null) {
      return chooseInitialServer(key);
    }

    if (isTimeOut()) {
      LOG.warn("session timeout. choose another server.");
      return chooseServerWhenTimeout(key);
    } else {
      this.lastAccessedTime = System.currentTimeMillis();
    }

    if (isErrorThresholdMet()) {
      LOG.warn("reached max error. choose another server.");
      errorThresholdMet = true;
      return chooseServerErrorThresholdMet(key);
    }

    return lastServer;
  }

  @Override
  public void setLoadBalancer(ILoadBalancer lb) {
    this.lb = lb;
  }

  @Override
  public ILoadBalancer getLoadBalancer() {
    return this.lb;
  }
}
