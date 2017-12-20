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

import java.util.Map;

import io.servicecomb.metrics.core.monitor.RegistryMonitor;

public class RegistryMetric {

  private final InstanceMetric instanceMetric;

  private final Map<String, InvocationMetric> invocationMetrics;

  public InstanceMetric getInstanceMetric() {
    return instanceMetric;
  }

  public Map<String, InvocationMetric> getInvocationMetrics() {
    return invocationMetrics;
  }

  public RegistryMetric(RegistryMonitor registryMonitor, int pollerIndex) {
    invocationMetrics = registryMonitor.toInvocationMetrics(pollerIndex);

    //sum instance level metric
    long waitInQueue = 0;
    TimerMetric lifeTimeInQueue = new TimerMetric();
    TimerMetric executionTime = new TimerMetric();
    TimerMetric consumerLatency = new TimerMetric();
    TimerMetric producerLatency = new TimerMetric();

    CallMetric consumerCall = new CallMetric();
    CallMetric producerCall = new CallMetric();

    for (InvocationMetric metric : invocationMetrics.values()) {
      waitInQueue += metric.getWaitInQueue();
      lifeTimeInQueue = lifeTimeInQueue.merge(metric.getLifeTimeInQueue());
      executionTime = executionTime.merge(metric.getExecutionTime());
      consumerLatency = consumerLatency.merge(metric.getConsumerLatency());
      producerLatency = producerLatency.merge(metric.getProducerLatency());
      consumerCall = consumerCall.merge(metric.getConsumerCall());
      producerCall = producerCall.merge(metric.getProducerCall());
    }

    instanceMetric = new InstanceMetric(waitInQueue, lifeTimeInQueue, executionTime, consumerLatency, producerLatency,
        consumerCall, producerCall);
  }
}
