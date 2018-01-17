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

package org.apache.servicecomb.metrics.core.monitor;

import org.apache.servicecomb.metrics.common.MetricsConst;
import org.apache.servicecomb.metrics.common.ProducerInvocationMetric;

import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.MonitorConfig;

public class ProducerInvocationMonitor extends InvocationMonitor {
  private final BasicCounter waitInQueue;

  private final TimerMonitor lifeTimeInQueue;

  private final TimerMonitor executionTime;

  private final TimerMonitor producerLatency;

  private final CallMonitor producerCall;

  public BasicCounter getWaitInQueue() {
    return waitInQueue;
  }

  public TimerMonitor getLifeTimeInQueue() {
    return lifeTimeInQueue;
  }

  public TimerMonitor getExecutionTime() {
    return executionTime;
  }

  public TimerMonitor getProducerLatency() {
    return producerLatency;
  }

  public CallMonitor getProducerCall() {
    return producerCall;
  }

  public ProducerInvocationMonitor(String operationName) {
    super(operationName, String.format(MetricsConst.PRODUCER_PREFIX_TEMPLATE, operationName));
    this.waitInQueue = new BasicCounter(MonitorConfig.builder(this.getPrefix() + ".waitInQueue.count").build());
    this.lifeTimeInQueue = new TimerMonitor(this.getPrefix() + ".lifeTimeInQueue");
    this.executionTime = new TimerMonitor(this.getPrefix() + ".executionTime");
    this.producerLatency = new TimerMonitor(this.getPrefix() + ".producerLatency");
    this.producerCall = new CallMonitor(this.getPrefix() + ".producerCall");
  }

  public ProducerInvocationMetric toMetric(int windowTimeIndex) {
    return new ProducerInvocationMetric(this.getOperationName(), this.getPrefix(),
        this.getWaitInQueue().getValue(windowTimeIndex).longValue(),
        lifeTimeInQueue.toMetric(windowTimeIndex),
        executionTime.toMetric(windowTimeIndex),
        producerLatency.toMetric(windowTimeIndex),
        producerCall.toMetric(windowTimeIndex));
  }
}
