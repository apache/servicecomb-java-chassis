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
package org.apache.servicecomb.metrics.core.meter.invocation;

import java.util.List;

import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.meter.SimpleTimer;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;

public class ConsumerInvocationMeter extends AbstractInvocationMeter {
  private final SimpleTimer consumerEncodeRequestTimer;

  private final SimpleTimer consumerDecodeResponseTimer;

  private final SimpleTimer consumerGetConnectionTimer;

  private final SimpleTimer consumerSendRequestTimer;

  private final SimpleTimer consumerWaitResponseTimer;

  public ConsumerInvocationMeter(Id id, MetricsBootstrapConfig metricsBootstrapConfig) {
    super(id, metricsBootstrapConfig);
    consumerSendRequestTimer = createStageTimer(InvocationStageTrace.STAGE_CONSUMER_SEND);
    consumerGetConnectionTimer = createStageTimer(InvocationStageTrace.STAGE_CONSUMER_CONNECTION);
    consumerEncodeRequestTimer = createStageTimer(InvocationStageTrace.STAGE_CONSUMER_ENCODE_REQUEST);
    consumerDecodeResponseTimer = createStageTimer(InvocationStageTrace.STAGE_CONSUMER_DECODE_RESPONSE);
    consumerWaitResponseTimer = createStageTimer(InvocationStageTrace.STAGE_CONSUMER_WAIT);
  }

  @Override
  public void onInvocationFinish(InvocationFinishEvent event) {
    super.onInvocationFinish(event);

    InvocationStageTrace invocationStageTrace = event.getInvocation().getInvocationStageTrace();
    consumerEncodeRequestTimer.record(invocationStageTrace.calcConsumerEncodeRequest());
    consumerSendRequestTimer.record(invocationStageTrace.calcConsumerSendRequest());
    consumerGetConnectionTimer.record(invocationStageTrace.calcConnection());
    consumerWaitResponseTimer.record(invocationStageTrace.calcWait());
    consumerDecodeResponseTimer.record(invocationStageTrace.calcConsumerDecodeResponse());
  }

  @Override
  public void calcMeasurements(List<Measurement> measurements, long msNow, long secondInterval) {
    super.calcMeasurements(measurements, msNow, secondInterval);

    consumerSendRequestTimer.calcMeasurements(measurements, msNow, secondInterval);
    consumerGetConnectionTimer.calcMeasurements(measurements, msNow, secondInterval);
    consumerEncodeRequestTimer.calcMeasurements(measurements, msNow, secondInterval);
    consumerWaitResponseTimer.calcMeasurements(measurements, msNow, secondInterval);
    consumerDecodeResponseTimer.calcMeasurements(measurements, msNow, secondInterval);
  }
}
