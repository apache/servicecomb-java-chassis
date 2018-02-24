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

package org.apache.servicecomb.metrics.core.event;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.servicecomb.core.metrics.InvocationFinishedEvent;
import org.apache.servicecomb.foundation.common.event.EventListener;
import org.apache.servicecomb.foundation.metrics.MetricsConst;
import org.apache.servicecomb.metrics.core.MonitorManager;
import org.apache.servicecomb.swagger.invocation.InvocationType;

public class InvocationFinishedEventListener implements EventListener<InvocationFinishedEvent> {
  @Override
  public Class<InvocationFinishedEvent> getEventClass() {
    return InvocationFinishedEvent.class;
  }

  @Override
  public void process(InvocationFinishedEvent data) {
    String[] tags = new String[] {MetricsConst.TAG_OPERATION, data.getOperationName(),
        MetricsConst.TAG_ROLE, String.valueOf(data.getInvocationType()).toLowerCase(),
        MetricsConst.TAG_STATUS, String.valueOf(data.getStatusCode())};
    this.updateLatency(MetricsConst.STAGE_TOTAL, data.getTotalElapsedNanoTime(), tags);
    this.updateCount(tags);
    if (InvocationType.PRODUCER.equals(data.getInvocationType())) {
      this.updateLatency(MetricsConst.STAGE_QUEUE, data.getInQueueNanoTime(), tags);
      this.updateLatency(MetricsConst.STAGE_EXECUTION, data.getExecutionElapsedNanoTime(), tags);
    }
  }

  private void updateLatency(String stage, long value, String... basicTags) {
    String[] tags = ArrayUtils
        .addAll(basicTags, MetricsConst.TAG_STAGE, stage, MetricsConst.TAG_UNIT, String.valueOf(TimeUnit.MILLISECONDS));
    MonitorManager.getInstance()
        .getTimer(MetricsConst.SERVICECOMB_INVOCATION, ArrayUtils.addAll(tags, MetricsConst.TAG_STATISTIC, "latency"))
        .record(value, TimeUnit.NANOSECONDS);
    MonitorManager.getInstance()
        .getMaxGauge(MetricsConst.SERVICECOMB_INVOCATION, ArrayUtils.addAll(tags, MetricsConst.TAG_STATISTIC, "max"))
        .update(TimeUnit.NANOSECONDS.toMillis(value));
  }

  private void updateCount(String... basicTags) {
    String[] tags = ArrayUtils.addAll(basicTags, MetricsConst.TAG_STAGE, MetricsConst.STAGE_TOTAL);
    MonitorManager.getInstance().getStepCounter(MetricsConst.SERVICECOMB_INVOCATION,
        ArrayUtils.addAll(tags, MetricsConst.TAG_STATISTIC, "tps")).increment();
    MonitorManager.getInstance().getCounter(MetricsConst.SERVICECOMB_INVOCATION,
        ArrayUtils.addAll(tags, MetricsConst.TAG_STATISTIC, "count")).increment();
  }
}
