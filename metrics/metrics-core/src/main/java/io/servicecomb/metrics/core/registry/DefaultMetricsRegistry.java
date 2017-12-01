/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.metrics.core.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.servo.monitor.LongGauge;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.Pollers;

import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.metrics.core.EmbeddedMetricsName;
import io.servicecomb.metrics.core.metric.BasicTimerMetric;
import io.servicecomb.metrics.core.metric.LongGaugeMetric;
import io.servicecomb.metrics.core.metric.Metric;

public class DefaultMetricsRegistry implements MetricsRegistry {

  private static final String METRICS_POLLING_TIME = "servicecomb.metrics.polling.millisecond";

  private final Map<String, Metric> allRegisteredMetrics = new ConcurrentHashMap<>();

  public DefaultMetricsRegistry() {
    int pollingTime = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_POLLING_TIME, 5000).get();
    this.init(String.valueOf(pollingTime));
  }

  public DefaultMetricsRegistry(String pollingInterval) {
    this.init(pollingInterval);
  }

  private void init(String pollingInterval) {
    System.getProperties().setProperty("servo.pollers", pollingInterval);
    initDefaultSupportedMetrics();
  }

  @Override
  public void registerMetric(Metric metric) {
    allRegisteredMetrics.put(metric.getName(), metric);
  }

  @Override
  public List<Long> getPollingIntervals() {
    return Pollers.getPollingIntervals();
  }

  @Override
  public Number getMetricsValue(String name, String tag) {
    if (allRegisteredMetrics.containsKey(name)) {
      return allRegisteredMetrics.get(name).get(tag);
    } else {
      throw new ServiceCombException("can't find metric " + name + " in registry");
    }
  }

  @Override
  public Map<String, Number> getAllMetricsValue() {
    Map<String, Number> metricValues = new HashMap<>();
    for (Entry<String, Metric> entry : allRegisteredMetrics.entrySet()) {
      metricValues.putAll(entry.getValue().getAll());
    }
    return metricValues;
  }

  private void initDefaultSupportedMetrics() {
    //prepare for queue
    this.registerMetric(new LongGaugeMetric(
        new LongGauge(MonitorConfig.builder(EmbeddedMetricsName.INSTANCE_QUEUE_COUNTINQUEUE).build())));
    this.registerMetric(new BasicTimerMetric(EmbeddedMetricsName.INSTANCE_QUEUE_EXECUTIONTIME));
    this.registerMetric(new BasicTimerMetric(EmbeddedMetricsName.INSTANCE_QUEUE_LIFETIMEINQUEUE));
  }
}

