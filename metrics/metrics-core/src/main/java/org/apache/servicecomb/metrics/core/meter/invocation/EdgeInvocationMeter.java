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

public class EdgeInvocationMeter extends ConsumerInvocationMeter {
  private SimpleTimer executorQueueTimer;

  private SimpleTimer serverFiltersRequestTimer;

  private SimpleTimer serverFiltersResponseTimer;

  private SimpleTimer sendResponseTimer;

  public EdgeInvocationMeter(Registry registry, Id id) {
    super(registry, id);
    executorQueueTimer = createStageTimer(MeterInvocationConst.STAGE_EXECUTOR_QUEUE);
    serverFiltersRequestTimer = createStageTimer(MeterInvocationConst.STAGE_SERVER_FILTERS_REQUEST);
    serverFiltersResponseTimer = createStageTimer(MeterInvocationConst.STAGE_SERVER_FILTERS_RESPONSE);
    sendResponseTimer = createStageTimer(MeterInvocationConst.STAGE_PRODUCER_SEND_RESPONSE);
  }

  @Override
  public void onInvocationFinish(InvocationFinishEvent event) {
    super.onInvocationFinish(event);
    InvocationStageTrace invocationStageTrace = event.getInvocation().getInvocationStageTrace();

    executorQueueTimer.record((long) invocationStageTrace.calcThreadPoolQueueTime());
    serverFiltersRequestTimer.record((long) invocationStageTrace.calcServerFiltersRequestTime());
    serverFiltersResponseTimer.record((long) invocationStageTrace.calcServerFiltersResponseTime());
    sendResponseTimer.record((long) invocationStageTrace.calcSendResponseTime());
  }

  @Override
  public void calcMeasurements(List<Measurement> measurements, long msNow, long secondInterval) {
    super.calcMeasurements(measurements, msNow, secondInterval);

    executorQueueTimer.calcMeasurements(measurements, msNow, secondInterval);
    serverFiltersRequestTimer.calcMeasurements(measurements, msNow, secondInterval);
    serverFiltersResponseTimer.calcMeasurements(measurements, msNow, secondInterval);
    sendResponseTimer.calcMeasurements(measurements, msNow, secondInterval);
  }
}
