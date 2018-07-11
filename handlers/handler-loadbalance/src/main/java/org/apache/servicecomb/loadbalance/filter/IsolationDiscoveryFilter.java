package org.apache.servicecomb.loadbalance.filter;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.event.AlarmEvent.Type;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.loadbalance.Configuration;
import org.apache.servicecomb.loadbalance.ServiceCombLoadBalancerStats;
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
    return settings;
  }

  private boolean allowVisit(Invocation invocation, MicroserviceInstance instance) {
    Settings settings = createSettings(invocation);
    ServiceCombServerStats serverStats = ServiceCombLoadBalancerStats.INSTANCE.getServiceCombServerStats(instance);
    if (serverStats == null) {
      // first time accessed.
      return true;
    }

    if (!checkThresholdAllowed(settings, serverStats)) {
      if ((System.currentTimeMillis() - serverStats.getLastVisitTime()) > settings.singleTestTime) {
        LOGGER.info("The Service {}'s instance {} has been break, will give a single test opportunity.",
            invocation.getMicroserviceName(),
            instance.getInstanceId());
        eventBus.post(new IsolationServerEvent(invocation.getMicroserviceName(), serverStats.getTotalRequests(),
            serverStats.getCountinuousFailureCount(),
            serverStats.getFailedRate(),
            settings.continuousFailureThreshold, settings.errorThresholdPercentage, settings.enableRequestThreshold,
            settings.singleTestTime, Type.CLOSE));
        return true;
      }

      LOGGER.warn("The Service {}'s instance {} has been break!", invocation.getMicroserviceName(),
          instance.getInstanceId());
      eventBus.post(
          new IsolationServerEvent(invocation.getMicroserviceName(), serverStats.getTotalRequests(),
              serverStats.getCountinuousFailureCount(),
              serverStats.getFailedRate(),
              settings.continuousFailureThreshold, settings.errorThresholdPercentage, settings.enableRequestThreshold,
              settings.singleTestTime, Type.OPEN));
      return false;
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

    if (serverStats.getFailedRate() >= settings.errorThresholdPercentage) {
      return false;
    }
    return true;
  }
}
