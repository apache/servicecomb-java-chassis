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
package org.apache.servicecomb.foundation.vertx.metrics.metric;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.vertx.metrics.MetricsOptionsEx;

import com.google.common.annotations.VisibleForTesting;

import io.vertx.core.Vertx;

public class DefaultClientEndpointMetricManager {
  private final MetricsOptionsEx metricsOptionsEx;

  // to avoid save too many endpoint that not exist any more
  // must check expired periodically
  private Map<String, DefaultClientEndpointMetric> clientEndpointMetricMap = new ConcurrentHashMapEx<>();

  // clientEndpointMetricMap is thread safe
  // but get/isExpired/remove is not safe
  //  1.isExpired
  //  2.get
  //  3.remove
  // will get a removed instance
  // so must lock the logic
  private final ReadWriteLock rwlock = new ReentrantReadWriteLock();

  public DefaultClientEndpointMetricManager(MetricsOptionsEx metricsOptionsEx) {
    this.metricsOptionsEx = metricsOptionsEx;
  }

  public DefaultClientEndpointMetric getOrCreateEndpointMetric(String address) {
    rwlock.readLock().lock();
    try {
      return clientEndpointMetricMap.computeIfAbsent(address, DefaultClientEndpointMetric::new);
    } finally {
      rwlock.readLock().unlock();
    }
  }

  @VisibleForTesting
  public DefaultClientEndpointMetric getClientEndpointMetric(String serverAddress) {
    return clientEndpointMetricMap.get(serverAddress);
  }

  public Map<String, DefaultClientEndpointMetric> getClientEndpointMetricMap() {
    return clientEndpointMetricMap;
  }

  @VisibleForTesting
  public void onCheckClientEndpointMetricExpired(long periodic) {
    for (DefaultClientEndpointMetric metric : clientEndpointMetricMap.values()) {
      if (metric.isExpired(metricsOptionsEx.getCheckClientEndpointMetricExpiredInNano())) {
        rwlock.writeLock().lock();
        try {
          if (metric.isExpired(metricsOptionsEx.getCheckClientEndpointMetricExpiredInNano())) {
            clientEndpointMetricMap.remove(metric.getAddress());
          }
        } finally {
          rwlock.writeLock().unlock();
        }
      }
    }
  }

  public void setVertx(Vertx vertx) {
    vertx.setPeriodic(metricsOptionsEx.getCheckClientEndpointMetricIntervalInMilliseconds(),
        this::onCheckClientEndpointMetricExpired);
  }
}
