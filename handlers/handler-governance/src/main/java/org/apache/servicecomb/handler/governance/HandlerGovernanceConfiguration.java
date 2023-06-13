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
package org.apache.servicecomb.handler.governance;

import org.apache.servicecomb.governance.handler.BulkheadHandler;
import org.apache.servicecomb.governance.handler.CircuitBreakerHandler;
import org.apache.servicecomb.governance.handler.InstanceBulkheadHandler;
import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = HandlerGovernanceConfiguration.GOVERNANCE_ENABLED,
    havingValue = "true", matchIfMissing = true)
public class HandlerGovernanceConfiguration {
  public static final String GOVERNANCE_PREFIX = "servicecomb.governance";

  public static final String GOVERNANCE_ENABLED = GOVERNANCE_PREFIX + ".enabled";

  @Bean
  public ConsumerInstanceBulkheadFilter consumerInstanceBulkheadFilter(
      InstanceBulkheadHandler instanceBulkheadHandler) {
    return new ConsumerInstanceBulkheadFilter(instanceBulkheadHandler);
  }

  @Bean
  public ConsumerInstanceIsolationFilter consumerInstanceIsolationFilter(
      InstanceIsolationHandler instanceIsolationHandler) {
    return new ConsumerInstanceIsolationFilter(instanceIsolationHandler);
  }

  @Bean
  public ProviderBulkheadFilter providerBulkheadFilter(BulkheadHandler bulkheadHandler) {
    return new ProviderBulkheadFilter(bulkheadHandler);
  }

  @Bean
  public ProviderCircuitBreakerFilter providerCircuitBreakerFilter(CircuitBreakerHandler circuitBreakerHandler) {
    return new ProviderCircuitBreakerFilter(circuitBreakerHandler);
  }

  @Bean
  public ProviderRateLimitingFilter providerRateLimitingFilter(RateLimitingHandler rateLimitingHandler) {
    return new ProviderRateLimitingFilter(rateLimitingHandler);
  }
}
