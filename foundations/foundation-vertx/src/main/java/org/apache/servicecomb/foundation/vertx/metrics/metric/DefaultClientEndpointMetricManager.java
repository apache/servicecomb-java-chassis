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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.vertx.metrics.MetricsOptionsEx;

import com.google.common.annotations.VisibleForTesting;

import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;

public class DefaultClientEndpointMetricManager {
  private final Vertx vertx;

  private final MetricsOptionsEx metricsOptionsEx;

  // to avoid save too many endpoint that not exist any more
  // must check expired periodically
  private Map<SocketAddress, DefaultClientEndpointMetric> clientEndpointMetricMap = new ConcurrentHashMapEx<>();

  private final ReadWriteLock rwlock = new ReentrantReadWriteLock();

  private AtomicBoolean inited = new AtomicBoolean(false);

  public DefaultClientEndpointMetricManager(Vertx vertx, MetricsOptionsEx metricsOptionsEx) {
    this.vertx = vertx;
    this.metricsOptionsEx = metricsOptionsEx;
  }

  @VisibleForTesting
  public DefaultClientEndpointMetric getClientEndpointMetric(SocketAddress serverAddress) {
    return clientEndpointMetricMap.get(serverAddress);
  }

  public Map<SocketAddress, DefaultClientEndpointMetric> getClientEndpointMetricMap() {
    return clientEndpointMetricMap;
  }

  public DefaultClientEndpointMetric onConnect(SocketAddress serverAddress) {
    if (inited.compareAndSet(false, true)) {
      vertx.setPeriodic(metricsOptionsEx.getCheckClientEndpointMetricIntervalInMilliseconds(),
          this::onCheckClientEndpointMetricExpired);
    }

    rwlock.readLock().lock();
    try {
      DefaultClientEndpointMetric clientEndpointMetric = clientEndpointMetricMap
          .computeIfAbsent(serverAddress, DefaultClientEndpointMetric::new);
      clientEndpointMetric.onConnect();
      return clientEndpointMetric;
    } finally {
      rwlock.readLock().unlock();
    }
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
}
