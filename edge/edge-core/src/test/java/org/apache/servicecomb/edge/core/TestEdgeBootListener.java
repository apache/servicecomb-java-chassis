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

package org.apache.servicecomb.edge.core;

import org.apache.servicecomb.core.BootListener.BootEvent;
import org.apache.servicecomb.core.BootListener.EventType;
import org.apache.servicecomb.core.executor.ExecutorManager;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.netflix.config.DynamicPropertyFactory;

public class TestEdgeBootListener {
  EdgeBootListener listener = new EdgeBootListener();

  BootEvent bootEvent = new BootEvent();

  @Before
  public void setup() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void teardown() {
    ArchaiusUtils.resetConfig();
    System.clearProperty(ExecutorManager.KEY_EXECUTORS_DEFAULT);
  }

  @Test
  public void onBootEvent_ignore() {
    System.setProperty(ExecutorManager.KEY_EXECUTORS_DEFAULT, ExecutorManager.EXECUTOR_DEFAULT);

    bootEvent.setEventType(EventType.AFTER_CONSUMER_PROVIDER);
    listener.onBootEvent(bootEvent);

    Assert.assertEquals(ExecutorManager.EXECUTOR_DEFAULT,
        DynamicPropertyFactory.getInstance().getStringProperty(ExecutorManager.KEY_EXECUTORS_DEFAULT, null).get());
  }

  @Test
  public void onBootEvent_accept_notChange() {
    System.setProperty(ExecutorManager.KEY_EXECUTORS_DEFAULT, ExecutorManager.EXECUTOR_DEFAULT);

    bootEvent.setEventType(EventType.BEFORE_PRODUCER_PROVIDER);
    listener.onBootEvent(bootEvent);

    Assert.assertEquals(ExecutorManager.EXECUTOR_DEFAULT,
        DynamicPropertyFactory.getInstance().getStringProperty(ExecutorManager.KEY_EXECUTORS_DEFAULT, null).get());
  }

  @Test
  public void onBootEvent_change() {
    bootEvent.setEventType(EventType.BEFORE_PRODUCER_PROVIDER);
    listener.onBootEvent(bootEvent);

    Assert.assertEquals(ExecutorManager.EXECUTOR_REACTIVE,
        DynamicPropertyFactory.getInstance().getStringProperty(ExecutorManager.KEY_EXECUTORS_DEFAULT, null).get());
  }
}
