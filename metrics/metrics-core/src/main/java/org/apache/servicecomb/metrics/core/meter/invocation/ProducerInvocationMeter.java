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
import org.apache.servicecomb.swagger.invocation.Response;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.Timer;

public class ProducerInvocationMeter extends AbstractInvocationMeter {
  private Timer executorQueueTimer;

  private Timer executionTimer;

  public ProducerInvocationMeter(Registry registry, Id id, Invocation invocation, Response response) {
    super(registry, id, invocation, response);

    executorQueueTimer =
        registry.timer(id.withTag(MeterInvocationConst.TAG_STAGE, MeterInvocationConst.STAGE_EXECUTOR_QUEUE));
    executionTimer =
        registry.timer(id.withTag(MeterInvocationConst.TAG_STAGE, MeterInvocationConst.STAGE_EXECUTION));
  }

  @Override
  public void onInvocationFinish(InvocationFinishEvent event) {
    super.onInvocationFinish(event);

    Invocation invocation = event.getInvocation();
    executorQueueTimer.record(invocation.getStartExecutionTime() - invocation.getStartTime(), TimeUnit.NANOSECONDS);
    executionTimer.record(event.getNanoCurrent() - invocation.getStartExecutionTime(), TimeUnit.NANOSECONDS);
  }
}
