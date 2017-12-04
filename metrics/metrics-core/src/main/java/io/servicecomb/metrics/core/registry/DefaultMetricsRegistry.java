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
import java.util.stream.Collectors;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.servo.monitor.Pollers;

import io.servicecomb.metrics.core.EmbeddedMetricsName;
import io.servicecomb.metrics.core.metric.Metric;
import io.servicecomb.metrics.core.metric.MetricFactory;

public class DefaultMetricsRegistry implements MetricsRegistry {

  private static final String METRICS_POLLING_TIME = "servicecomb.metrics.polling.millisecond";

  private final Map<String, Metric> allRegisteredMetrics = new ConcurrentHashMap<>();

  private final MetricFactory factory;

  public DefaultMetricsRegistry(MetricFactory factory) {
    int pollingTime = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_POLLING_TIME, 5000).get();
    this.factory = factory;
    this.init(String.valueOf(pollingTime));
  }

  public DefaultMetricsRegistry(MetricFactory factory, String pollingInterval) {
    this.factory = factory;
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
  public Metric getMetric(String name) {
    return allRegisteredMetrics.getOrDefault(name, null);
  }

  @Override
  public Metric getOrCreateMetric(Metric metric) {
    Metric metricReturn = allRegisteredMetrics.putIfAbsent(metric.getName(), metric);
    if (metricReturn == null) {
      metricReturn = allRegisteredMetrics.get(metric.getName());
    }
    return metricReturn;
  }

  @Override
  public List<Long> getPollingIntervals() {
    return Pollers.getPollingIntervals();
  }

  @Override
  public Map<String, Number> getAllMetricsValue() {
    return getMetricsValues(allRegisteredMetrics);
  }

  @Override
  public Map<String, Number> getMetricsValues(String operationName) {
    Map<String, Metric> filteredMetrics = allRegisteredMetrics.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith("servicecomb." + operationName))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    return getMetricsValues(filteredMetrics);
  }

  @Override
  public Map<String, Number> getMetricsValues(String operationName, String catalog) {
    Map<String, Metric> filteredMetrics = allRegisteredMetrics.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(String.join(".", "servicecomb", operationName, catalog)))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    return getMetricsValues(filteredMetrics);
  }

  private Map<String, Number> getMetricsValues(Map<String, Metric> metrics) {
    Map<String, Number> metricValues = new HashMap<>();
    for (Entry<String, Metric> entry : metrics.entrySet()) {
      metricValues.putAll(entry.getValue().getAll());
    }
    return metricValues;
  }

  private void initDefaultSupportedMetrics() {
    //prepare for queue
    String instanceCountInQueueName = String
        .format(EmbeddedMetricsName.QUEUE_COUNT_IN_QUEUE, "instance");
    this.registerMetric(factory.createCounter(instanceCountInQueueName));

    String instanceExecutionTime = String
        .format(EmbeddedMetricsName.QUEUE_EXECUTION_TIME, "instance");
    this.registerMetric(factory.createTimer(instanceExecutionTime));

    String lifeTimeInQueueTime = String
        .format(EmbeddedMetricsName.QUEUE_LIFE_TIME_IN_QUEUE, "instance");
    this.registerMetric(factory.createTimer(lifeTimeInQueueTime));
  }
}

