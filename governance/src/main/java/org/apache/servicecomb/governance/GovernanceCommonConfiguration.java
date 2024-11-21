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
import org.apache.servicecomb.governance.handler.FaultInjectionHandler;
import org.apache.servicecomb.governance.handler.GovernanceCacheHandler;
import org.apache.servicecomb.governance.handler.IdentifierRateLimitingHandler;
import org.apache.servicecomb.governance.handler.InstanceBulkheadHandler;
import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.apache.servicecomb.governance.handler.LoadBalanceHandler;
import org.apache.servicecomb.governance.handler.MapperHandler;
import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.handler.TimeLimiterHandler;
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
import org.apache.servicecomb.governance.properties.FaultInjectionProperties;
import org.apache.servicecomb.governance.properties.GovernanceCacheProperties;
import org.apache.servicecomb.governance.properties.IdentifierRateLimitProperties;
import org.apache.servicecomb.governance.properties.InstanceBulkheadProperties;
import org.apache.servicecomb.governance.properties.InstanceIsolationProperties;
import org.apache.servicecomb.governance.properties.LoadBalanceProperties;
import org.apache.servicecomb.governance.properties.MapperProperties;
import org.apache.servicecomb.governance.properties.MatchProperties;
import org.apache.servicecomb.governance.properties.RateLimitProperties;
import org.apache.servicecomb.governance.properties.RetryProperties;
import org.apache.servicecomb.governance.properties.TimeLimiterProperties;
import org.apache.servicecomb.governance.service.MatchersService;
import org.apache.servicecomb.governance.service.MatchersServiceImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class GovernanceCommonConfiguration {
  // properties configuration
  @Bean
  public BulkheadProperties scbBulkheadProperties() {
    return new BulkheadProperties();
  }

  @Bean
  public InstanceBulkheadProperties scbInstanceBulkheadProperties() {
    return new InstanceBulkheadProperties();
  }

  @Bean
  public CircuitBreakerProperties scbCircuitBreakerProperties() {
    return new CircuitBreakerProperties();
  }

  @Bean
  public InstanceIsolationProperties scbInstanceIsolationProperties() {
    return new InstanceIsolationProperties();
  }

  @Bean
  public MatchProperties scbMatchProperties() {
    return new MatchProperties();
  }

  @Bean
  public RateLimitProperties scbRateLimitProperties() {
    return new RateLimitProperties();
  }

  @Bean
  public IdentifierRateLimitProperties scbIdentifierRateLimitProperties() {
    return new IdentifierRateLimitProperties();
  }

  @Bean
  public RetryProperties scbRetryProperties() {
    return new RetryProperties();
  }

  @Bean
  public TimeLimiterProperties scbTimeLimiterProperties() {
    return new TimeLimiterProperties();
  }

  @Bean
  public GovernanceCacheProperties scbCacheProperties() {
    return new GovernanceCacheProperties();
  }

  @Bean
  public FaultInjectionProperties scbFaultInjectionProperties() {
    return new FaultInjectionProperties();
  }

  @Bean
  public LoadBalanceProperties scbLoadBalanceProperties() {
    return new LoadBalanceProperties();
  }

  @Bean
  public MapperProperties scbMapperProperties() {
    return new MapperProperties();
  }

  // handlers configuration
  @Bean
  public BulkheadHandler scbBulkheadHandler(BulkheadProperties bulkheadProperties) {
    return new BulkheadHandler(bulkheadProperties);
  }

  @Bean
  public InstanceBulkheadHandler scbInstanceBulkheadHandler(InstanceBulkheadProperties instanceBulkheadProperties) {
    return new InstanceBulkheadHandler(instanceBulkheadProperties);
  }

  @Bean
  public LoadBalanceHandler scbLoadBalanceHandler(LoadBalanceProperties loadBalanceProperties) {
    return new LoadBalanceHandler(loadBalanceProperties);
  }

  @Bean
  public CircuitBreakerHandler scbCircuitBreakerHandler(CircuitBreakerProperties circuitBreakerProperties,
      AbstractCircuitBreakerExtension circuitBreakerExtension) {
    return new CircuitBreakerHandler(circuitBreakerProperties, circuitBreakerExtension);
  }

  @Bean
  public InstanceIsolationHandler scbInstanceIsolationHandler(InstanceIsolationProperties instanceIsolationProperties,
      AbstractInstanceIsolationExtension isolationExtension,
      ObjectProvider<MeterRegistry> meterRegistry) {
    return new InstanceIsolationHandler(instanceIsolationProperties, isolationExtension, meterRegistry);
  }

  @Bean
  public RateLimitingHandler scbRateLimitingHandler(RateLimitProperties rateLimitProperties) {
    return new RateLimitingHandler(rateLimitProperties);
  }

  @Bean
  public IdentifierRateLimitingHandler scbIdentifierRateLimitingHandler(
      IdentifierRateLimitProperties identifierRateLimitProperties) {
    return new IdentifierRateLimitingHandler(identifierRateLimitProperties);
  }

  @Bean
  public RetryHandler scbRetryHandler(RetryProperties retryProperties, AbstractRetryExtension retryExtension) {
    return new RetryHandler(retryProperties, retryExtension);
  }

  @Bean
  public TimeLimiterHandler scbTimeLimiterHandler(TimeLimiterProperties timeLimiterProperties) {
    return new TimeLimiterHandler(timeLimiterProperties);
  }

  @Bean
  public GovernanceCacheHandler<String, Object> scbGovernanceCacheHandler(GovernanceCacheProperties cacheProperties) {
    return new GovernanceCacheHandler<String, Object>(cacheProperties);
  }

  @Bean
  public FaultInjectionHandler scbFaultInjectionHandler(FaultInjectionProperties faultInjectionProperties) {
    return new FaultInjectionHandler(faultInjectionProperties);
  }

  @Bean
  public MapperHandler scbMapperHandler(MapperProperties mapperProperties) {
    return new MapperHandler(mapperProperties);
  }

  // request processor
  @Bean
  public RequestProcessor scbRequestProcessor(Map<String, MatchOperator> operatorMap) {
    return new RequestProcessor(operatorMap);
  }

  // matchers
  @Bean
  public MatchersService scbMatchersService(RequestProcessor requestProcessor, MatchProperties matchProperties) {
    return new MatchersServiceImpl(requestProcessor, matchProperties);
  }

  @Bean
  public MatchersManager scbMatchersManager(MatchersService matchersService) {
    return new MatchersManager(matchersService);
  }

  // operators
  @Bean
  public CompareOperator scbCompareOperator() {
    return new CompareOperator();
  }

  @Bean
  public ContainsOperator scbContainsOperator() {
    return new ContainsOperator();
  }

  @Bean
  public ExactOperator scbExactOperator() {
    return new ExactOperator();
  }

  @Bean
  public PrefixOperator scbPrefixOperator() {
    return new PrefixOperator();
  }

  @Bean
  public SuffixOperator scbSuffixOperator() {
    return new SuffixOperator();
  }
}
