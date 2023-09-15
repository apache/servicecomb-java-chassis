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

import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.DEFAULT_METRICS_WINDOW_TIME;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.METRICS_WINDOW_TIME;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.provider.producer.ProducerMeta;
import org.apache.servicecomb.core.provider.producer.ProducerProviderManager;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrap;
import org.apache.servicecomb.metrics.core.publish.MetricsRestPublisher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class TestMetricsBootListener {
  Environment environment = Mockito.mock(Environment.class);

  MetricsBootstrap metricsBootstrap = new MetricsBootstrap();

  @Before
  public void setUp() {
    Mockito.when(environment.getProperty(METRICS_WINDOW_TIME, int.class, DEFAULT_METRICS_WINDOW_TIME))
        .thenReturn(DEFAULT_METRICS_WINDOW_TIME);
    Mockito.when(environment.getProperty(
            CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN, int.class, 7))
        .thenReturn(7);
    metricsBootstrap.setEnvironment(environment);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void onBeforeProducerProvider_metrics_endpoint_enabled_by_default() {
    Mockito.when(environment.getProperty("servicecomb.metrics.endpoint.enabled", boolean.class, true))
        .thenReturn(true);
    final MetricsBootListener listener = new MetricsBootListener(metricsBootstrap);
    listener.setEnvironment(environment);
    listener.setMetricsRestPublisher(new MetricsRestPublisher());
    final List<ProducerMeta> producerMetas = new ArrayList<>();
    final BootEvent event = new BootEvent();
    final ProducerMeta producerMeta = new ProducerMeta();
    final SCBEngine scbEngine = new SCBEngine() {
      public final ProducerProviderManager producerProviderManager = new ProducerProviderManager(this) {

        @Override
        public void addProducerMeta(String schemaId, Object instance) {
          producerMeta.setSchemaId(schemaId);
          producerMeta.setInstance(instance);
          producerMetas.add(producerMeta);
        }
      };

      @Override
      public ProducerProviderManager getProducerProviderManager() {
        return producerProviderManager;
      }
    };
    event.setScbEngine(scbEngine);
    listener.onBeforeProducerProvider(event);

    MatcherAssert.assertThat(producerMetas, Matchers.contains(producerMeta));
    MatcherAssert.assertThat(producerMeta.getSchemaId(), Matchers.equalTo("metricsEndpoint"));
    MatcherAssert.assertThat(producerMeta.getInstance(), Matchers.instanceOf(MetricsRestPublisher.class));
  }

  @Test
  public void onBeforeProducerProvider_metrics_endpoint_disabled() {
    Mockito.when(environment.getProperty("servicecomb.metrics.endpoint.enabled", boolean.class, true))
        .thenReturn(false);
    final MetricsBootListener listener = new MetricsBootListener(metricsBootstrap);
    listener.setEnvironment(environment);

    final List<ProducerMeta> producerMetas = new ArrayList<>();
    final BootEvent event = new BootEvent();
    final SCBEngine scbEngine = new SCBEngine() {
      public final ProducerProviderManager producerProviderManager = new ProducerProviderManager(this) {

        @Override
        public void addProducerMeta(String schemaId, Object instance) {
          producerMetas.add(new ProducerMeta(schemaId, instance));
        }
      };

      @Override
      public ProducerProviderManager getProducerProviderManager() {
        return producerProviderManager;
      }
    };
    event.setScbEngine(scbEngine);
    listener.onBeforeProducerProvider(event);

    MatcherAssert.assertThat(producerMetas, Matchers.empty());
  }
}
