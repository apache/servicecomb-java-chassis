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

package org.apache.servicecomb.metrics.core;

import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.metrics.MetricsConst;
import org.apache.servicecomb.swagger.invocation.InvocationType;

import com.netflix.servo.monitor.Counter;

public class InvocationMetricsManager {

  //invocationName -> statusCode -> ConsumerInvocationMetrics
  private final Map<String, Map<Integer, ConsumerInvocationMetrics>> consumerMetrics;

  //invocationName -> statusCode -> ProducerInvocationMetrics
  private final Map<String, Map<Integer, ProducerInvocationMetrics>> producerMetrics;

  //invocationName -> Counter
  private final Map<String, Counter> waitInQueueCounters;

  private static final InvocationMetricsManager INSTANCE = new InvocationMetricsManager();

  public static InvocationMetricsManager getInstance() {
    return INSTANCE;
  }

  private InvocationMetricsManager() {
    this.consumerMetrics = new ConcurrentHashMapEx<>();
    this.producerMetrics = new ConcurrentHashMapEx<>();
    this.waitInQueueCounters = new ConcurrentHashMapEx<>();
  }

  public void incrementWaitInQueue(String invocationName) {
    updateWaitInQueue(invocationName, 1);
  }

  public void decrementWaitInQueue(String invocationName) {
    updateWaitInQueue(invocationName, -1);
  }

  private void updateWaitInQueue(String invocationName, long value) {
    waitInQueueCounters.computeIfAbsent(invocationName,
        f -> MonitorManager.getInstance().getCounter(
            MetricsConst.SERVICECOMB_INVOCATION, MetricsConst.TAG_OPERATION, invocationName,
            MetricsConst.TAG_STAGE, MetricsConst.STAGE_QUEUE,
            MetricsConst.TAG_ROLE, String.valueOf(InvocationType.PRODUCER).toLowerCase(),
            MetricsConst.TAG_STATISTIC, "waitInQueue")).increment(value);
  }

  public void updateProducer(String invocationName, int statusCode, long inQueueNanoTime, long executionElapsedNanoTime,
      long totalElapsedNanoTime) {
    producerMetrics.computeIfAbsent(invocationName, f -> new ConcurrentHashMapEx<>())
        .computeIfAbsent(statusCode, f -> new ProducerInvocationMetrics(
            MetricsConst.TAG_OPERATION, invocationName,
            MetricsConst.TAG_ROLE, String.valueOf(InvocationType.PRODUCER).toLowerCase(),
            MetricsConst.TAG_STATUS, String.valueOf(statusCode)))
        .update(inQueueNanoTime, executionElapsedNanoTime, totalElapsedNanoTime);
  }

  public void updateConsumer(String invocationName, int statusCode, long totalElapsedNanoTime) {
    consumerMetrics.computeIfAbsent(invocationName, f -> new ConcurrentHashMapEx<>())
        .computeIfAbsent(statusCode, f -> new ConsumerInvocationMetrics(
            MetricsConst.TAG_OPERATION, invocationName,
            MetricsConst.TAG_ROLE, String.valueOf(InvocationType.CONSUMER).toLowerCase(),
            MetricsConst.TAG_STATUS, String.valueOf(statusCode))).update(totalElapsedNanoTime);
  }
}
