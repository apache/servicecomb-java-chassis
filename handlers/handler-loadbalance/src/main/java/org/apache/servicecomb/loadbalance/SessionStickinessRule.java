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

import com.google.common.annotations.VisibleForTesting;

/**
 * TODO: Implements new SessionStickinessRule. The old implementation is not correct.
 *
 */
public class SessionStickinessRule implements RuleExt {
  private static final Logger LOG = LoggerFactory.getLogger(SessionStickinessRule.class);

  private final Object lock = new Object();

  private LoadBalancer loadBalancer;

  // use random rule as the trigger rule, to prevent consumer instance select the same producer instance.
  private final RuleExt triggerRule;

  private volatile ServiceCombServer lastServer = null;

  private long lastAccessedTime = 0;

  private volatile boolean errorThresholdMet = false;

  private static final int MILLI_COUNT_IN_SECOND = 1000;

  private String microserviceName;

  public SessionStickinessRule() {
    triggerRule = new RoundRobinRuleExt();
  }

  public void setLoadBalancer(LoadBalancer loadBalancer) {
    this.microserviceName = loadBalancer.getMicroServiceName();
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

  @VisibleForTesting
  ServiceCombServer chooseServerWhenTimeout(List<ServiceCombServer> servers, Invocation invocation) {
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

    if (!servers.contains(lastServer)) {
      return chooseNextServer(servers, invocation);
    }

    return lastServer;
  }
}
