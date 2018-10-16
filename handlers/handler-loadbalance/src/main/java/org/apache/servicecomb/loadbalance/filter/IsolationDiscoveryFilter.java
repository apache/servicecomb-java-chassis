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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.event.AlarmEvent.Type;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.loadbalance.Configuration;
import org.apache.servicecomb.loadbalance.ServiceCombLoadBalancerStats;
import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.loadbalance.ServiceCombServerStats;
import org.apache.servicecomb.loadbalance.event.IsolationServerEvent;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryContext;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryFilter;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.netflix.config.DynamicPropertyFactory;

/**
 * Isolate instances by error metrics
 */
public class IsolationDiscoveryFilter implements DiscoveryFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(IsolationDiscoveryFilter.class);

  class Settings {
    int errorThresholdPercentage;

    long singleTestTime;

    long enableRequestThreshold;

    int continuousFailureThreshold;

    int minIsolationTime; // to avoid isolation recover too fast due to no concurrent control in concurrent scenario
  }
  public EventBus eventBus = EventManager.getEventBus();

  @Override
  public int getOrder() {
    return 500;
  }

  @Override
  public boolean enabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.loadbalance.filter.isolation.enabled", true).get();
  }

  @Override
  public boolean isGroupingFilter() {
    return false;
  }

  @Override
  public DiscoveryTreeNode discovery(DiscoveryContext context, DiscoveryTreeNode parent) {
    Map<String, MicroserviceInstance> instances = parent.data();
    Invocation invocation = context.getInputParameters();
    if (!Configuration.INSTANCE.isIsolationFilterOpen(invocation.getMicroserviceName())) {
      return parent;
    }

    Map<String, MicroserviceInstance> filteredServers = new HashMap<>();
    for (String key : instances.keySet()) {
      MicroserviceInstance instance = instances.get(key);
      if (allowVisit(invocation, instance)) {
        filteredServers.put(key, instance);
      }
    }
    DiscoveryTreeNode child = new DiscoveryTreeNode().data(filteredServers);
    parent.child("filterred", child);
    return child;
  }

  private Settings createSettings(Invocation invocation) {
    Settings settings = new Settings();
    settings.errorThresholdPercentage = Configuration.INSTANCE
        .getErrorThresholdPercentage(invocation.getMicroserviceName());
    settings.singleTestTime = Configuration.INSTANCE.getSingleTestTime(invocation.getMicroserviceName());
    settings.enableRequestThreshold = Configuration.INSTANCE
        .getEnableRequestThreshold(invocation.getMicroserviceName());
    settings.continuousFailureThreshold = Configuration.INSTANCE
        .getContinuousFailureThreshold(invocation.getMicroserviceName());
    settings.minIsolationTime = Configuration.INSTANCE
        .getMinIsolationTime(invocation.getMicroserviceName());
    return settings;
  }

  private boolean allowVisit(Invocation invocation, MicroserviceInstance instance) {
    ServiceCombServer server = ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServer(instance);
    if (server == null) {
      // first time accessed.
      return true;
    }
    ServiceCombServerStats serverStats = ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(server);
    Settings settings = createSettings(invocation);
    if (!checkThresholdAllowed(settings, serverStats)) {
      if (serverStats.isIsolated()
          && (System.currentTimeMillis() - serverStats.getLastVisitTime()) > settings.singleTestTime) {
        // [1]we can implement better recovery based on several attempts, but here we do not know if this attempt is success
        LOGGER.info("The Service {}'s instance {} has been isolated for a while, give a single test opportunity.",
            invocation.getMicroserviceName(),
            instance.getInstanceId());
        return true;
      }
      if (!serverStats.isIsolated()) {
        ServiceCombLoadBalancerStats.INSTANCE.markIsolated(server, true);
        eventBus.post(
            new IsolationServerEvent(invocation.getMicroserviceName(), serverStats.getTotalRequests(),
                serverStats.getCountinuousFailureCount(),
                serverStats.getFailedRate(),
                settings.continuousFailureThreshold, settings.errorThresholdPercentage, settings.enableRequestThreshold,
                settings.singleTestTime, Type.OPEN));
        LOGGER.warn("Isolate service {}'s instance {}.", invocation.getMicroserviceName(),
            instance.getInstanceId());
      }
      return false;
    }
    if (serverStats.isIsolated()) {
      // [2] so that we add a feature to isolate for at least a minimal time, and we can avoid
      // high volume of concurrent requests with a percentage of error(e.g. 50%) scenario with no isolation
      if ((System.currentTimeMillis() - serverStats.getLastVisitTime()) <= settings.minIsolationTime) {
        return false;
      }
      ServiceCombLoadBalancerStats.INSTANCE.markIsolated(server, false);
      eventBus.post(new IsolationServerEvent(invocation.getMicroserviceName(), serverStats.getTotalRequests(),
          serverStats.getCountinuousFailureCount(),
          serverStats.getFailedRate(),
          settings.continuousFailureThreshold, settings.errorThresholdPercentage, settings.enableRequestThreshold,
          settings.singleTestTime, Type.CLOSE));
      LOGGER.warn("Recover service {}'s instance {} from isolation.", invocation.getMicroserviceName(),
          instance.getInstanceId());
    }
    return true;
  }

  private boolean checkThresholdAllowed(Settings settings, ServiceCombServerStats serverStats) {
    if (serverStats.getTotalRequests() < settings.enableRequestThreshold) {
      return true;
    }

    if (settings.continuousFailureThreshold > 0) {
      // continuousFailureThreshold has higher priority to decide the result
      if (serverStats.getCountinuousFailureCount() >= settings.continuousFailureThreshold) {
        return false;
      }
    }

    if (settings.errorThresholdPercentage == 0) {
      return true;
    }
    if (serverStats.getFailedRate() >= settings.errorThresholdPercentage) {
      return false;
    }
    return true;
  }
}
