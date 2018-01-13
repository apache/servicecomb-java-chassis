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
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.servicecomb.metrics.common.ConsumerInvocationMetric;
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
    familySamples.add(getFamilySamples("Instance Level", registryMetric.getInstanceMetric().toMap()));
    for (Entry<String, ConsumerInvocationMetric> consumerMetric : registryMetric.getConsumerMetrics().entrySet()) {
      familySamples
          .add(getFamilySamples(consumerMetric.getKey() + " Consumer Side", consumerMetric.getValue().toMap()));
    }
    for (Entry<String, ProducerInvocationMetric> producerMetric : registryMetric.getProducerMetrics().entrySet()) {
      familySamples
          .add(getFamilySamples(producerMetric.getKey() + " Producer Side", producerMetric.getValue().toMap()));
    }
    return familySamples;
  }

  private MetricFamilySamples getFamilySamples(String name, Map<String, Number> metrics) {
    List<Sample> samples = metrics.entrySet()
        .stream()
        .map((entry) -> new Sample(entry.getKey().replace(".", "_"),
            new ArrayList<>(), new ArrayList<>(), entry.getValue().doubleValue()))
        .collect(Collectors.toList());
    return new MetricFamilySamples(name, Type.UNTYPED, name + " Metrics", samples);
  }
}
