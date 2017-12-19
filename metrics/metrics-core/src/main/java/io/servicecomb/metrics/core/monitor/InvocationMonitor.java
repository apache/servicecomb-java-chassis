/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.metrics.core.monitor;

import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.MonitorConfig;

import io.servicecomb.metrics.core.metric.InvocationMetric;

public class InvocationMonitor {
  private final String operationName;

  private final BasicCounter waitInQueue;

  private final TimerMonitor lifeTimeInQueue;

  private final TimerMonitor executionTime;

  private final TimerMonitor consumerLatency;

  private final TimerMonitor producerLatency;

  public String getOperationName() {
    return operationName;
  }

  public BasicCounter getWaitInQueue() {
    return waitInQueue;
  }

  public TimerMonitor getLifeTimeInQueue() {
    return lifeTimeInQueue;
  }

  public TimerMonitor getExecutionTime() {
    return executionTime;
  }

  public TimerMonitor getConsumerLatency() {
    return consumerLatency;
  }

  public TimerMonitor getProducerLatency() {
    return producerLatency;
  }

  public InvocationMonitor(String operationName) {
    this.operationName = operationName;
    this.waitInQueue = new BasicCounter(MonitorConfig.builder("waitInQueue").build());
    this.lifeTimeInQueue = new TimerMonitor("lifeTimeInQueue");
    this.executionTime = new TimerMonitor("executionTime");
    this.consumerLatency = new TimerMonitor("consumerLatency");
    this.producerLatency = new TimerMonitor("producerLatency");
  }

  public InvocationMetric toInvocationMetric(int pollerIndex) {
    return new InvocationMetric(this.getOperationName(), this.getWaitInQueue().getValue(pollerIndex).longValue(),
        this.lifeTimeInQueue.toTimerMetric(pollerIndex),
        this.executionTime.toTimerMetric(pollerIndex),
        this.consumerLatency.toTimerMetric(pollerIndex),
        this.producerLatency.toTimerMetric(pollerIndex));
  }
}
