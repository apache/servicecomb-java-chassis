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

public class InstanceCalculationMetric {
  private final Map<String, ConsumerInvocationMetric> consumerMetrics;

  private final Map<String, ProducerInvocationMetric> producerMetrics;

  //TODO:current java chassis unable know invocation type before starting process,totalWaitInQueue = ProducerInvocation + UnknownTypeInvocation
  private final long totalWaitInQueue;

  private final long producerWaitInQueue;

  private final TimerMetric lifeTimeInQueue;

  private final TimerMetric executionTime;

  private final TimerMetric consumerLatency;

  private final TimerMetric producerLatency;

  private final CallMetric consumerCall;

  private final CallMetric producerCall;

  public long getTotalWaitInQueue() {
    return totalWaitInQueue;
  }

  public long getProducerWaitInQueue() {
    return producerWaitInQueue;
  }

  public Map<String, ConsumerInvocationMetric> getConsumerMetrics() {
    return consumerMetrics;
  }

  public Map<String, ProducerInvocationMetric> getProducerMetrics() {
    return producerMetrics;
  }

  public TimerMetric getLifeTimeInQueue() {
    return lifeTimeInQueue;
  }

  public TimerMetric getExecutionTime() {
    return executionTime;
  }

  public TimerMetric getConsumerLatency() {
    return consumerLatency;
  }

  public TimerMetric getProducerLatency() {
    return producerLatency;
  }

  public CallMetric getConsumerCall() {
    return consumerCall;
  }

  public CallMetric getProducerCall() {
    return producerCall;
  }

  public InstanceCalculationMetric() {
    this.totalWaitInQueue = 0;
    this.producerWaitInQueue = 0;
    this.consumerMetrics = new HashMap<>();
    this.producerMetrics = new HashMap<>();
    this.lifeTimeInQueue = new TimerMetric(MetricsConst.INSTANCE_PRODUCER_PREFIX + ".lifeTimeInQueue");
    this.executionTime = new TimerMetric(MetricsConst.INSTANCE_PRODUCER_PREFIX + ".executionTime");
    this.consumerLatency = new TimerMetric(MetricsConst.INSTANCE_CONSUMER_PREFIX + ".consumerLatency");
    this.producerLatency = new TimerMetric(MetricsConst.INSTANCE_PRODUCER_PREFIX + ".producerLatency");
    this.consumerCall = new CallMetric(MetricsConst.INSTANCE_CONSUMER_PREFIX + ".consumerCall");
    this.producerCall = new CallMetric(MetricsConst.INSTANCE_PRODUCER_PREFIX + ".producerCall");
  }

  public InstanceCalculationMetric(long totalWaitInQueue, long producerWaitInQueue,
      Map<String, ConsumerInvocationMetric> consumerMetrics,
      Map<String, ProducerInvocationMetric> producerMetrics,
      TimerMetric lifeTimeInQueue, TimerMetric executionTime,
      TimerMetric consumerLatency, TimerMetric producerLatency,
      CallMetric consumerCall, CallMetric producerCall) {
    this.totalWaitInQueue = totalWaitInQueue;
    this.producerWaitInQueue = producerWaitInQueue;
    this.consumerMetrics = consumerMetrics;
    this.producerMetrics = producerMetrics;
    this.lifeTimeInQueue = lifeTimeInQueue;
    this.executionTime = executionTime;
    this.consumerLatency = consumerLatency;
    this.producerLatency = producerLatency;
    this.consumerCall = consumerCall;
    this.producerCall = producerCall;
  }
}
