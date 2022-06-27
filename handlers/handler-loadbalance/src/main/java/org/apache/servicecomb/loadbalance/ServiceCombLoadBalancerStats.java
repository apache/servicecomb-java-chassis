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

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.consumer.MicroserviceInstancePing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.netflix.config.DynamicPropertyFactory;

/**
 *  Add special stats that com.netflix.loadbalancer.LoadBalancerStats not provided
 */
public class ServiceCombLoadBalancerStats {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombLoadBalancerStats.class);

  private final Map<ServiceCombServer, ServiceCombServerStats> pingView = new ConcurrentHashMap<>();

  private int serverExpireInSeconds = DynamicPropertyFactory.getInstance()
      .getIntProperty(Configuration.SERVER_EXPIRED_IN_SECONDS, 300).get();

  private long timerIntervalInMillis = DynamicPropertyFactory.getInstance()
      .getLongProperty(Configuration.TIMER_INTERVAL_IN_MILLIS, 10000).get();

  private LoadingCache<ServiceCombServer, ServiceCombServerStats> serverStatsCache;

  private final Map<String, ServiceCombServer> serviceCombServers = new ConcurrentHashMap<>();

  public static ServiceCombLoadBalancerStats INSTANCE;

  private Timer timer;

  static {
    INSTANCE = new ServiceCombLoadBalancerStats();
    INSTANCE.init();
  }

  /**
   * Should be singleton, use it only for testing
   */
  ServiceCombLoadBalancerStats() {
  }

  public void markIsolated(ServiceCombServer server, boolean isolated) {
    try {
      serverStatsCache.get(server).markIsolated(isolated);
    } catch (ExecutionException e) {
      LOGGER.error("Not expected to happen, maybe a bug.", e);
    }
  }

  public void markSuccess(ServiceCombServer server) {
    try {
      serverStatsCache.get(server).markSuccess();
    } catch (ExecutionException e) {
      LOGGER.error("Not expected to happen, maybe a bug.", e);
    }
  }

  public void markFailure(ServiceCombServer server) {
    try {
      serverStatsCache.get(server).markFailure();
    } catch (ExecutionException e) {
      LOGGER.error("Not expected to happen, maybe a bug.", e);
    }
  }

  public ServiceCombServerStats getServiceCombServerStats(ServiceCombServer server) {
    try {
      return serverStatsCache.get(server);
    } catch (ExecutionException e) {
      LOGGER.error("Not expected to happen, maybe a bug.", e);
      return null;
    }
  }

  public ServiceCombServer getServiceCombServer(MicroserviceInstance instance) {
    return serviceCombServers.get(instance.getInstanceId());
  }

  @VisibleForTesting
  void setServerExpireInSeconds(int sec) {
    this.serverExpireInSeconds = sec;
  }

  @VisibleForTesting
  void setTimerIntervalInMillis(int millis) {
    this.timerIntervalInMillis = millis;
  }

  @VisibleForTesting
  Map<ServiceCombServer, ServiceCombServerStats> getPingView() {
    return this.pingView;
  }

  @VisibleForTesting
  public void init() {
    // for testing
    if (timer != null) {
      timer.cancel();
    }
    if (serverStatsCache != null) {
      serverStatsCache.cleanUp();
    }

    pingView.clear();

    serverStatsCache =
        CacheBuilder.newBuilder()
            .expireAfterAccess(serverExpireInSeconds, TimeUnit.SECONDS)
            .removalListener(
                (RemovalListener<ServiceCombServer, ServiceCombServerStats>) notification -> {
                  ServiceCombServer server = notification.getKey();
                  LOGGER.info("stats of instance {} removed, host is {}",
                      server.getInstance().getInstanceId(), server.getHost());
                  pingView.remove(notification.getKey());
                  serviceCombServers.remove(notification.getKey());
                })
            .build(
                new CacheLoader<ServiceCombServer, ServiceCombServerStats>() {
                  public ServiceCombServerStats load(ServiceCombServer server) {
                    ServiceCombServerStats stats = new ServiceCombServerStats(server.getMicroserviceName());
                    pingView.put(server, stats);
                    serviceCombServers.put(server.getInstance().getInstanceId(), server);
                    return stats;
                  }
                });

    timer = new Timer("LoadBalancerStatsTimer", true);
    timer.schedule(new TimerTask() {
      private final MicroserviceInstancePing ping = SPIServiceUtils.getPriorityHighestService(MicroserviceInstancePing.class);

      @Override
      public void run() {
        try {
          Map<ServiceCombServer, ServiceCombServerStats> allServers = pingView;
          allServers.forEach((server, stats) -> {
            if ((System.currentTimeMillis() - stats.getLastVisitTime() > timerIntervalInMillis) && !ping
                .ping(server.getInstance())) {
              LOGGER.info("ping mark server {} failure.", server.getInstance().getInstanceId());
              stats.markFailure();
            }
          });
          serverStatsCache.cleanUp();
        } catch (Throwable e) {
          LOGGER.warn("LoadBalancerStatsTimer error.", e);
        }
      }
    }, timerIntervalInMillis, timerIntervalInMillis);
  }
}

