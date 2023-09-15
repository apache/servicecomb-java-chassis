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
package org.apache.servicecomb.foundation.metrics;

import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.DEFAULT_METRICS_WINDOW_TIME;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.METRICS_WINDOW_TIME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.servicecomb.foundation.metrics.registry.GlobalRegistry;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;

import mockit.Deencapsulation;

public class TestMetricsBootstrap {
  MetricsBootstrap bootstrap = new MetricsBootstrap();

  GlobalRegistry globalRegistry = new GlobalRegistry();

  EventBus eventBus = new EventBus();

  Environment environment = Mockito.mock(Environment.class);

  @BeforeEach
  public void setUp() {
    bootstrap.setMetricsInitializers(List.of());
    Mockito.when(environment.getProperty(METRICS_WINDOW_TIME, int.class, DEFAULT_METRICS_WINDOW_TIME))
        .thenReturn(DEFAULT_METRICS_WINDOW_TIME);
    Mockito.when(environment.getProperty(
            CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN, int.class, 7))
        .thenReturn(7);
    bootstrap.setEnvironment(environment);
  }

  @Test
  public void loadMetricsInitializers() {
    List<MetricsInitializer> initList = new ArrayList<>();
    MetricsInitializer metricsInitializer = new MetricsInitializer() {
      @Override
      public void init(GlobalRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
        initList.add(this);
      }
    };

    bootstrap.setMetricsInitializers(Arrays.asList(metricsInitializer, metricsInitializer));

    bootstrap.start(globalRegistry, eventBus);
    bootstrap.shutdown();

    MatcherAssert.assertThat(initList, Matchers.contains(metricsInitializer, metricsInitializer));
  }

  @Test
  public void shutdown() {
    ScheduledExecutorService scheduledExecutorService = Mockito.mock(ScheduledExecutorService.class);
    List<MetricsInitializer> destroyList = new ArrayList<>();
    MetricsInitializer initializer1 = new MetricsInitializer() {
      @Override
      public int getOrder() {
        return 1;
      }

      @Override
      public void init(GlobalRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
      }

      @Override
      public void destroy() {
        destroyList.add(this);
      }
    };

    MetricsInitializer initializer2 = new MetricsInitializer() {
      @Override
      public int getOrder() {
        return 2;
      }

      @Override
      public void init(GlobalRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
      }

      @Override
      public void destroy() {
        destroyList.add(this);
      }
    };
    bootstrap.setMetricsInitializers(Arrays.asList(initializer1, initializer2));
    Deencapsulation.setField(bootstrap, "executorService", scheduledExecutorService);

    bootstrap.shutdown();

    MatcherAssert.assertThat(destroyList, Matchers.contains(initializer2, initializer1));
  }

  @Test
  public void shutdown_notStart() {
    Assertions.assertNull(Deencapsulation.getField(bootstrap, "executorService"));

    // should not throw exception
    bootstrap.shutdown();
  }
}
