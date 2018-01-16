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

package org.apache.servicecomb.metrics.prometheus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.servicecomb.metrics.common.CallMetric;
import org.apache.servicecomb.metrics.common.ConsumerInvocationMetric;
import org.apache.servicecomb.metrics.common.DoubleMetricValue;
import org.apache.servicecomb.metrics.common.LongMetricValue;
import org.apache.servicecomb.metrics.common.ProducerInvocationMetric;
import org.apache.servicecomb.metrics.common.RegistryMetric;
import org.apache.servicecomb.metrics.core.publish.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;

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

    List<Sample> instanceSamples = new ArrayList<>();
    instanceSamples.addAll(convertMetricValues(registryMetric.getInstanceMetric().getSystemMetric().toMap()));
    instanceSamples.addAll(convertConsumerMetric(registryMetric.getInstanceMetric().getConsumerMetric()));
    instanceSamples.addAll(convertCallMetric(registryMetric.getInstanceMetric().getConsumerMetric().getConsumerCall()));
    instanceSamples.addAll(convertProducerMetric(registryMetric.getInstanceMetric().getProducerMetric()));
    instanceSamples.addAll(convertCallMetric(registryMetric.getInstanceMetric().getProducerMetric().getProducerCall()));
    familySamples
        .add(new MetricFamilySamples("Instance Level", Type.UNTYPED, "Instance Level Metrics", instanceSamples));

    if (registryMetric.getConsumerMetrics().size() != 0) {
      List<Sample> consumerSamples = new ArrayList<>();
      for (ConsumerInvocationMetric metric : registryMetric.getConsumerMetrics().values()) {
        consumerSamples.addAll(convertConsumerMetric(metric));
        consumerSamples.addAll(convertCallMetric(metric.getConsumerCall()));
      }
      familySamples
          .add(new MetricFamilySamples("Consumer Side", Type.UNTYPED, "Consumer Side Metrics", consumerSamples));
    }

    if (registryMetric.getCustomMetrics().size() != 0) {
      familySamples.add(getFamilySamples("User Custom", registryMetric.getCustomMetrics()));
    }


    if (registryMetric.getProducerMetrics().size() != 0) {
      List<Sample> producerSamples = new ArrayList<>();
      for (ProducerInvocationMetric metric : registryMetric.getProducerMetrics().values()) {
        producerSamples.addAll(convertProducerMetric(metric));
        producerSamples.addAll(convertCallMetric(metric.getProducerCall()));
      }
      familySamples
          .add(new MetricFamilySamples("Producer Side", Type.UNTYPED, "Producer Side Metrics", producerSamples));
    }

    return familySamples;
  }

  private <T extends Number> MetricFamilySamples getFamilySamples(String name, Map<String, T> metrics) {
    List<Sample> samples = metrics.entrySet()
        .stream()
        .map((entry) -> new Sample(entry.getKey().replace(".", "_"),
            new ArrayList<>(), new ArrayList<>(), entry.getValue().doubleValue()))
        .collect(Collectors.toList());
    return new MetricFamilySamples(name, Type.UNTYPED, name + " Metrics", samples);
  private List<Sample> convertConsumerMetric(ConsumerInvocationMetric metric) {
    return convertMetricValues(metric.getConsumerLatency().toMap());
  }

  private List<Sample> convertProducerMetric(ProducerInvocationMetric metric) {
    List<Sample> samples = new ArrayList<>();
    samples.addAll(convertMetricValues(metric.getExecutionTime().toMap()));
    samples.addAll(convertMetricValues(metric.getLifeTimeInQueue().toMap()));
    samples.addAll(convertMetricValues(metric.getProducerLatency().toMap()));
    samples.add(
        new Sample(formatMetricName(metric.getPrefix() + ".waitInQueue.count"), new ArrayList<>(), new ArrayList<>(),
            (double) metric.getWaitInQueue()));
    return samples;
  }

  private List<Sample> convertMetricValues(Map<String, Number> metrics) {
    return metrics.entrySet().stream().map((entry) ->
        new Sample(formatMetricName(entry.getKey()), new ArrayList<>(), new ArrayList<>(),
            entry.getValue().doubleValue())).collect(Collectors.toList());
  }

  private List<Sample> convertCallMetric(CallMetric metric) {
    List<Sample> samples = new ArrayList<>();
    String totalName = formatMetricName(metric.getPrefix() + ".total");
    for (LongMetricValue value : metric.getTotalValues()) {
      samples.add(new Sample(totalName,
          new ArrayList<>(value.getDimensions().keySet()), new ArrayList<>(value.getDimensions().values()),
          (double) value.getValue()));
    }
    String tpsName = formatMetricName(metric.getPrefix() + ".tps");
    for (DoubleMetricValue value : metric.getTpsValues()) {
      samples.add(new Sample(tpsName,
          new ArrayList<>(value.getDimensions().keySet()), new ArrayList<>(value.getDimensions().values()),
          value.getValue()));
    }
    return samples;
  }

  //convert name for match prometheus
  private String formatMetricName(String name) {
    return name.replace(".", "_");
  }
}
