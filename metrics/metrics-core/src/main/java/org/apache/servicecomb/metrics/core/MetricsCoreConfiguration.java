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

import java.time.Duration;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrap;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.metrics.core.publish.DefaultLogPublisher;
import org.apache.servicecomb.metrics.core.publish.SlowInvocationLogger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.CountingMode;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@Configuration
public class MetricsCoreConfiguration {
  @Bean
  public MetricsBootstrapConfig metricsBootstrapConfig(Environment environment) {
    return new MetricsBootstrapConfig(environment);
  }

  @Bean
  @ConditionalOnMissingBean
  public MeterRegistry meterRegistry(MetricsBootstrapConfig config) {
    return new SimpleMeterRegistry(s -> {
      if ("simple.step".equals(s)) {
        return Duration.ofMillis(config.getMsPollInterval()).toString();
      }
      if ("simple.mode".equals(s)) {
        return CountingMode.STEP.name();
      }
      return null;
    }, Clock.SYSTEM);
  }

  @Bean
  public MetricsBootListener metricsBootListener(MetricsBootstrap metricsBootstrap) {
    return new MetricsBootListener(metricsBootstrap);
  }

  @Bean
  public MetricsBootstrap metricsBootstrap(MetricsBootstrapConfig config) {
    return new MetricsBootstrap(config);
  }

  // Begin MetricsInitializers

  @Bean
  public DefaultLogPublisher defaultLogPublisher() {
    return new DefaultLogPublisher();
  }

  @Bean
  public InvocationMetersInitializer invocationMetersInitializer() {
    return new InvocationMetersInitializer();
  }

  @Bean
  public ThreadPoolMetersInitializer threadPoolMetersInitializer() {
    return new ThreadPoolMetersInitializer();
  }

  @Bean
  public VertxMetersInitializer vertxMetersInitializer() {
    return new VertxMetersInitializer();
  }

  @Bean
  public OsMetersInitializer osMetersInitializer() {
    return new OsMetersInitializer();
  }

  @Bean
  public SlowInvocationLogger slowInvocationLogger(SCBEngine scbEngine) {
    return new SlowInvocationLogger(scbEngine);
  }

  // End MetricsInitializers
}
