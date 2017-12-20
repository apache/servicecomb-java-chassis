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

public abstract class ModelMetric {
  private final long waitInQueue;

  private final TimerMetric lifeTimeInQueue;

  private final TimerMetric executionTime;

  private final TimerMetric consumerLatency;

  private final TimerMetric producerLatency;

  private final CallMetric consumerCall;

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

  public ModelMetric(long waitInQueue,
      TimerMetric lifeTimeInQueue, TimerMetric executionTime, TimerMetric consumerLatency,
      TimerMetric producerLatency, CallMetric consumerCall, CallMetric producerCall) {
    this.waitInQueue = waitInQueue;
    this.lifeTimeInQueue = lifeTimeInQueue;
    this.executionTime = executionTime;
    this.consumerLatency = consumerLatency;
    this.producerLatency = producerLatency;
    this.consumerCall = consumerCall;
    this.producerCall = producerCall;
  }
}
