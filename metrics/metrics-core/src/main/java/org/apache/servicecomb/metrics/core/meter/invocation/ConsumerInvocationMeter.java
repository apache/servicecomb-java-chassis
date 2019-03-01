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
import org.apache.servicecomb.foundation.metrics.meter.SimpleTimer;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Registry;

public class ConsumerInvocationMeter extends AbstractInvocationMeter {
  private SimpleTimer clientFiltersRequestTimer;

  private SimpleTimer consumerSendRequestTimer;

  private SimpleTimer consumerGetConnectionTimer;

  private SimpleTimer consumerWriteToBufTimer;

  private SimpleTimer consumerWaitResponseTimer;

  private SimpleTimer consumerWakeConsumerTimer;

  private SimpleTimer clientFiltersResponseTimer;

  public ConsumerInvocationMeter(Registry registry, Id id) {
    super(registry, id);
    clientFiltersRequestTimer = createStageTimer(MeterInvocationConst.STAGE_CLIENT_FILTERS_REQUEST);
    consumerSendRequestTimer = createStageTimer(MeterInvocationConst.STAGE_CONSUMER_SEND_REQUEST);
    consumerGetConnectionTimer = createStageTimer(MeterInvocationConst.STAGE_CONSUMER_GET_CONNECTION);
    consumerWriteToBufTimer = createStageTimer(MeterInvocationConst.STAGE_CONSUMER_WRITE_TO_BUF);
    consumerWakeConsumerTimer = createStageTimer(MeterInvocationConst.STAGE_CONSUMER_WAKE_CONSUMER);
    clientFiltersResponseTimer = createStageTimer(MeterInvocationConst.STAGE_CLIENT_FILTERS_RESPONSE);
    consumerWaitResponseTimer = createStageTimer(MeterInvocationConst.STAGE_CONSUMER_WAIT_RESPONSE);
  }

  @Override
  public void onInvocationFinish(InvocationFinishEvent event) {
    super.onInvocationFinish(event);

    InvocationStageTrace invocationStageTrace = event.getInvocation().getInvocationStageTrace();
    clientFiltersRequestTimer.record((long) invocationStageTrace.calcClientFiltersRequestTime());
    consumerSendRequestTimer.record((long) invocationStageTrace.calcSendRequestTime());
    consumerGetConnectionTimer.record((long) invocationStageTrace.calcGetConnectionTime());
    consumerWriteToBufTimer.record((long) invocationStageTrace.calcWriteToBufferTime());
    consumerWaitResponseTimer.record((long) invocationStageTrace.calcReceiveResponseTime());
    consumerWakeConsumerTimer.record((long) invocationStageTrace.calcWakeConsumer());
    clientFiltersResponseTimer.record((long) invocationStageTrace.calcClientFiltersResponseTime());
  }

  @Override
  public void calcMeasurements(List<Measurement> measurements, long msNow, long secondInterval) {
    super.calcMeasurements(measurements, msNow, secondInterval);

    clientFiltersRequestTimer.calcMeasurements(measurements, msNow, secondInterval);
    consumerSendRequestTimer.calcMeasurements(measurements, msNow, secondInterval);
    consumerGetConnectionTimer.calcMeasurements(measurements, msNow, secondInterval);
    consumerWriteToBufTimer.calcMeasurements(measurements, msNow, secondInterval);
    consumerWaitResponseTimer.calcMeasurements(measurements, msNow, secondInterval);
    consumerWakeConsumerTimer.calcMeasurements(measurements, msNow, secondInterval);
    clientFiltersResponseTimer.calcMeasurements(measurements, msNow, secondInterval);
  }
}
