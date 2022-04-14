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

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.CircuitBreakerPolicy;
import org.apache.servicecomb.governance.properties.CircuitBreakerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@Component
public class CircuitBreakerHandler extends AbstractGovernanceHandler<CircuitBreaker, CircuitBreakerPolicy> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CircuitBreakerHandler.class);

  private CircuitBreakerProperties circuitBreakerProperties;

  @Autowired
  public CircuitBreakerHandler(CircuitBreakerProperties circuitBreakerProperties) {
    this.circuitBreakerProperties = circuitBreakerProperties;
  }

  @Override
  protected String createKey(GovernanceRequest governanceRequest, CircuitBreakerPolicy policy) {
    return CircuitBreakerProperties.MATCH_CIRCUITBREAKER_KEY + "." + policy.getName();
  }

  @Override
  public CircuitBreakerPolicy matchPolicy(GovernanceRequest governanceRequest) {
    return matchersManager.match(governanceRequest, circuitBreakerProperties.getParsedEntity());
  }

  @Override
  protected CircuitBreaker createProcessor(GovernanceRequest governanceRequest, CircuitBreakerPolicy policy) {
    return getCircuitBreaker(policy);
  }

  private CircuitBreaker getCircuitBreaker(CircuitBreakerPolicy policy) {
    LOGGER.info("applying new policy: {}", policy.toString());

    CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
        .failureRateThreshold(policy.getFailureRateThreshold())
        .slowCallRateThreshold(policy.getSlowCallRateThreshold())
        .waitDurationInOpenState(Duration.parse(policy.getWaitDurationInOpenState()))
        .slowCallDurationThreshold(Duration.parse(policy.getSlowCallDurationThreshold()))
        .permittedNumberOfCallsInHalfOpenState(policy.getPermittedNumberOfCallsInHalfOpenState())
        .minimumNumberOfCalls(policy.getMinimumNumberOfCalls())
        .slidingWindowType(policy.getSlidingWindowTypeEnum())
        .slidingWindowSize(Integer.valueOf(policy.getSlidingWindowSize()))
        .build();
    CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
    return circuitBreakerRegistry.circuitBreaker(policy.getName(), circuitBreakerConfig);
  }
}
