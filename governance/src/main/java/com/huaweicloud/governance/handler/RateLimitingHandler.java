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
package com.huaweicloud.governance.handler;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.huaweicloud.governance.policy.Policy;
import com.huaweicloud.governance.policy.RateLimitingPolicy;

import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

@Component("RateLimitingHandler")
public class RateLimitingHandler extends AbstractGovHandler<RateLimiter> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingHandler.class);

  @Override
  public <RESULT> DecorateCheckedSupplier<RESULT> process(DecorateCheckedSupplier<RESULT> supplier, Policy policy) {
    RateLimiter rateLimiter = getActuator("servicecomb.rateLimiting." + policy.name(), (RateLimitingPolicy) policy,
        this::getRateLimiter);
    return supplier.withRateLimiter(rateLimiter);
  }

  @Override
  public HandlerType type() {
    return HandlerType.SERVER;
  }

  /**
   * @param policy
   * @return
   */
  private RateLimiter getRateLimiter(RateLimitingPolicy policy) {
    LOGGER.info("applying new policy: {}", policy.toString());

    RateLimiterConfig config;
    config = RateLimiterConfig.custom()
        .limitForPeriod(policy.getRate())
        .limitRefreshPeriod(Duration.ofMillis(policy.getLimitRefreshPeriod()))
        .timeoutDuration(Duration.ofMillis(policy.getTimeoutDuration()))
        .build();
    RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);
    return rateLimiterRegistry.rateLimiter(policy.name());
  }
}
