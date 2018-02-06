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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.metrics.common.MetricsConst;
import org.apache.servicecomb.metrics.core.utils.MonitorUtils;

import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.MonitorConfig;

public class ProducerInvocationMonitor {
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

  public ProducerInvocationMonitor(String operation) {
    this.waitInQueue = new BasicCounter(MonitorConfig.builder(MetricsConst.SERVICECOMB_INVOCATION)
        .withTag(MetricsConst.TAG_OPERATION, operation)
        .withTag(MetricsConst.TAG_STAGE, MetricsConst.STAGE_QUEUE)
        .withTag(MetricsConst.TAG_ROLE, MetricsConst.ROLE_PRODUCER)
        .withTag(MetricsConst.TAG_STATISTIC, "waitInQueue")
        .build());

    this.lifeTimeInQueue = new TimerMonitor(operation, MetricsConst.STAGE_QUEUE, MetricsConst.ROLE_PRODUCER);
    this.executionTime = new TimerMonitor(operation, MetricsConst.STAGE_EXECUTION, MetricsConst.ROLE_PRODUCER);
    this.producerLatency = new TimerMonitor(operation, MetricsConst.STAGE_FULL, MetricsConst.ROLE_PRODUCER);
    this.producerCall = new CallMonitor(operation, MetricsConst.STAGE_FULL, MetricsConst.ROLE_PRODUCER);
  }

  public Map<String, Double> toMetric(int windowTimeIndex) {
    Map<String, Double> metrics = new HashMap<>();
    metrics.put(MonitorUtils.getMonitorName(waitInQueue.getConfig()),
        waitInQueue.getValue(windowTimeIndex).doubleValue());
    metrics.putAll(lifeTimeInQueue.toMetric(windowTimeIndex));
    metrics.putAll(executionTime.toMetric(windowTimeIndex));
    metrics.putAll(producerLatency.toMetric(windowTimeIndex));
    metrics.putAll(producerCall.toMetric(windowTimeIndex));
    return metrics;
  }
}
