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

import org.apache.servicecomb.foundation.metrics.MetricsBootstrap;
import org.apache.servicecomb.metrics.core.publish.DefaultLogPublisher;
import org.apache.servicecomb.metrics.core.publish.MetricsRestPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsCoreConfiguration {
  @Bean
  public HealthBootListener healthBootListener() {
    return new HealthBootListener();
  }

  @Bean
  public MetricsBootListener metricsBootListener(MetricsBootstrap metricsBootstrap) {
    return new MetricsBootListener(metricsBootstrap);
  }

  @Bean
  public MetricsBootstrap metricsBootstrap() {
    return new MetricsBootstrap();
  }

  // Begin MetricsInitializers

  @Bean
  public DefaultLogPublisher defaultLogPublisher() {
    return new DefaultLogPublisher();
  }

  @Bean
  public DefaultRegistryInitializer defaultRegistryInitializer() {
    return new DefaultRegistryInitializer();
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
  public MetricsRestPublisher metricsRestPublisher() {
    return new MetricsRestPublisher();
  }

  // End MetricsInitializers
}
