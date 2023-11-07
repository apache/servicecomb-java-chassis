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

public class EdgeInvocationMeter extends ConsumerInvocationMeter {
  private final SimpleTimer providerDecodeRequestTimer;

  private final SimpleTimer providerEncodeResponseTimer;

  private final SimpleTimer sendResponseTimer;

  public EdgeInvocationMeter(Id id, MetricsBootstrapConfig metricsBootstrapConfig) {
    super(id, metricsBootstrapConfig);
    providerDecodeRequestTimer = createStageTimer(InvocationStageTrace.STAGE_PROVIDER_DECODE_REQUEST);
    providerEncodeResponseTimer = createStageTimer(InvocationStageTrace.STAGE_PROVIDER_ENCODE_RESPONSE);
    sendResponseTimer = createStageTimer(InvocationStageTrace.STAGE_PROVIDER_SEND);
  }

  @Override
  public void onInvocationFinish(InvocationFinishEvent event) {
    super.onInvocationFinish(event);
    InvocationStageTrace invocationStageTrace = event.getInvocation().getInvocationStageTrace();

    providerDecodeRequestTimer.record(invocationStageTrace.calcProviderDecodeRequest());
    providerEncodeResponseTimer.record(invocationStageTrace.calcProviderEncodeResponse());
    sendResponseTimer.record(invocationStageTrace.calcProviderSendResponse());
  }

  @Override
  public void calcMeasurements(List<Measurement> measurements, long msNow, long secondInterval) {
    super.calcMeasurements(measurements, msNow, secondInterval);

    providerDecodeRequestTimer.calcMeasurements(measurements, msNow, secondInterval);
    providerEncodeResponseTimer.calcMeasurements(measurements, msNow, secondInterval);
    sendResponseTimer.calcMeasurements(measurements, msNow, secondInterval);
  }
}
