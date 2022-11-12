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

package org.apache.servicecomb.governance.handler;

import io.github.resilience4j.micrometer.tagged.TaggedTimeLimiterMetrics;
import io.github.resilience4j.micrometer.tagged.TimeLimiterMetricNames;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.TimeLimiterPolicy;
import org.apache.servicecomb.governance.properties.TimeLimiterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class TimeLimiterHandler extends AbstractGovernanceHandler<TimeLimiter, TimeLimiterPolicy> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TimeLimiterHandler.class);

  private final TimeLimiterProperties timeLimiterProperties;

  public TimeLimiterHandler(TimeLimiterProperties timeLimiterProperties) {
    this.timeLimiterProperties = timeLimiterProperties;
  }

  @Override
  protected String createKey(GovernanceRequest governanceRequest, TimeLimiterPolicy policy) {
    return timeLimiterProperties.getConfigKey() + "." + policy.getName();
  }

  @Override
  public TimeLimiterPolicy matchPolicy(GovernanceRequest governanceRequest) {
    return matchersManager.match(governanceRequest, timeLimiterProperties.getParsedEntity());
  }

  @Override
  public Disposable<TimeLimiter> createProcessor(String key, GovernanceRequest governanceRequest,
      TimeLimiterPolicy policy) {
    return getTimeLimiter(key, policy);
  }

  private Disposable<TimeLimiter> getTimeLimiter(String key, TimeLimiterPolicy policy) {
    LOGGER.info("applying new policy {} for {}", key, policy);
    TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
        .timeoutDuration(Duration.parse(policy.getTimeoutDuration()))
        .cancelRunningFuture(policy.isCancelRunningFuture())
        .build();
    TimeLimiterRegistry timeLimiterRegistry = TimeLimiterRegistry.of(timeLimiterConfig);
    if (meterRegistry != null) {
      TaggedTimeLimiterMetrics.ofTimeLimiterRegistry(TimeLimiterMetricNames.custom()
          .callsMetricName(timeLimiterProperties.getConfigKey() + ".calls")
          .build(), timeLimiterRegistry).bindTo(meterRegistry);
    }
    return new DisposableTimeLimiter(key, timeLimiterRegistry, timeLimiterRegistry.timeLimiter(key));
  }
}
