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
package org.apache.servicecomb.metrics.core;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.cumulative.CumulativeDistributionSummary;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.HistogramGauges;
import io.micrometer.core.instrument.distribution.StepBucketHistogram;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Custom SimpleMeterRegistry to support StepDistributionSummary
 */
public class SimpleMeterRegistryExt extends SimpleMeterRegistry {
  private final SimpleConfig config;

  public SimpleMeterRegistryExt(SimpleConfig config, Clock clock) {
    super(config, clock);
    this.config = config;
  }

  @Override
  protected DistributionSummary newDistributionSummary(Meter.Id id,
      DistributionStatisticConfig distributionStatisticConfig, double scale) {
    DistributionStatisticConfig merged = distributionStatisticConfig
        .merge(DistributionStatisticConfig.builder().expiry(config.step()).build());

    DistributionSummary summary;
    switch (config.mode()) {
      case CUMULATIVE:
        summary = new CumulativeDistributionSummary(id, clock, merged, scale, false);
        break;
      case STEP:
      default:
        summary = new StepDistributionSummaryExt(id, clock, merged, scale, config.step().toMillis(),
            new StepBucketHistogram(clock, config.step().toMillis(), distributionStatisticConfig,
                false, false));
        break;
    }

    HistogramGauges.registerWithCommonFormat(summary, this);

    return summary;
  }
}
