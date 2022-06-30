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

package org.apache.servicecomb.governance;

import java.util.Map;

import org.apache.servicecomb.governance.handler.BulkheadHandler;
import org.apache.servicecomb.governance.handler.CircuitBreakerHandler;
import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.handler.ext.AbstractCircuitBreakerExtension;
import org.apache.servicecomb.governance.handler.ext.AbstractInstanceIsolationExtension;
import org.apache.servicecomb.governance.handler.ext.AbstractRetryExtension;
import org.apache.servicecomb.governance.marker.RequestProcessor;
import org.apache.servicecomb.governance.marker.operator.CompareOperator;
import org.apache.servicecomb.governance.marker.operator.ContainsOperator;
import org.apache.servicecomb.governance.marker.operator.ExactOperator;
import org.apache.servicecomb.governance.marker.operator.MatchOperator;
import org.apache.servicecomb.governance.marker.operator.PrefixOperator;
import org.apache.servicecomb.governance.marker.operator.SuffixOperator;
import org.apache.servicecomb.governance.properties.BulkheadProperties;
import org.apache.servicecomb.governance.properties.CircuitBreakerProperties;
import org.apache.servicecomb.governance.properties.InstanceIsolationProperties;
import org.apache.servicecomb.governance.properties.MatchProperties;
import org.apache.servicecomb.governance.properties.RateLimitProperties;
import org.apache.servicecomb.governance.properties.RetryProperties;
import org.apache.servicecomb.governance.service.MatchersService;
import org.apache.servicecomb.governance.service.MatchersServiceImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class GovernanceConfiguration {
  // properties configuration
  @Bean
  public BulkheadProperties bulkheadProperties() {
    return new BulkheadProperties();
  }

  @Bean
  public CircuitBreakerProperties circuitBreakerProperties() {
    return new CircuitBreakerProperties();
  }

  @Bean
  public InstanceIsolationProperties instanceIsolationProperties() {
    return new InstanceIsolationProperties();
  }

  @Bean
  public MatchProperties matchProperties() {
    return new MatchProperties();
  }

  @Bean
  public RateLimitProperties rateLimitProperties() {
    return new RateLimitProperties();
  }

  @Bean
  public RetryProperties retryProperties() {
    return new RetryProperties();
  }

  // handlers configuration
  @Bean
  public BulkheadHandler bulkheadHandler(BulkheadProperties bulkheadProperties) {
    return new BulkheadHandler(bulkheadProperties);
  }

  @Bean
  public CircuitBreakerHandler circuitBreakerHandler(CircuitBreakerProperties circuitBreakerProperties,
      AbstractCircuitBreakerExtension circuitBreakerExtension,
      ObjectProvider<MeterRegistry> meterRegistry) {
    return new CircuitBreakerHandler(circuitBreakerProperties, circuitBreakerExtension, meterRegistry);
  }

  @Bean
  public InstanceIsolationHandler instanceIsolationHandler(InstanceIsolationProperties instanceIsolationProperties,
      AbstractInstanceIsolationExtension isolationExtension,
      ObjectProvider<MeterRegistry> meterRegistry) {
    return new InstanceIsolationHandler(instanceIsolationProperties, isolationExtension, meterRegistry);
  }

  @Bean
  public RateLimitingHandler rateLimitingHandler(RateLimitProperties rateLimitProperties) {
    return new RateLimitingHandler(rateLimitProperties);
  }

  @Bean
  public RetryHandler retryHandler(RetryProperties retryProperties, AbstractRetryExtension retryExtension) {
    return new RetryHandler(retryProperties, retryExtension);
  }

  // request processor
  @Bean
  public RequestProcessor requestProcessor(Map<String, MatchOperator> operatorMap) {
    return new RequestProcessor(operatorMap);
  }

  // matchers
  @Bean
  public MatchersService matchersService(RequestProcessor requestProcessor, MatchProperties matchProperties) {
    return new MatchersServiceImpl(requestProcessor, matchProperties);
  }

  @Bean
  public MatchersManager matchersManager(MatchersService matchersService) {
    return new MatchersManager(matchersService);
  }

  // operators
  @Bean
  public CompareOperator compareOperator() {
    return new CompareOperator();
  }

  @Bean
  public ContainsOperator containsOperator() {
    return new ContainsOperator();
  }

  @Bean
  public ExactOperator exactOperator() {
    return new ExactOperator();
  }

  @Bean
  public PrefixOperator prefixOperator() {
    return new PrefixOperator();
  }

  @Bean
  public SuffixOperator suffixOperator() {
    return new SuffixOperator();
  }
}
