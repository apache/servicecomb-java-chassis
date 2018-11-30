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

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.metrics.meter.AbstractPeriodMeter;
import org.apache.servicecomb.foundation.metrics.meter.SimpleTimer;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Registry;

public abstract class AbstractInvocationMeter extends AbstractPeriodMeter {
  private final Registry registry;

  //total time
  private SimpleTimer totalTimer;

  // prepare time
  private SimpleTimer prepareTimer;

  // handler request
  private SimpleTimer handlersRequestTimer;

  // handler response
  private SimpleTimer handlersResponseTimer;

  private long lastUpdated;

  public AbstractInvocationMeter(Registry registry, Id id) {
    this.registry = registry;
    this.id = id;

    totalTimer = creatStageTimer(MeterInvocationConst.STAGE_TOTAL);
    prepareTimer = creatStageTimer(MeterInvocationConst.STAGE_PREPARE);
    handlersRequestTimer = creatStageTimer(MeterInvocationConst.STAGE_HANDLERS_REQUEST);
    handlersResponseTimer = creatStageTimer(MeterInvocationConst.STAGE_HANDLERS_RESPONSE);
  }

  protected SimpleTimer creatStageTimer(String stageValue) {
    return createTimer(id.withTag(MeterInvocationConst.TAG_STAGE, stageValue));
  }

  protected SimpleTimer createTimer(String tagKey, String tagValue) {
    return createTimer(id.withTag(tagKey, tagValue));
  }

  protected SimpleTimer createTimer(Id timerId) {
    return new SimpleTimer(timerId);
  }

  public void onInvocationFinish(InvocationFinishEvent event) {
    lastUpdated = registry.clock().wallTime();

    InvocationStageTrace stageTrace = event.getInvocation().getInvocationStageTrace();
    totalTimer.record((long) stageTrace.calcTotalTime());
    handlersRequestTimer.record((long) stageTrace.calcHandlersRequestTime());
    handlersResponseTimer.record((long) stageTrace.calcHandlersResponseTime());
    prepareTimer.record((long) stageTrace.calcInvocationPrepareTime());
  }

  @Override
  public void calcMeasurements(long msNow, long secondInterval) {
    List<Measurement> measurements = new ArrayList<>(3);
    calcMeasurements(measurements, msNow, secondInterval);
    allMeasurements = measurements;
  }

  @Override
  public void calcMeasurements(List<Measurement> measurements, long msNow, long secondInterval) {
    totalTimer.calcMeasurements(measurements, msNow, secondInterval);
    handlersRequestTimer.calcMeasurements(measurements, msNow, secondInterval);
    handlersResponseTimer.calcMeasurements(measurements, msNow, secondInterval);
    prepareTimer.calcMeasurements(measurements, msNow, secondInterval);
  }

  @Override
  public boolean hasExpired() {
    return super.hasExpired();
  }
}
