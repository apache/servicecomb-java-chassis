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

package org.apache.servicecomb.loadbalance.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.event.AlarmEvent.Type;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.loadbalance.Configuration;
import org.apache.servicecomb.loadbalance.LoadBalancer;
import org.apache.servicecomb.loadbalance.ServerListFilterExt;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.loadbalance.event.IsolationServerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerStats;

public final class IsolationServerListFilter implements ServerListFilterExt {

  private static final Logger LOGGER = LoggerFactory.getLogger(IsolationServerListFilter.class);

  private static final double PERCENT = 100;

  private int errorThresholdPercentage;

  private long singleTestTime;

  private long enableRequestThreshold;

  private int continuousFailureThreshold;

  private LoadBalancer loadBalancer;

  public EventBus eventBus = EventManager.getEventBus();

  @Override
  public int getOrder() {
    return 100;
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.loadbalance.filter.isolation.enabled", true).get();
  }

  @Override
  public void setLoadBalancer(LoadBalancer loadBalancer) {
    this.loadBalancer = loadBalancer;
  }

  @Override
  public List<Server> getFilteredListOfServers(List<Server> servers, Invocation invocation) {
    if (!Configuration.INSTANCE.isIsolationFilterOpen(this.loadBalancer.getMicroServiceName())) {
      return servers;
    }

    List<Server> filteredServers = new ArrayList<>();
    for (Server server : servers) {
      if (allowVisit(server)) {
        filteredServers.add(server);
      }
    }
    return filteredServers;
  }

  private void updateSettings() {
    errorThresholdPercentage = Configuration.INSTANCE
        .getErrorThresholdPercentage(this.loadBalancer.getMicroServiceName());
    singleTestTime = Configuration.INSTANCE.getSingleTestTime(this.loadBalancer.getMicroServiceName());
    enableRequestThreshold = Configuration.INSTANCE.getEnableRequestThreshold(this.loadBalancer.getMicroServiceName());
    continuousFailureThreshold = Configuration.INSTANCE
        .getContinuousFailureThreshold(this.loadBalancer.getMicroServiceName());
  }

  private boolean allowVisit(Server server) {
    updateSettings();
    ServerStats serverStats = this.loadBalancer.getLoadBalancerStats().getSingleServerStat(server);
    long totalRequest = serverStats.getTotalRequestsCount();
    long failureRequest = serverStats.getSuccessiveConnectionFailureCount();
    int currentCountinuousFailureCount = 0;
    double currentErrorThresholdPercentage = 0;
    if (totalRequest < enableRequestThreshold) {
      return true;
    }

    if (continuousFailureThreshold > 0) {
      // continuousFailureThreshold has higher priority to decide the result
      currentCountinuousFailureCount = ((ServiceCombServer) server).getCountinuousFailureCount();
      if (currentCountinuousFailureCount < continuousFailureThreshold) {
        return true;
      }
    } else {
      // if continuousFailureThreshold, then check error percentage
      currentErrorThresholdPercentage = (failureRequest / (double) totalRequest) * PERCENT;
      if (currentErrorThresholdPercentage < errorThresholdPercentage) {
        return true;
      }
    }

    if ((System.currentTimeMillis() - ((ServiceCombServer) server).getLastVisitTime()) > singleTestTime) {
      LOGGER.info("The Service {}'s instance {} has been break, will give a single test opportunity.",
          this.loadBalancer.getMicroServiceName(),
          server);
      eventBus.post(new IsolationServerEvent(this.loadBalancer.getMicroServiceName(), totalRequest,
          currentCountinuousFailureCount,
          currentErrorThresholdPercentage,
          continuousFailureThreshold, errorThresholdPercentage, enableRequestThreshold,
          singleTestTime, Type.CLOSE));
      return true;
    }

    LOGGER.warn("The Service {}'s instance {} has been break!", this.loadBalancer.getMicroServiceName(), server);
    eventBus.post(
        new IsolationServerEvent(this.loadBalancer.getMicroServiceName(), totalRequest, currentCountinuousFailureCount,
            currentErrorThresholdPercentage,
            continuousFailureThreshold, errorThresholdPercentage, enableRequestThreshold,
            singleTestTime, Type.OPEN));
    return false;
  }
}
