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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

public abstract class AbstractInvocationMeter {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInvocationMeter.class);

  // total time distribution
  private final DistributionSummary totalSummary;

  //total time
  private final Timer totalTimer;

  // prepare time
  private final Timer prepareTimer;

  protected final MetricsBootstrapConfig metricsBootstrapConfig;

  public AbstractInvocationMeter(MeterRegistry meterRegistry, String name, Tags tags,
      MetricsBootstrapConfig metricsBootstrapConfig) {
    this.metricsBootstrapConfig = metricsBootstrapConfig;

    double[] sla = toSla(metricsBootstrapConfig.getLatencyDistribution());
    if (sla != null) {
      totalSummary = DistributionSummary.builder(name)
          .tags(tags.and(MeterInvocationConst.TAG_TYPE, MeterInvocationConst.TAG_DISTRIBUTION))
          .distributionStatisticExpiry(Duration.ofMillis(metricsBootstrapConfig.getMsPollInterval()))
          .serviceLevelObjectives(sla).register(meterRegistry);
    } else {
      totalSummary = null;
    }
    this.totalTimer = Timer.builder(name).tags(tags.and(MeterInvocationConst.TAG_TYPE, MeterInvocationConst.TAG_STAGE
        , MeterInvocationConst.TAG_STAGE, InvocationStageTrace.STAGE_TOTAL)).register(meterRegistry);
    this.prepareTimer = Timer.builder(name).tags(tags.and(MeterInvocationConst.TAG_TYPE, MeterInvocationConst.TAG_STAGE
        , MeterInvocationConst.TAG_STAGE, InvocationStageTrace.STAGE_PREPARE)).register(meterRegistry);
  }

  private static double[] toSla(String config) {
    if (StringUtils.isEmpty(config)) {
      return null;
    }
    config = config.trim() + "," + LatencyDistributionConfig.MAX_LATENCY;
    String[] array = config.split("\\s*,+\\s*");
    double[] result = new double[array.length];

    for (int idx = 0; idx < array.length - 1; idx++) {
      long msMin = Long.parseLong(array[idx]);
      long msMax = Long.parseLong(array[idx + 1]);
      if (msMin >= msMax) {
        LOGGER.error("invalid latency scope, min={}, max={}.", array[idx], array[idx + 1]);
        return null;
      }

      result[idx] = msMin;
    }
    result[array.length - 1] = LatencyDistributionConfig.MAX_LATENCY;

    if (Double.compare(0, result[0]) == 0) {
      double[] target = new double[result.length - 1];
      System.arraycopy(result, 1, target, 0, target.length);
      return target;
    }

    return result;
  }

  public void onInvocationFinish(InvocationFinishEvent event) {
    InvocationStageTrace stageTrace = event.getInvocation().getInvocationStageTrace();
    totalTimer.record(stageTrace.calcTotal(), TimeUnit.NANOSECONDS);
    prepareTimer.record(stageTrace.calcPrepare(), TimeUnit.NANOSECONDS);
    if (totalSummary != null) {
      totalSummary.record(TimeUnit.NANOSECONDS.toMillis(stageTrace.calcTotal()));
    }
  }
}
