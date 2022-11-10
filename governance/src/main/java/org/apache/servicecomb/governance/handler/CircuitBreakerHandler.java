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

import org.apache.servicecomb.governance.handler.ext.AbstractCircuitBreakerExtension;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.CircuitBreakerPolicy;
import org.apache.servicecomb.governance.properties.CircuitBreakerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.CircuitBreakerMetricNames;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;

public class CircuitBreakerHandler extends AbstractGovernanceHandler<CircuitBreaker, CircuitBreakerPolicy> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CircuitBreakerHandler.class);

  private final CircuitBreakerProperties circuitBreakerProperties;

  private final AbstractCircuitBreakerExtension circuitBreakerExtension;

  public CircuitBreakerHandler(CircuitBreakerProperties circuitBreakerProperties,
      AbstractCircuitBreakerExtension circuitBreakerExtension) {
    this.circuitBreakerProperties = circuitBreakerProperties;
    this.circuitBreakerExtension = circuitBreakerExtension;
  }

  @Override
  protected String createKey(GovernanceRequest governanceRequest, CircuitBreakerPolicy policy) {
    return this.circuitBreakerProperties.getConfigKey() + "." + policy.getName();
  }

  @Override
  public CircuitBreakerPolicy matchPolicy(GovernanceRequest governanceRequest) {
    return matchersManager.match(governanceRequest, circuitBreakerProperties.getParsedEntity());
  }

  @Override
  public Disposable<CircuitBreaker> createProcessor(String key, GovernanceRequest governanceRequest,
      CircuitBreakerPolicy policy) {
    return getCircuitBreaker(key, policy);
  }

  private Disposable<CircuitBreaker> getCircuitBreaker(String key, CircuitBreakerPolicy policy) {
    LOGGER.info("applying new policy {} for {}", key, policy);

    CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
        .failureRateThreshold(policy.getFailureRateThreshold())
        .slowCallRateThreshold(policy.getSlowCallRateThreshold())
        .waitDurationInOpenState(Duration.parse(policy.getWaitDurationInOpenState()))
        .slowCallDurationThreshold(Duration.parse(policy.getSlowCallDurationThreshold()))
        .permittedNumberOfCallsInHalfOpenState(policy.getPermittedNumberOfCallsInHalfOpenState())
        .minimumNumberOfCalls(policy.getMinimumNumberOfCalls())
        .slidingWindowType(policy.getSlidingWindowTypeEnum())
        .slidingWindowSize(Integer.parseInt(policy.getSlidingWindowSize()))
        .recordException(circuitBreakerExtension::isFailedResult)
        .recordResult(r -> circuitBreakerExtension.isFailedResult(policy.getRecordFailureStatus(), r))
        .build();
    CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
    if (meterRegistry != null) {
      TaggedCircuitBreakerMetrics
          .ofCircuitBreakerRegistry(CircuitBreakerMetricNames.custom()
              .callsMetricName(this.circuitBreakerProperties.getConfigKey() + ".calls")
              .notPermittedCallsMetricName(this.circuitBreakerProperties.getConfigKey() + ".not.permitted.calls")
              .stateMetricName(this.circuitBreakerProperties.getConfigKey() + ".state")
              .bufferedCallsMetricName(this.circuitBreakerProperties.getConfigKey() + ".buffered.calls")
              .slowCallsMetricName(this.circuitBreakerProperties.getConfigKey() + ".slow.calls")
              .failureRateMetricName(this.circuitBreakerProperties.getConfigKey() + ".failure.rate")
              .slowCallRateMetricName(this.circuitBreakerProperties.getConfigKey() + ".slow.call.rate")
              .build(), circuitBreakerRegistry)
          .bindTo(meterRegistry);
    }
    return new DisposableCircuitBreaker(key, circuitBreakerRegistry,
        circuitBreakerRegistry.circuitBreaker(key, circuitBreakerConfig));
  }
}
