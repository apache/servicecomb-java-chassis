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

package io.servicecomb.metrics.prometheus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.servicecomb.metrics.common.CallMetric;
import io.servicecomb.metrics.common.ConsumerInvocationMetric;
import io.servicecomb.metrics.common.DoubleMetricValue;
import io.servicecomb.metrics.common.LongMetricValue;
import io.servicecomb.metrics.common.ProducerInvocationMetric;
import io.servicecomb.metrics.common.RegistryMetric;
import io.servicecomb.metrics.core.publish.DataSource;

@Component
public class MetricsCollector extends Collector implements Collector.Describable {

  private final DataSource dataSource;

  @Autowired
  public MetricsCollector(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    return load();
  }

  @Override
  public List<MetricFamilySamples> describe() {
    return load();
  }

  private List<MetricFamilySamples> load() {
    RegistryMetric registryMetric = dataSource.getRegistryMetric();
    List<MetricFamilySamples> familySamples = new ArrayList<>();

    List<Sample> samples = new ArrayList<>();
    samples.addAll(convertMetricValues(registryMetric.getInstanceMetric().getSystemMetric().toMap()));
    samples.addAll(convertConsumerMetric(registryMetric.getInstanceMetric().getConsumerMetric()));
    samples.addAll(convertCallMetric(registryMetric.getInstanceMetric().getConsumerMetric().getConsumerCall()));
    samples.addAll(convertProducerMetric(registryMetric.getInstanceMetric().getProducerMetric()));
    samples.addAll(convertCallMetric(registryMetric.getInstanceMetric().getProducerMetric().getProducerCall()));
    familySamples.add(new MetricFamilySamples("Instance Level", Type.UNTYPED, "Instance Level Metrics", samples));

    if (registryMetric.getConsumerMetrics().size() != 0) {
      samples = new ArrayList<>();
      for (ConsumerInvocationMetric metric : registryMetric.getConsumerMetrics().values()) {
        samples.addAll(convertConsumerMetric(metric));
        samples.addAll(convertCallMetric(metric.getConsumerCall()));
      }
      familySamples.add(new MetricFamilySamples("Consumer Side", Type.UNTYPED, "Consumer Side Metrics", samples));
    }

    if (registryMetric.getProducerMetrics().size() != 0) {
      samples = new ArrayList<>();
      for (ProducerInvocationMetric metric : registryMetric.getProducerMetrics().values()) {
        samples.addAll(convertProducerMetric(metric));
        samples.addAll(convertCallMetric(metric.getProducerCall()));
      }
      familySamples.add(new MetricFamilySamples("Producer Side", Type.UNTYPED, "Producer Side Metrics", samples));
    }

    return familySamples;
  }

  private List<Sample> convertConsumerMetric(ConsumerInvocationMetric metric) {
    return convertMetricValues(metric.getConsumerLatency().toMap());
  }

  private List<Sample> convertProducerMetric(ProducerInvocationMetric metric) {
    List<Sample> samples = new ArrayList<>();
    samples.addAll(convertMetricValues(metric.getExecutionTime().toMap()));
    samples.addAll(convertMetricValues(metric.getLifeTimeInQueue().toMap()));
    samples.addAll(convertMetricValues(metric.getProducerLatency().toMap()));
    samples.add(new Sample(metric.getPrefix() + ".waitInQueue.count", new ArrayList<>(), new ArrayList<>(),
        (double) metric.getWaitInQueue()));
    return samples;
  }

  private List<Sample> convertMetricValues(Map<String, Number> metrics) {
    return metrics.entrySet().stream().map((entry) ->
        new Sample(entry.getKey().replace(".", "_"),
            new ArrayList<>(), new ArrayList<>(), entry.getValue().doubleValue())).collect(Collectors.toList());
  }

  private List<Sample> convertCallMetric(CallMetric metric) {
    List<Sample> samples = new ArrayList<>();
    String totalName = (metric.getPrefix() + ".total").replace(".", "_");
    for (LongMetricValue value : metric.getTotalValue()) {
      samples.add(new Sample(totalName,
          new ArrayList<>(value.getDimensions().keySet()), new ArrayList<>(value.getDimensions().values()),
          (double) value.getValue()));
    }
    String tpsName = (metric.getPrefix() + ".tps").replace(".", "_");
    for (DoubleMetricValue value : metric.getTpsValues()) {
      samples.add(new Sample(tpsName,
          new ArrayList<>(value.getDimensions().keySet()), new ArrayList<>(value.getDimensions().values()),
          value.getValue()));
    }
    return samples;
  }
}
