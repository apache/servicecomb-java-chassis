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

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.IdentifierRateLimitingPolicy;
import org.apache.servicecomb.governance.properties.IdentifierRateLimitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.micrometer.tagged.RateLimiterMetricNames;
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

public class IdentifierRateLimitingHandler extends
    AbstractGovernanceHandler<RateLimiter, IdentifierRateLimitingPolicy> {
  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierRateLimitingHandler.class);

  private final IdentifierRateLimitProperties rateLimitProperties;

  public IdentifierRateLimitingHandler(IdentifierRateLimitProperties rateLimitProperties) {
    this.rateLimitProperties = rateLimitProperties;
  }

  @Override
  protected String createKey(GovernanceRequest governanceRequest, IdentifierRateLimitingPolicy policy) {
    if (StringUtils.isEmpty(policy.getIdentifier()) ||
        StringUtils.isEmpty(governanceRequest.getHeader(policy.getIdentifier()))) {
      LOGGER.info("identifier rate limiting is not properly configured, identifier is empty.");
      return null;
    }
    return this.rateLimitProperties.getConfigKey()
        + "." + policy.getName()
        + "." + governanceRequest.getHeader(policy.getIdentifier());
  }

  @Override
  protected void onConfigurationChanged(String key) {
    if (key.startsWith(this.rateLimitProperties.getConfigKey())) {
      for (String processorKey : processors.keySet()) {
        if (processorKey.startsWith(key)) {
          Disposable<RateLimiter> processor = processors.remove(processorKey);
          if (processor != null) {
            LOGGER.info("remove identifier rate limiting processor {}", key);
            processor.dispose();
          }
        }
      }
    }
  }

  @Override
  public IdentifierRateLimitingPolicy matchPolicy(GovernanceRequest governanceRequest) {
    return matchersManager.match(governanceRequest, rateLimitProperties.getParsedEntity());
  }

  @Override
  public Disposable<RateLimiter> createProcessor(String key, GovernanceRequest governanceRequest,
      IdentifierRateLimitingPolicy policy) {
    return getRateLimiter(key, policy);
  }

  private Disposable<RateLimiter> getRateLimiter(String key, IdentifierRateLimitingPolicy policy) {
    LOGGER.info("applying new policy {} for {}", key, policy.toString());

    RateLimiterConfig config = RateLimiterConfig.custom()
        .limitForPeriod(policy.getRate())
        .limitRefreshPeriod(Duration.parse(policy.getLimitRefreshPeriod()))
        .timeoutDuration(Duration.parse(policy.getTimeoutDuration()))
        .build();
    RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);
    if (meterRegistry != null) {
      TaggedRateLimiterMetrics
          .ofRateLimiterRegistry(RateLimiterMetricNames.custom()
                  .availablePermissionsMetricName(
                      this.rateLimitProperties.getConfigKey() + ".available.permissions")
                  .waitingThreadsMetricName(this.rateLimitProperties.getConfigKey() + ".waiting.threads")
                  .build(),
              rateLimiterRegistry)
          .bindTo(meterRegistry);
    }
    return new DisposableRateLimiter(key, rateLimiterRegistry.rateLimiter(key), rateLimiterRegistry);
  }
}
