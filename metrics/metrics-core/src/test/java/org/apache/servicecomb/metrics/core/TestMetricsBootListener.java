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
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.metrics.core.publish.HealthCheckerRestPublisher;
import org.apache.servicecomb.metrics.core.publish.MetricsRestPublisher;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.junit.Assert;
import org.junit.Test;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestMetricsBootListener {
  MetricsBootListener listener = new MetricsBootListener();

  @Test
  public void registerSchemas() {
    List<Object[]> argsList = new ArrayList<>();

    ProducerSchemaFactory producerSchemaFactory = new MockUp<ProducerSchemaFactory>() {
      @Mock
      SchemaMeta getOrCreateProducerSchema(String schemaId,
          Class<?> producerClass,
          Object producerInstance) {
        argsList.add(new Object[] {schemaId, producerClass, producerInstance});
        return null;
      }
    }.getMockInstance();
    Deencapsulation.setField(listener, "producerSchemaFactory", producerSchemaFactory);

    Microservice microservice = new Microservice();
    microservice.setServiceName("name");

    BootEvent event = new BootEvent();
    event.setEventType(EventType.BEFORE_PRODUCER_PROVIDER);
    listener.onBootEvent(event);

    Object[] args = argsList.get(0);
    //we have remove parameter microserviceName
    Assert.assertEquals("healthEndpoint", args[0]);
    Assert.assertEquals(HealthCheckerRestPublisher.class, args[1]);
    Assert.assertEquals(HealthCheckerRestPublisher.class, args[2].getClass());

    MetricsRestPublisher metricsRestPublisher =
        SPIServiceUtils.getTargetService(MetricsInitializer.class, MetricsRestPublisher.class);
    args = argsList.get(1);
    //we have remove parameter microserviceName
    Assert.assertEquals("metricsEndpoint", args[0]);
    Assert.assertEquals(MetricsRestPublisher.class, args[1]);
    Assert.assertSame(metricsRestPublisher, args[2]);
  }
}
