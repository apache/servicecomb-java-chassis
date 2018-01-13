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

public class ProducerInvocationMetric extends InvocationMetric {
  private final long waitInQueue;

  private final TimerMetric lifeTimeInQueue;

  private final TimerMetric executionTime;

  private final TimerMetric producerLatency;

  private final CallMetric producerCall;

  public long getWaitInQueue() {
    return waitInQueue;
  }

  public TimerMetric getLifeTimeInQueue() {
    return lifeTimeInQueue;
  }

  public TimerMetric getExecutionTime() {
    return executionTime;
  }

  public TimerMetric getProducerLatency() {
    return producerLatency;
  }

  public CallMetric getProducerCall() {
    return producerCall;
  }

  public ProducerInvocationMetric(@JsonProperty("operationName") String operationName,
      @JsonProperty("prefix") String prefix,
      @JsonProperty("waitInQueue") long waitInQueue,
      @JsonProperty("lifeTimeInQueue") TimerMetric lifeTimeInQueue,
      @JsonProperty("executionTime") TimerMetric executionTime,
      @JsonProperty("producerLatency") TimerMetric producerLatency,
      @JsonProperty("producerCall") CallMetric producerCall) {
    super(operationName, prefix);
    this.waitInQueue = waitInQueue;
    this.lifeTimeInQueue = lifeTimeInQueue;
    this.executionTime = executionTime;
    this.producerLatency = producerLatency;
    this.producerCall = producerCall;
  }

  public ProducerInvocationMetric merge(ProducerInvocationMetric metric) {
    return new ProducerInvocationMetric(this.getOperationName(), this.getPrefix(),
        this.getWaitInQueue() + metric.getWaitInQueue(),
        lifeTimeInQueue.merge(metric.getLifeTimeInQueue()),
        executionTime.merge(metric.getExecutionTime()),
        producerLatency.merge(metric.getProducerLatency()),
        producerCall.merge(metric.getProducerCall()));
  }

  public Map<String, Number> toMap() {
    Map<String, Number> metrics = new HashMap<>();
    metrics.put(getPrefix() + ".waitInQueue.count", getWaitInQueue());
    metrics.putAll(lifeTimeInQueue.toMap());
    metrics.putAll(executionTime.toMap());
    metrics.putAll(producerLatency.toMap());
    metrics.putAll(producerCall.toMap());
    return metrics;
  }
}
