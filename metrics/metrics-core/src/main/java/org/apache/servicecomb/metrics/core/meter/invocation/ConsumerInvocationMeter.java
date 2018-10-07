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

import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.swagger.invocation.Response;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.Timer;

public class ConsumerInvocationMeter extends AbstractInvocationMeter {

  private Timer clientFiltersRequestTimer;

  private Timer consumerSendRequestTimer;

  private Timer consumerGetConnectionTimer;

  private Timer consumerWriteToBufTimer;

  private Timer consumerWaitResponseTimer;

  private Timer consumerWakeConsumerTimer;

  private Timer clientFiltersResponseTimer;


  public ConsumerInvocationMeter(Registry registry, Id id, Invocation invocation, Response response) {
    super(registry, id, invocation, response);
    clientFiltersRequestTimer =
        registry.timer(id.withTag(MeterInvocationConst.TAG_STAGE, MeterInvocationConst.STAGE_CLIENT_FILTERS_REQUEST));
    consumerSendRequestTimer =
        registry.timer(id.withTag(MeterInvocationConst.TAG_STAGE, MeterInvocationConst.STAGE_CONSUMER_SEND_REQUEST));
    consumerGetConnectionTimer =
        registry.timer(id.withTag(MeterInvocationConst.TAG_STAGE, MeterInvocationConst.STAGE_CONSUMER_GET_CONNECTION));
    consumerWriteToBufTimer =
        registry.timer(id.withTag(MeterInvocationConst.TAG_STAGE, MeterInvocationConst.STAGE_CONSUMER_WRITE_TO_BUF));
    consumerWakeConsumerTimer =
        registry.timer(id.withTag(MeterInvocationConst.TAG_STAGE, MeterInvocationConst.STAGE_CONSUMER_WAKE_CONSUMER));
    clientFiltersResponseTimer =
        registry.timer(id.withTag(MeterInvocationConst.TAG_STAGE, MeterInvocationConst.STAGE_CLIENT_FILTERS_RESPONSE));
    consumerWaitResponseTimer =
        registry.timer(id.withTag(MeterInvocationConst.TAG_STAGE, MeterInvocationConst.STAGE_CONSUMER_WAIT_RESPONSE));
  }

  @Override
  public void onInvocationFinish(InvocationFinishEvent event) {
    super.onInvocationFinish(event);
    InvocationStageTrace invocationStageTrace = event.getInvocation().getInvocationStageTrace();

    clientFiltersRequestTimer.record((long) invocationStageTrace.calcClientFiltersRequestTime(),
        TimeUnit.NANOSECONDS);
    consumerSendRequestTimer.record((long) invocationStageTrace.calcSendRequestTime(),
        TimeUnit.NANOSECONDS);
    consumerGetConnectionTimer.record((long) invocationStageTrace.calcGetConnectionTime(),
        TimeUnit.NANOSECONDS);
    consumerWriteToBufTimer.record((long) invocationStageTrace.calcWriteToBufferTime(),
        TimeUnit.NANOSECONDS);
    consumerWaitResponseTimer.record((long) invocationStageTrace.calcReceiveResponseTime(),
        TimeUnit.NANOSECONDS);
    consumerWakeConsumerTimer.record((long) invocationStageTrace.calcWakeConsumer(),
        TimeUnit.NANOSECONDS);
    clientFiltersResponseTimer.record((long) invocationStageTrace.calcClientFiltersResponseTime(),
        TimeUnit.NANOSECONDS);
  }
}
