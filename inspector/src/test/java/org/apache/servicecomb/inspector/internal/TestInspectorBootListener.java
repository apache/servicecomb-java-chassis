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

import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.core.BootListener.EventType;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import io.vertx.core.file.impl.FileResolverImpl;

public class TestInspectorBootListener {
  Environment environment = Mockito.mock(Environment.class);

  @BeforeEach
  public void setUp() {
    Mockito.when(environment.getProperty("servicecomb.transport.eventloop.size", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty(FileResolverImpl.DISABLE_CP_RESOLVING_PROP_NAME, boolean.class, true))
        .thenReturn(true);
    LegacyPropertyFactory.setEnvironment(environment);
  }

  @Test
  public void getOrder() {
    Assertions.assertEquals(Short.MAX_VALUE, new InspectorBootListener(null).getOrder());
  }

  @Test
  public void filterEvent() {
    BootEvent event = new BootEvent();
    InspectorBootListener listener = new InspectorBootListener(new InspectorConfig());

    try (LogCollector logCollector = new LogCollector()) {
      for (EventType eventType : EventType.values()) {
        if (!EventType.AFTER_TRANSPORT.equals(eventType)) {
          event.setEventType(eventType);
          listener.onBootEvent(event);
        }
      }

      Assertions.assertTrue(logCollector.getEvents().isEmpty());
    }
  }

  @Test
  public void disabled() {
    SCBEngine scbEngine = SCBBootstrap.createSCBEngineForTest();
    scbEngine.setProducerMicroserviceMeta(new MicroserviceMeta(scbEngine,
        "app", "ms", false));

    InspectorConfig inspectorConfig = new InspectorConfig()
        .setEnabled(false);
    new InspectorBootListener(inspectorConfig)
        .onAfterTransport(new BootEvent(scbEngine, EventType.AFTER_TRANSPORT));

    Assertions.assertNull(scbEngine.getProducerMicroserviceMeta().findSchemaMeta("inspector"));
  }
}
