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

public class EdgeInvocationMeter extends ConsumerInvocationMeter {
  private final Timer providerDecodeRequestTimer;

  private final Timer providerEncodeResponseTimer;

  private final Timer sendResponseTimer;

  public EdgeInvocationMeter(MeterRegistry meterRegistry, String name, Tags tags,
      MetricsBootstrapConfig metricsBootstrapConfig) {
    super(meterRegistry, name, tags, metricsBootstrapConfig);
    providerDecodeRequestTimer = Timer.builder(name)
        .tags(tags.and(MeterInvocationConst.TAG_TYPE, MeterInvocationConst.TAG_STAGE,
            MeterInvocationConst.TAG_STAGE, InvocationStageTrace.STAGE_PROVIDER_DECODE_REQUEST))
        .register(meterRegistry);
    providerEncodeResponseTimer = Timer.builder(name)
        .tags(tags.and(MeterInvocationConst.TAG_TYPE, MeterInvocationConst.TAG_STAGE,
            MeterInvocationConst.TAG_STAGE, InvocationStageTrace.STAGE_PROVIDER_ENCODE_RESPONSE))
        .register(meterRegistry);
    sendResponseTimer = Timer.builder(name).tags(tags.and(MeterInvocationConst.TAG_TYPE, MeterInvocationConst.TAG_STAGE,
            MeterInvocationConst.TAG_STAGE, InvocationStageTrace.STAGE_PROVIDER_SEND))
        .register(meterRegistry);
  }

  @Override
  public void onInvocationFinish(InvocationFinishEvent event) {
    super.onInvocationFinish(event);
    InvocationStageTrace invocationStageTrace = event.getInvocation().getInvocationStageTrace();

    providerDecodeRequestTimer.record(invocationStageTrace.calcProviderDecodeRequest(), TimeUnit.NANOSECONDS);
    providerEncodeResponseTimer.record(invocationStageTrace.calcProviderEncodeResponse(), TimeUnit.NANOSECONDS);
    sendResponseTimer.record(invocationStageTrace.calcProviderSendResponse(), TimeUnit.NANOSECONDS);
  }
}
