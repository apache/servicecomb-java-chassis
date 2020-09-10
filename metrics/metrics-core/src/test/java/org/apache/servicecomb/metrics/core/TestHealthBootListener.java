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
import org.apache.servicecomb.core.BootListener.EventType;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.definition.schema.ProducerSchemaFactory;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.metrics.core.publish.HealthCheckerRestPublisher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mockit.Mock;
import mockit.MockUp;

public class TestHealthBootListener {
  HealthBootListener listener = new HealthBootListener();

  @Before
  public void setUp() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void onBootEvent_on_BEFORE_PRODUCER_PROVIDER_event_health_endpoint_enabled_by_default() {
    List<Object[]> argsList = new ArrayList<>();

    listener.producerSchemaFactory = new MockUp<ProducerSchemaFactory>() {
      @Mock
      SchemaMeta getOrCreateProducerSchema(String schemaId,
          Class<?> producerClass,
          Object producerInstance) {
        argsList.add(new Object[] {schemaId, producerClass, producerInstance});
        return null;
      }
    }.getMockInstance();

    BootEvent event = new BootEvent();
    event.setEventType(EventType.BEFORE_PRODUCER_PROVIDER);
    listener.onBootEvent(event);

    Assert.assertEquals(1, argsList.size());
    Object[] args = argsList.get(0);
    //we have remove parameter microserviceName
    Assert.assertEquals("healthEndpoint", args[0]);
    Assert.assertEquals(HealthCheckerRestPublisher.class, args[1]);
    Assert.assertEquals(HealthCheckerRestPublisher.class, args[2].getClass());
  }

  @Test
  public void onBootEvent_on_BEFORE_PRODUCER_PROVIDER_event_health_endpoint_disabled() {
    ArchaiusUtils.setProperty("servicecomb.health.endpoint.enabled", false);
    List<Object[]> argsList = new ArrayList<>();

    listener.producerSchemaFactory = new MockUp<ProducerSchemaFactory>() {
      @Mock
      SchemaMeta getOrCreateProducerSchema(String schemaId,
          Class<?> producerClass,
          Object producerInstance) {
        argsList.add(new Object[] {schemaId, producerClass, producerInstance});
        return null;
      }
    }.getMockInstance();

    BootEvent event = new BootEvent();
    event.setEventType(EventType.BEFORE_PRODUCER_PROVIDER);
    listener.onBootEvent(event);

    Assert.assertThat(argsList, Matchers.empty());
  }
}