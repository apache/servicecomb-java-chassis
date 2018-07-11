package org.apache.servicecomb.loadbalance;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 *  Add special stats that com.netflix.loadbalancer.LoadBalancerStats not provided
 */
public class ServiceCombLoadBalancerStats {
  private final static Logger LOGGER = LoggerFactory.getLogger(ServiceCombLoadBalancerStats.class);

  private static final int SERVERSTATS_EXPIRE_MINUTES = 30;

  private static final LoadingCache<ServiceCombServer, ServiceCombServerStats> SERVER_STATES_CACHE =
      CacheBuilder.newBuilder()
          .expireAfterAccess(SERVERSTATS_EXPIRE_MINUTES, TimeUnit.MINUTES)
          .removalListener(new RemovalListener<ServiceCombServer, ServiceCombServerStats>() {
            @Override
            public void onRemoval(RemovalNotification<ServiceCombServer, ServiceCombServerStats> notification) {
              // nothing to do
            }
          })
          .build(
              new CacheLoader<ServiceCombServer, ServiceCombServerStats>() {
                public ServiceCombServerStats load(ServiceCombServer server) {
                  return new ServiceCombServerStats();
                }
              });

  public static final ServiceCombLoadBalancerStats INSTANCE = new ServiceCombLoadBalancerStats();

  private ServiceCombLoadBalancerStats() {

  }

  public void markSuccess(ServiceCombServer server) {
    try {
      SERVER_STATES_CACHE.get(server).markSuccess();
    } catch (ExecutionException e) {
      LOGGER.error("Not expected to happen here, maybe a bug.", e);
    }
  }

  public void markFailure(ServiceCombServer server) {
    try {
      SERVER_STATES_CACHE.get(server).markFailure();
    } catch (ExecutionException e) {
      LOGGER.error("Not expected to happen here, maybe a bug.", e);
    }
  }

  public ServiceCombServerStats getServiceCombServerStats(MicroserviceInstance instance) {
    for (ServiceCombServer server : SERVER_STATES_CACHE.asMap().keySet()) {
      if (server.getInstance().equals(instance)) {
        try {
          return SERVER_STATES_CACHE.get(server);
        } catch (ExecutionException e) {
          LOGGER.error("Not expected to happen here, maybe a bug.", e);
        }
      }
    }
    return null;
  }
}

