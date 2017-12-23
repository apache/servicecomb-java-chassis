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
    consumerMetrics = new HashMap<>();
    producerMetrics = new HashMap<>();

    //TODO:current java chassis unable know invocation type before starting process,totalWaitInQueue = ProducerInvocation + UnknownTypeInvocation
    long totalWaitInQueue = 0;
    long producerWaitInQueue = 0;
    TimerMetric lifeTimeInQueue = new TimerMetric(MetricsConst.INSTANCE_PRODUCER_PREFIX + ".lifeTimeInQueue");
    TimerMetric executionTime = new TimerMetric(MetricsConst.INSTANCE_PRODUCER_PREFIX + ".executionTime");
    TimerMetric consumerLatency = new TimerMetric(MetricsConst.INSTANCE_CONSUMER_PREFIX + ".consumerLatency");
    TimerMetric producerLatency = new TimerMetric(MetricsConst.INSTANCE_PRODUCER_PREFIX + ".producerLatency");
    CallMetric consumerCall = new CallMetric(MetricsConst.INSTANCE_CONSUMER_PREFIX + ".consumerCall");
    CallMetric producerCall = new CallMetric(MetricsConst.INSTANCE_PRODUCER_PREFIX + ".producerCall");

    for (InvocationMetric metric : invocationMetrics.values()) {
      if (metric != null) {
        if (metric instanceof ConsumerInvocationMetric) {
          ConsumerInvocationMetric consumerMetric = (ConsumerInvocationMetric) metric;
          consumerLatency = consumerLatency.merge(consumerMetric.getConsumerLatency());
          consumerCall = consumerCall.merge(consumerMetric.getConsumerCall());
          consumerMetrics.put(metric.getOperationName(), consumerMetric);
        } else if (metric instanceof ProducerInvocationMetric) {
          ProducerInvocationMetric producerMetric = (ProducerInvocationMetric) metric;
          totalWaitInQueue += producerMetric.getWaitInQueue();
          producerWaitInQueue += producerMetric.getWaitInQueue();
          lifeTimeInQueue = lifeTimeInQueue.merge(producerMetric.getLifeTimeInQueue());
          executionTime = executionTime.merge(producerMetric.getExecutionTime());
          producerLatency = producerLatency.merge(producerMetric.getProducerLatency());
          producerCall = producerCall.merge(producerMetric.getProducerCall());
          producerMetrics.put(metric.getOperationName(), producerMetric);
        } else {
          totalWaitInQueue += metric.getWaitInQueue();
        }
      }
    }

    instanceMetric = new InstanceMetric(totalWaitInQueue, systemMetric,
        new ConsumerInvocationMetric("instance", MetricsConst.INSTANCE_CONSUMER_PREFIX,
            producerWaitInQueue, consumerLatency, consumerCall),
        new ProducerInvocationMetric("instance", MetricsConst.INSTANCE_PRODUCER_PREFIX,
            totalWaitInQueue, lifeTimeInQueue, executionTime, producerLatency, producerCall));
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
