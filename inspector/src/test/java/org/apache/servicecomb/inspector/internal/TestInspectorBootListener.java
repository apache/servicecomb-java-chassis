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
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.provider.producer.ProducerProviderManager;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.junit.Assert;
import org.junit.Test;

public class TestInspectorBootListener {
  @Test
  public void getOrder() {
    Assert.assertEquals(Short.MAX_VALUE, new InspectorBootListener(null, null).getOrder());
  }

  @Test
  public void filterEvent() {
    BootEvent event = new BootEvent();
    InspectorBootListener listener = new InspectorBootListener(new InspectorConfig(), null);

    try (LogCollector logCollector = new LogCollector()) {
      for (EventType eventType : EventType.values()) {
        if (!EventType.AFTER_TRANSPORT.equals(eventType)) {
          event.setEventType(eventType);
          listener.onBootEvent(event);
        }
      }

      Assert.assertTrue(logCollector.getEvents().isEmpty());
    }
  }

  @Test
  public void disabled() {
    SCBEngine scbEngine = SCBBootstrap.createSCBEngineForTest();
    scbEngine.setProducerMicroserviceMeta(new MicroserviceMeta(scbEngine, "ms", false));

    InspectorConfig inspectorConfig = new InspectorConfig()
        .setEnabled(false);
    new InspectorBootListener(inspectorConfig, null)
        .onAfterTransport(new BootEvent(scbEngine, EventType.AFTER_TRANSPORT));

    Assert.assertNull(scbEngine.getProducerMicroserviceMeta().findSchemaMeta("inspector"));
  }

  @Test
  public void enabled() {
    Holder<Object> holder = new Holder<>();

    SCBEngine scbEngine = SCBBootstrap.createSCBEngineForTest();
    scbEngine.setProducerMicroserviceMeta(new MicroserviceMeta(scbEngine, "ms", false));
    scbEngine.setProducerProviderManager(new ProducerProviderManager(scbEngine) {
      @Override
      public SchemaMeta registerSchema(String schemaId, Object instance) {
        if ("inspector".equals(schemaId)) {
          holder.value = instance;
        }
        return null;
      }
    });

    InspectorConfig inspectorConfig = new InspectorConfig()
        .setEnabled(true);
    new InspectorBootListener(inspectorConfig, null)
        .onAfterTransport(new BootEvent(scbEngine, EventType.AFTER_TRANSPORT));

    Assert.assertNotNull(holder.value);
  }
}
