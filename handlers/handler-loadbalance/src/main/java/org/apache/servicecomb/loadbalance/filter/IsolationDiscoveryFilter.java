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
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.discovery.DiscoveryContext;
import org.apache.servicecomb.registry.discovery.DiscoveryFilter;
import org.apache.servicecomb.registry.discovery.DiscoveryTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;

/**
 * Isolate instances by error metrics
 */
public class IsolationDiscoveryFilter implements DiscoveryFilter {

  public static final String TRYING_INSTANCES_EXISTING = "scb-hasTryingInstances";

  private static final Logger LOGGER = LoggerFactory.getLogger(IsolationDiscoveryFilter.class);

  private static final String EMPTY_INSTANCE_PROTECTION = "servicecomb.loadbalance.filter.isolation.emptyInstanceProtectionEnabled";

  private final DynamicBooleanProperty emptyProtection = DynamicPropertyFactory.getInstance()
      .getBooleanProperty(EMPTY_INSTANCE_PROTECTION, false);

  public class Settings {
    public int errorThresholdPercentage;

    public long singleTestTime;

    public long enableRequestThreshold;

    public int continuousFailureThreshold;

    public int minIsolationTime; // to avoid isolation recover too fast due to no concurrent control in concurrent scenario
  }

  public EventBus eventBus = EventManager.getEventBus();

  @Override
  public int getOrder() {
    return 500;
  }

  public IsolationDiscoveryFilter() {
    emptyProtection.addCallback(() -> {
      boolean newValue = emptyProtection.get();
      LOGGER.info("{} changed from {} to {}", EMPTY_INSTANCE_PROTECTION, emptyProtection, newValue);
    });
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
    instances.entrySet().forEach(stringMicroserviceInstanceEntry -> {
      MicroserviceInstance instance = stringMicroserviceInstanceEntry.getValue();
      if (allowVisit(invocation, instance)) {
        filteredServers.put(stringMicroserviceInstanceEntry.getKey(), instance);
      }
    });

    DiscoveryTreeNode child = new DiscoveryTreeNode();
    if (ZoneAwareDiscoveryFilter.GROUP_Instances_All
        .equals(context.getContextParameter(ZoneAwareDiscoveryFilter.KEY_ZONE_AWARE_STEP)) && filteredServers.isEmpty()
        && emptyProtection.get()) {
      LOGGER.warn("All servers have been isolated, allow one of them based on load balance rule.");
      child.data(instances);
    } else {
      child.data(filteredServers);
    }
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
        if (!ServiceCombServerStats.applyForTryingChance()) {
          // this server hasn't been isolated for enough long time, or there is no trying chance
          return false;
        }
        // [1]we can implement better recovery based on several attempts, but here we do not know if this attempt is success
        invocation.addLocalContext(TRYING_INSTANCES_EXISTING, Boolean.TRUE);
        return true;
      }
      if (!serverStats.isIsolated()) {
        serverStats.markIsolated(true);
        eventBus.post(
            new IsolationServerEvent(invocation, instance, serverStats,
                settings, Type.OPEN, server.getEndpoint()));
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
      serverStats.markIsolated(false);
      eventBus.post(new IsolationServerEvent(invocation, instance, serverStats,
          settings, Type.CLOSE, server.getEndpoint()));
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
    return serverStats.getFailedRate() < settings.errorThresholdPercentage;
  }
}
