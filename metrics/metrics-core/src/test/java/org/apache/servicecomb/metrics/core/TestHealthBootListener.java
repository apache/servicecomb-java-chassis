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

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.provider.producer.ProducerMeta;
import org.apache.servicecomb.core.provider.producer.ProducerProviderManager;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.metrics.core.publish.HealthCheckerRestPublisher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestHealthBootListener {

  @Before
  public void setUp() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void onBeforeProducerProvider_health_endpoint_enabled_by_default() {
    final HealthBootListener listener = new HealthBootListener();

    final List<ProducerMeta> producerMetas = new ArrayList<>();
    final BootEvent event = new BootEvent();
    final ProducerMeta producerMeta = new ProducerMeta();
    final SCBEngine scbEngine = new SCBEngine() {
      final public ProducerProviderManager producerProviderManager = new ProducerProviderManager(this) {

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

    Assert.assertThat(producerMetas, Matchers.contains(producerMeta));
    Assert.assertThat(producerMeta.getSchemaId(), Matchers.equalTo("healthEndpoint"));
    Assert.assertThat(producerMeta.getInstance(), Matchers.instanceOf(HealthCheckerRestPublisher.class));
  }

  @Test
  public void onBeforeProducerProvider_health_endpoint_disabled() {
    ArchaiusUtils.setProperty("servicecomb.health.endpoint.enabled", false);
    final HealthBootListener listener = new HealthBootListener();

    final List<ProducerMeta> producerMetas = new ArrayList<>();
    final BootEvent event = new BootEvent();
    final SCBEngine scbEngine = new SCBEngine() {
      final public ProducerProviderManager producerProviderManager = new ProducerProviderManager(this) {

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

    Assert.assertThat(producerMetas, Matchers.empty());
  }
}