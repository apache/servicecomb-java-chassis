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

package io.servicecomb.metrics.core.metric;

import java.util.HashMap;
import java.util.Map;

import io.servicecomb.metrics.core.MetricsConst;

public class RegistryMetric {

  private final InstanceMetric instanceMetric;

  private final Map<String, ConsumerInvocationMetric> consumerMetrics;

  private final Map<String, ProducerInvocationMetric> producerMetrics;

  public InstanceMetric getInstanceMetric() {
    return instanceMetric;
  }

  public Map<String, ConsumerInvocationMetric> getConsumerMetrics() {
    return consumerMetrics;
  }

  public Map<String, ProducerInvocationMetric> getProducerMetrics() {
    return producerMetrics;
  }

  public RegistryMetric(SystemMetric systemMetric, Map<String, InvocationMetric> invocationMetrics) {
    //sum instance level metric
    InstanceCalculationMetric calculationMetric = new InstanceCalculationMetric();
    for (InvocationMetric metric : invocationMetrics.values()) {
      calculationMetric = metric.merge(calculationMetric);
    }

    this.instanceMetric = new InstanceMetric(calculationMetric.getTotalWaitInQueue(), systemMetric,
        new ConsumerInvocationMetric("instance", MetricsConst.INSTANCE_CONSUMER_PREFIX,
            calculationMetric.getProducerWaitInQueue(),
            calculationMetric.getConsumerLatency(), calculationMetric.getConsumerCall()),
        new ProducerInvocationMetric("instance", MetricsConst.INSTANCE_PRODUCER_PREFIX,
            calculationMetric.getProducerWaitInQueue(),
            calculationMetric.getLifeTimeInQueue(), calculationMetric.getExecutionTime(),
            calculationMetric.getProducerLatency(), calculationMetric.getProducerCall()));
    this.producerMetrics = calculationMetric.getProducerMetrics();
    this.consumerMetrics = calculationMetric.getConsumerMetrics();
  }

  public Map<String, Number> toMap() {
    Map<String, Number> metrics = new HashMap<>();
    metrics.putAll(instanceMetric.getConsumerMetric().toMap());
    metrics.putAll(instanceMetric.getProducerMetric().toMap());
    //will override waitInQueue.count value
    metrics.put(MetricsConst.INSTANCE_PRODUCER_PREFIX + ".waitInQueue.count", instanceMetric.getWaitInQueue());
    for (ConsumerInvocationMetric metric : consumerMetrics.values()) {
      metrics.putAll(metric.toMap());
    }
    for (ProducerInvocationMetric metric : producerMetrics.values()) {
      metrics.putAll(metric.toMap());
    }
    return metrics;
  }
}
