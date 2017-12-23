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

package io.servicecomb.metrics.core.monitor;

import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.MonitorConfig;

import io.servicecomb.metrics.core.MetricsConst;
import io.servicecomb.metrics.core.metric.ConsumerInvocationMetric;
import io.servicecomb.metrics.core.metric.InvocationMetric;
import io.servicecomb.metrics.core.metric.ProducerInvocationMetric;
import io.servicecomb.swagger.invocation.InvocationType;

public class InvocationMonitor extends BasicMonitor {
  private final String operationName;

  private final String consumerPrefix;

  private final String producerPrefix;

  private final BasicCounter waitInQueue;

  private final TimerMonitor lifeTimeInQueue;

  private final TimerMonitor executionTime;

  private final TimerMonitor producerLatency;

  private final TimerMonitor consumerLatency;

  private final CallMonitor producerCall;

  private final CallMonitor consumerCall;

  private InvocationMonitorType invocationMonitorType = InvocationMonitorType.UNKNOWN;

  //TODO:current java chassis unable know invocation type before starting process,so we need set it,can improve later
  public void setInvocationMonitorType(InvocationType invocationType) {
    if (InvocationMonitorType.UNKNOWN.equals(this.invocationMonitorType)) {
      this.invocationMonitorType = invocationType == InvocationType.PRODUCER ?
          InvocationMonitorType.PRODUCER : InvocationMonitorType.CONSUMER;
    }
  }

  public String getOperationName() {
    return operationName;
  }

  public TimerMonitor getConsumerLatency() {
    return consumerLatency;
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

  public CallMonitor getConsumerCall() {
    return consumerCall;
  }

  public CallMonitor getProducerCall() {
    return producerCall;
  }

  public BasicCounter getWaitInQueue() {
    return waitInQueue;
  }

  public InvocationMonitor(String operationName) {
    this.operationName = operationName;
    this.consumerPrefix = String.format(MetricsConst.CONSUMER_PREFIX_TEMPLATE, operationName);
    this.producerPrefix = String.format(MetricsConst.PRODUCER_PREFIX_TEMPLATE, operationName);
    this.waitInQueue = new BasicCounter(MonitorConfig.builder(producerPrefix + ".waitInQueue.count").build());

    this.consumerLatency = new TimerMonitor(consumerPrefix + ".consumerLatency");
    this.consumerCall = new CallMonitor(consumerPrefix + ".consumerCall");

    this.lifeTimeInQueue = new TimerMonitor(producerPrefix + ".lifeTimeInQueue");
    this.executionTime = new TimerMonitor(producerPrefix + ".executionTime");
    this.producerLatency = new TimerMonitor(producerPrefix + ".producerLatency");
    this.producerCall = new CallMonitor(producerPrefix + ".producerCall");
  }

  public InvocationMetric toInvocationMetric(int windowTimeIndex) {
    InvocationMetric metric;
    long queueCount = waitInQueue.getValue(windowTimeIndex).longValue();
    if (invocationMonitorType.equals(InvocationMonitorType.PRODUCER)) {
      metric = new ProducerInvocationMetric(operationName, producerPrefix, queueCount,
          lifeTimeInQueue.toTimerMetric(windowTimeIndex),
          executionTime.toTimerMetric(windowTimeIndex),
          producerLatency.toTimerMetric(windowTimeIndex),
          producerCall.toCallMetric(windowTimeIndex));
    } else if (invocationMonitorType.equals(InvocationMonitorType.CONSUMER)) {
      metric = new ConsumerInvocationMetric(operationName, consumerPrefix, queueCount,
          consumerLatency.toTimerMetric(windowTimeIndex), consumerCall.toCallMetric(windowTimeIndex));
    } else {
      metric = new InvocationMetric(operationName, consumerPrefix, queueCount);
    }
    return metric;
  }
}
