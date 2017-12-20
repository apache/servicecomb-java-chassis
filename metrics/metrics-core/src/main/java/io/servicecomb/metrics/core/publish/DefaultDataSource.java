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

package io.servicecomb.metrics.core.publish;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.servo.monitor.Pollers;

import io.servicecomb.metrics.core.extra.SystemResource;
import io.servicecomb.metrics.core.metric.RegistryMetric;
import io.servicecomb.metrics.core.monitor.RegistryMonitor;

@Component
public class DefaultDataSource implements DataSource {
  private static final String METRICS_POLLING_TIME = "servicecomb.metrics.polling.time";

  private static final String METRICS_POLLING_MIN = "servicecomb.metrics.polling.min";

  private final List<Long> appliedPollingIntervals;

  private final RegistryMonitor registryMonitor;

  private final SystemResource systemResource;

  private final Map<Integer, RegistryMetric> registryMetrics;

  @Autowired
  public DefaultDataSource(SystemResource systemResource, RegistryMonitor registryMonitor) {
    this(systemResource, registryMonitor,
        DynamicPropertyFactory.getInstance().getStringProperty(METRICS_POLLING_TIME, "10000").get());
  }

  public DefaultDataSource(SystemResource systemResource, RegistryMonitor registryMonitor, String pollingSettings) {
    this.registryMetrics = new ConcurrentHashMap<>();
    this.systemResource = systemResource;
    this.registryMonitor = registryMonitor;
    //需要限制一下Polling的最小时间间隔， Servo推荐是10000（10秒），默认最低限制为100毫秒
    long minPollingTime = DynamicPropertyFactory.getInstance().getLongProperty(METRICS_POLLING_MIN, 100).get();
    System.getProperties().setProperty("servo.pollers", pollingSettings);

    List<Long> intervals = Pollers.getPollingIntervals();
    List<Long> appliedIntervals = new ArrayList<>();
    for (int index = 0; index < intervals.size(); index++) {
      int finalIndex = index;
      long finalInterval = intervals.get(finalIndex) < minPollingTime ? minPollingTime : intervals.get(finalIndex);
      final Runnable poller = () -> reloadRegistryMetric(finalIndex);
      Executors.newScheduledThreadPool(1)
          //for step counter correct work we need poll in time ,otherwise current step will return Datapoint.UNKNOWN (missing last sample)
          .scheduleWithFixedDelay(poller, 0, (long) (finalInterval * 0.5), MILLISECONDS);
      appliedIntervals.add(finalInterval);
    }
    this.appliedPollingIntervals = appliedIntervals;
  }

  @Override
  public RegistryMetric getRegistryMetric(int pollerIndex) {
    return registryMetrics.getOrDefault(pollerIndex, new RegistryMetric(systemResource, registryMonitor, pollerIndex));
  }

  @Override
  public List<Long> getAppliedPollingIntervals() {
    return appliedPollingIntervals;
  }

  private void reloadRegistryMetric(Integer pollingIndex) {
    registryMetrics.put(pollingIndex, new RegistryMetric(systemResource, registryMonitor, pollingIndex));
  }
}
