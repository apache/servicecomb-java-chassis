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
package org.apache.servicecomb.inspector.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.config.inject.ConfigObjectFactory;
import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.core.BootListener.EventType;
import org.apache.servicecomb.core.definition.schema.ProducerSchemaFactory;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.inspector.internal.InspectorBootListener;
import org.apache.servicecomb.inspector.internal.InspectorConfig;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestInspectorBootListener {
  static Map<String, String> schemas = new HashMap<>();

  static InspectorConfig inspectorConfig;

  @BeforeClass
  public static void setup() {
    ArchaiusUtils.resetConfig();
    inspectorConfig = new ConfigObjectFactory().create(InspectorConfig.class);
  }

  @AfterClass
  public static void teardown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void getOrder() {
    Assert.assertEquals(Short.MAX_VALUE, new InspectorBootListener().getOrder());
  }

  @Test
  public void filterEvent() {
    BootEvent event = new BootEvent();
    InspectorBootListener listener = new InspectorBootListener();

    LogCollector logCollector = new LogCollector();
    for (EventType eventType : EventType.values()) {
      if (!EventType.AFTER_TRANSPORT.equals(eventType)) {
        event.setEventType(eventType);
        listener.onBootEvent(event);
      }
    }
    logCollector.teardown();

    Assert.assertTrue(logCollector.getEvents().isEmpty());
  }

  @Test
  public void diabled() {
    ArchaiusUtils.setProperty("servicecomb.inspector.enabled", false);
    BootEvent event = new BootEvent();
    event.setEventType(EventType.AFTER_TRANSPORT);

    LogCollector logCollector = new LogCollector();
    new InspectorBootListener().onBootEvent(event);
    logCollector.teardown();

    Assert.assertEquals("inspector is not enabled.", logCollector.getLastEvents().getMessage());
  }

  @Test
  public void enabled(@Mocked ProducerSchemaFactory producerSchemaFactory, @Mocked ServiceRegistry serviceRegistry) {
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getServiceRegistry();
        result = serviceRegistry;
      }
    };

    ArchaiusUtils.setProperty("servicecomb.inspector.enabled", true);
    BootEvent event = new BootEvent();
    event.setEventType(EventType.AFTER_TRANSPORT);

    LogCollector logCollector = new LogCollector();
    InspectorBootListener listener = new InspectorBootListener();
    Deencapsulation.setField(listener, "producerSchemaFactory", producerSchemaFactory);
    listener.onBootEvent(event);
    logCollector.teardown();

    Assert.assertEquals("inspector is enabled.", logCollector.getLastEvents().getMessage());
  }
}
