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
package org.apache.servicecomb.solution.basic.integration;

import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.DEFAULT_METRICS_WINDOW_TIME;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.METRICS_WINDOW_TIME;

import java.util.Map;

import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestMetricsEndpointImpl {
  MetricsEndpointImpl publisher = new MetricsEndpointImpl();

  EventBus eventBus = new EventBus();

  Environment environment = Mockito.mock(Environment.class);

  @BeforeEach
  public void setUp() {
    Mockito.when(environment.getProperty(METRICS_WINDOW_TIME, int.class, DEFAULT_METRICS_WINDOW_TIME))
        .thenReturn(DEFAULT_METRICS_WINDOW_TIME);
    Mockito.when(environment.getProperty(
            CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN, int.class, 7))
        .thenReturn(7);
  }

  @Test
  public void measure_globalRegistryNull() {
    MeterRegistry registry = new SimpleMeterRegistry();
    publisher.init(registry, eventBus, new MetricsBootstrapConfig(environment));

    Map<String, Double> result = publisher.measure();

    Assertions.assertEquals(0, result.size());
  }

  @Test
  public void measure_normal() {
    MeterRegistry registry = new SimpleMeterRegistry();
    registry.timer("name", "t1", "v1", "t2", "v2");

    publisher.init(registry, eventBus, new MetricsBootstrapConfig(environment));
    Map<String, Double> result = publisher.measure();

    Assertions.assertEquals(3, result.size());
    Assertions.assertEquals(0, result.get("name(statistic=COUNT,t1=v1,t2=v2)"), 0);
    Assertions.assertEquals(0, result.get("name(statistic=TOTAL_TIME,t1=v1,t2=v2)"), 0);
    Assertions.assertEquals(0, result.get("name(statistic=MAX,t1=v1,t2=v2)"), 0);
  }
}
