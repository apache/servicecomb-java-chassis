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

package org.apache.servicecomb.metrics.common;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegistryMetric {
  private InstanceMetric instanceMetric;

  private Map<String, ConsumerInvocationMetric> consumerMetrics;

  private Map<String, ProducerInvocationMetric> producerMetrics;

  public InstanceMetric getInstanceMetric() {
    return instanceMetric;
  }

  public Map<String, ConsumerInvocationMetric> getConsumerMetrics() {
    return consumerMetrics;
  }

  public Map<String, ProducerInvocationMetric> getProducerMetrics() {
    return producerMetrics;
  }

  public RegistryMetric(@JsonProperty("instanceMetric") InstanceMetric instanceMetric,
      @JsonProperty("consumerMetrics") Map<String, ConsumerInvocationMetric> consumerMetrics,
      @JsonProperty("producerMetrics") Map<String, ProducerInvocationMetric> producerMetrics) {
    this.consumerMetrics = consumerMetrics;
    this.producerMetrics = producerMetrics;
    this.instanceMetric = instanceMetric;
  }

  public RegistryMetric(SystemMetric systemMetric,
      Map<String, ConsumerInvocationMetric> consumerMetrics,
      Map<String, ProducerInvocationMetric> producerMetrics) {
    this.consumerMetrics = consumerMetrics;
    this.producerMetrics = producerMetrics;

    ConsumerInvocationMetric instanceConsumerInvocationMetric = new ConsumerInvocationMetric("instance",
        MetricsConst.INSTANCE_CONSUMER_PREFIX,
        new TimerMetric(MetricsConst.INSTANCE_CONSUMER_PREFIX + ".consumerLatency"),
        new CallMetric(MetricsConst.INSTANCE_CONSUMER_PREFIX + ".consumerCall"));
    ProducerInvocationMetric instanceProducerInvocationMetric = new ProducerInvocationMetric("instance",
        MetricsConst.INSTANCE_PRODUCER_PREFIX, 0,
        new TimerMetric(MetricsConst.INSTANCE_PRODUCER_PREFIX + ".lifeTimeInQueue"),
        new TimerMetric(MetricsConst.INSTANCE_PRODUCER_PREFIX + ".executionTime"),
        new TimerMetric(MetricsConst.INSTANCE_PRODUCER_PREFIX + ".producerLatency"),
        new CallMetric(MetricsConst.INSTANCE_PRODUCER_PREFIX + ".producerCall"));

    //sum instance level metric
    for (ConsumerInvocationMetric metric : consumerMetrics.values()) {
      instanceConsumerInvocationMetric = instanceConsumerInvocationMetric.merge(metric);
    }
    for (ProducerInvocationMetric metric : producerMetrics.values()) {
      instanceProducerInvocationMetric = instanceProducerInvocationMetric.merge(metric);
    }

    this.instanceMetric = new InstanceMetric(systemMetric,
        instanceConsumerInvocationMetric, instanceProducerInvocationMetric);
  }

  public Map<String, Number> toMap() {
    Map<String, Number> metrics = new HashMap<>();
    metrics.putAll(instanceMetric.toMap());
    for (ConsumerInvocationMetric metric : consumerMetrics.values()) {
      metrics.putAll(metric.toMap());
    }
    for (ProducerInvocationMetric metric : producerMetrics.values()) {
      metrics.putAll(metric.toMap());
    }
    return metrics;
  }
}
