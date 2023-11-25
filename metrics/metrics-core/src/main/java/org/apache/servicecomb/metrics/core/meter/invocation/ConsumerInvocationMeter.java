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

import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

public class ConsumerInvocationMeter extends AbstractInvocationMeter {
  private final Timer consumerEncodeRequestTimer;

  private final Timer consumerDecodeResponseTimer;

  private final Timer consumerGetConnectionTimer;

  private final Timer consumerSendRequestTimer;

  private final Timer consumerWaitResponseTimer;

  public ConsumerInvocationMeter(MeterRegistry meterRegistry, String name, Tags tags,
      MetricsBootstrapConfig metricsBootstrapConfig) {
    super(meterRegistry, name, tags, metricsBootstrapConfig);
    consumerSendRequestTimer = Timer.builder(name)
        .tags(tags.and(MeterInvocationConst.TAG_TYPE, MeterInvocationConst.TAG_STAGE,
            MeterInvocationConst.TAG_STAGE, InvocationStageTrace.STAGE_CONSUMER_SEND)).register(meterRegistry);
    consumerGetConnectionTimer = Timer.builder(name)
        .tags(tags.and(MeterInvocationConst.TAG_TYPE, MeterInvocationConst.TAG_STAGE,
            MeterInvocationConst.TAG_STAGE, InvocationStageTrace.STAGE_CONSUMER_CONNECTION)).register(meterRegistry);
    consumerEncodeRequestTimer = Timer.builder(name)
        .tags(tags.and(MeterInvocationConst.TAG_TYPE, MeterInvocationConst.TAG_STAGE,
            MeterInvocationConst.TAG_STAGE, InvocationStageTrace.STAGE_CONSUMER_ENCODE_REQUEST))
        .register(meterRegistry);
    consumerDecodeResponseTimer = Timer.builder(name)
        .tags(tags.and(MeterInvocationConst.TAG_TYPE, MeterInvocationConst.TAG_STAGE,
            MeterInvocationConst.TAG_STAGE, InvocationStageTrace.STAGE_CONSUMER_DECODE_RESPONSE))
        .register(meterRegistry);
    consumerWaitResponseTimer = Timer.builder(name)
        .tags(tags.and(MeterInvocationConst.TAG_TYPE, MeterInvocationConst.TAG_STAGE,
            MeterInvocationConst.TAG_STAGE, InvocationStageTrace.STAGE_CONSUMER_WAIT)).register(meterRegistry);
  }

  @Override
  public void onInvocationFinish(InvocationFinishEvent event) {
    super.onInvocationFinish(event);

    InvocationStageTrace invocationStageTrace = event.getInvocation().getInvocationStageTrace();
    consumerEncodeRequestTimer.record(invocationStageTrace.calcConsumerEncodeRequest(), TimeUnit.NANOSECONDS);
    consumerSendRequestTimer.record(invocationStageTrace.calcConsumerSendRequest(), TimeUnit.NANOSECONDS);
    consumerGetConnectionTimer.record(invocationStageTrace.calcConnection(), TimeUnit.NANOSECONDS);
    consumerWaitResponseTimer.record(invocationStageTrace.calcWait(), TimeUnit.NANOSECONDS);
    consumerDecodeResponseTimer.record(invocationStageTrace.calcConsumerDecodeResponse(), TimeUnit.NANOSECONDS);
  }
}
