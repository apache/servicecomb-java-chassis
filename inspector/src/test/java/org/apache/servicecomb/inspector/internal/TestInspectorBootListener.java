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

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.priority.PriorityPropertyManager;
import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.core.BootListener.EventType;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestInspectorBootListener {
  static PriorityPropertyManager priorityPropertyManager;

  static InspectorConfig inspectorConfig;

  @BeforeClass
  public static void setup() {
    ConfigUtil.installDynamicConfig();
    priorityPropertyManager = new PriorityPropertyManager();
    inspectorConfig = priorityPropertyManager.createConfigObject(InspectorConfig.class);
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
  public void diabled() {
    ArchaiusUtils.setProperty("servicecomb.inspector.enabled", false);

    SCBEngine scbEngine = SCBBootstrap.createSCBEngineForTest();
    scbEngine.getTransportManager().clearTransportBeforeInit();
    scbEngine.run();
    Assert.assertNull(scbEngine.getProducerMicroserviceMeta().findSchemaMeta("inspector"));
    scbEngine.destroy();
  }

  @Test
  public void enabled() {
    ArchaiusUtils.setProperty("servicecomb.inspector.enabled", true);

    SCBEngine scbEngine = SCBBootstrap.createSCBEngineForTest();
    scbEngine.getTransportManager().clearTransportBeforeInit();
    scbEngine.run();
    Assert.assertNotNull(scbEngine.getProducerMicroserviceMeta().findSchemaMeta("inspector"));
    scbEngine.destroy();
  }
}
