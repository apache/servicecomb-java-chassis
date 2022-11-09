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

import org.apache.servicecomb.governance.handler.BulkheadHandler;
import org.apache.servicecomb.governance.handler.CircuitBreakerHandler;
import org.apache.servicecomb.governance.handler.FaultInjectionHandler;
import org.apache.servicecomb.governance.handler.MapperHandler;
import org.apache.servicecomb.governance.handler.ext.AbstractCircuitBreakerExtension;
import org.apache.servicecomb.governance.properties.BulkheadProperties;
import org.apache.servicecomb.governance.properties.CircuitBreakerProperties;
import org.apache.servicecomb.governance.properties.FaultInjectionProperties;
import org.apache.servicecomb.governance.properties.MapperProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

@Configuration
public class MockConfiguration {
  @Bean
  public MockMicroserviceMeta mockMicroserviceMeta() {
    return new MockMicroserviceMeta();
  }

  @Bean
  public MockRetryExtension mockRetryExtension() {
    return new MockRetryExtension();
  }

  @Bean
  public MockCircuitBreakerExtension circuitBreakerExtension() {
    return new MockCircuitBreakerExtension();
  }

  @Bean
  public MockInstanceIsolationExtension instanceIsolationExtension() {
    return new MockInstanceIsolationExtension();
  }

  @Bean
  public PrometheusMeterRegistry meterRegistry() {
    return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
  }

  @Bean
  public MapperProperties mapperProperties2() {
    return new MapperProperties(MapperProperties.MATCH_MAPPER_KEY + "2");
  }

  @Bean
  public MapperHandler mapperHandler2(@Qualifier("mapperProperties2") MapperProperties mapperProperties) {
    return new MapperHandler(mapperProperties);
  }

  @Bean
  public BulkheadProperties bulkheadProperties2() {
    return new BulkheadProperties(BulkheadProperties.MATCH_BULKHEAD_KEY + "2");
  }

  @Bean
  public BulkheadHandler bulkheadHandler2(@Qualifier("bulkheadProperties2") BulkheadProperties bulkheadProperties) {
    return new BulkheadHandler(bulkheadProperties);
  }

  @Bean
  public CircuitBreakerProperties circuitBreakerProperties2() {
    return new CircuitBreakerProperties(CircuitBreakerProperties.MATCH_CIRCUITBREAKER_KEY + "2");
  }

  @Bean
  public CircuitBreakerHandler circuitBreakerHandler2(
      @Qualifier("circuitBreakerProperties2") CircuitBreakerProperties circuitBreakerProperties,
      AbstractCircuitBreakerExtension circuitBreakerExtension) {
    return new CircuitBreakerHandler(circuitBreakerProperties, circuitBreakerExtension);
  }

  @Bean
  public FaultInjectionProperties faultInjectionProperties2() {
    return new FaultInjectionProperties(FaultInjectionProperties.MATCH_FAULT_INJECTION_KEY + "2");
  }

  @Bean
  public FaultInjectionHandler faultInjectionHandler2(
      @Qualifier("faultInjectionProperties2") FaultInjectionProperties faultInjectionProperties) {
    return new FaultInjectionHandler(faultInjectionProperties);
  }
}
