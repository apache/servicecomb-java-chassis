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

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.vertx.metrics.MetricsOptionsEx;

import com.google.common.annotations.VisibleForTesting;

import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;

public class DefaultClientEndpointMetricManager {
  private final MetricsOptionsEx metricsOptionsEx;

  // to avoid save too many endpoint that not exist any more
  // must check expired periodically
  private Map<SocketAddress, DefaultClientEndpointMetric> clientEndpointMetricMap = new ConcurrentHashMapEx<>();

  private final Object lock = new Object();

  public DefaultClientEndpointMetricManager(Vertx vertx, MetricsOptionsEx metricsOptionsEx) {
    this.metricsOptionsEx = metricsOptionsEx;
    vertx.setPeriodic(metricsOptionsEx.getCheckClientEndpointMetricIntervalInMilliseconds(),
        this::onCheckClientEndpointMetricExpired);
  }

  @VisibleForTesting
  public Map<SocketAddress, DefaultClientEndpointMetric> getClientEndpointMetricMap() {
    return clientEndpointMetricMap;
  }

  public DefaultClientEndpointMetric getOrCreateClientEndpointMetric(SocketAddress serverAddress) {
    synchronized (lock) {
      DefaultClientEndpointMetric metric = clientEndpointMetricMap
          .computeIfAbsent(serverAddress, DefaultClientEndpointMetric::new);
      metric.incRefCount();
      return metric;
    }
  }

  public DefaultClientEndpointMetric getClientEndpointMetric(SocketAddress serverAddress) {
    return clientEndpointMetricMap.get(serverAddress);
  }

  @VisibleForTesting
  public void onCheckClientEndpointMetricExpired(long periodic) {
    for (DefaultClientEndpointMetric metric : clientEndpointMetricMap.values()) {
      if (metric.isExpired(metricsOptionsEx.getCheckClientEndpointMetricExpiredInNano())) {
        synchronized (lock) {
          if (metric.isExpired(metricsOptionsEx.getCheckClientEndpointMetricExpiredInNano())) {
            clientEndpointMetricMap.remove(metric.getAddress());
          }
        }
      }
    }
  }
}
