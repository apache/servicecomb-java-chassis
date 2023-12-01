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

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.meter.LatencyDistributionConfig;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

public abstract class AbstractInvocationMeter {
  //total time
  private final Timer totalTimer;

  // prepare time
  private final Timer prepareTimer;

  protected final MetricsBootstrapConfig metricsBootstrapConfig;

  public AbstractInvocationMeter(MeterRegistry meterRegistry, String name, Tags tags,
      MetricsBootstrapConfig metricsBootstrapConfig) {
    this.metricsBootstrapConfig = metricsBootstrapConfig;

    Timer.Builder totalBuilder = Timer.builder(name)
        .tags(tags.and(MeterInvocationConst.TAG_TYPE, MeterInvocationConst.TAG_STAGE,
            MeterInvocationConst.TAG_STAGE, InvocationStageTrace.STAGE_TOTAL));
    if (!StringUtils.isEmpty(metricsBootstrapConfig.getLatencyDistribution())) {
      totalBuilder.sla(toDuration(metricsBootstrapConfig.getLatencyDistribution()))
          .distributionStatisticExpiry(Duration.ofMillis(metricsBootstrapConfig.getMsPollInterval()));
    }
    this.totalTimer = totalBuilder.register(meterRegistry);
    this.prepareTimer = Timer.builder(name).tags(tags.and(MeterInvocationConst.TAG_TYPE, MeterInvocationConst.TAG_STAGE
        , MeterInvocationConst.TAG_STAGE, InvocationStageTrace.STAGE_PREPARE)).register(meterRegistry);
  }

  protected static Duration[] toDuration(String config) {
    config = config.trim() + "," + LatencyDistributionConfig.MAX_LATENCY;
    String[] array = config.split("\\s*,+\\s*");
    Duration[] result = new Duration[array.length];

    for (int idx = 0; idx < array.length - 1; idx++) {
      long msMin = Long.parseLong(array[idx]);
      long msMax = Long.parseLong(array[idx + 1]);
      if (msMin >= msMax) {
        String msg = String.format("invalid latency scope, min=%s, max=%s.", array[idx], array[idx + 1]);
        throw new IllegalStateException(msg);
      }

      result[idx] = Duration.ofMillis(msMin);
    }
    result[array.length - 1] = Duration.ofMillis(LatencyDistributionConfig.MAX_LATENCY);

    if (result[0].toMillis() == 0) {
      Duration[] target = new Duration[result.length - 1];
      System.arraycopy(result, 1, target, 0, target.length);
      return target;
    }

    return result;
  }

  public void onInvocationFinish(InvocationFinishEvent event) {
    InvocationStageTrace stageTrace = event.getInvocation().getInvocationStageTrace();
    totalTimer.record(stageTrace.calcTotal(), TimeUnit.NANOSECONDS);
    prepareTimer.record(stageTrace.calcPrepare(), TimeUnit.NANOSECONDS);
  }
}
