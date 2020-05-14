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
package org.apache.servicecomb.serviceregistry.task;

import java.util.EventListener;

import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.task.event.SafeModeChangeEvent;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestServiceCenterTask {
  private EventBus eventBus = new EventBus();

  @Mocked
  private MicroserviceServiceCenterTask microserviceServiceCenterTask;

  private ServiceCenterTask serviceCenterTask;

  @BeforeClass
  public static void initClass() {
    ArchaiusUtils.resetConfig();
  }

  @AfterClass
  public static void teardownClass() {
    ArchaiusUtils.resetConfig();
  }

  @Before
  public void init() {
    serviceCenterTask =
        new ServiceCenterTask(eventBus, ServiceRegistryConfig.INSTANCE.getHeartbeatInterval(), 3,
            microserviceServiceCenterTask);
  }

  @Test
  public void testLifeCycleException(@Mocked MicroserviceInstanceRegisterTask instanceEvent,
      @Mocked MicroserviceInstanceHeartbeatTask heartBeatEvent) {
    new Expectations() {
      {
        instanceEvent.getTaskStatus();
        result = TaskStatus.FINISHED;
        heartBeatEvent.getHeartbeatResult();
        result = HeartbeatResult.DISCONNECTED;
      }
    };
    serviceCenterTask.init();
    eventBus.post(instanceEvent);
    Assert.assertTrue(Deencapsulation.getField(serviceCenterTask, "registerInstanceSuccess"));

    eventBus.post(heartBeatEvent);
    Assert.assertFalse(Deencapsulation.getField(serviceCenterTask, "registerInstanceSuccess"));
  }

  @Test
  public void testLifeCycleSuccess(@Mocked MicroserviceInstanceRegisterTask instanceEvent,
      @Mocked MicroserviceInstanceHeartbeatTask heartBeatEvent) {
    new Expectations() {
      {
        instanceEvent.getTaskStatus();
        result = TaskStatus.FINISHED;
        heartBeatEvent.getHeartbeatResult();
        result = HeartbeatResult.SUCCESS;
      }
    };
    serviceCenterTask.init();
    eventBus.post(instanceEvent);
    Assert.assertTrue(Deencapsulation.getField(serviceCenterTask, "registerInstanceSuccess"));

    eventBus.post(heartBeatEvent);
    Assert.assertTrue(Deencapsulation.getField(serviceCenterTask, "registerInstanceSuccess"));
  }

  @Test
  public void testSafeMode(@Mocked MicroserviceInstanceHeartbeatTask succeededTask,
      @Mocked MicroserviceInstanceHeartbeatTask failedTask) {
    new Expectations() {
      {
        succeededTask.getHeartbeatResult();
        result = HeartbeatResult.SUCCESS;
        failedTask.getHeartbeatResult();
        result = HeartbeatResult.DISCONNECTED;
      }
    };
    Holder<Integer> count = new Holder<>(0);
    EventListener eventListener = new EventListener() {
      @Subscribe
      public void onModeChanged(SafeModeChangeEvent modeChangeEvent) {
        count.value++;
      }
    };
    eventBus.register(eventListener);
    Assert.assertEquals(0, count.value.intValue());
    eventBus.post(failedTask);
    eventBus.post(failedTask);
    eventBus.post(failedTask);
    Assert.assertEquals(0, count.value.intValue());
    Assert.assertFalse(serviceCenterTask.getSafeMode());
    eventBus.post(failedTask);
    Assert.assertEquals(1, count.value.intValue());
    Assert.assertTrue(serviceCenterTask.getSafeMode());

    eventBus.post(succeededTask);
    eventBus.post(succeededTask);
    eventBus.post(succeededTask);
    Assert.assertTrue(serviceCenterTask.getSafeMode());
    Assert.assertEquals(1, count.value.intValue());
    eventBus.post(succeededTask);
    Assert.assertFalse(serviceCenterTask.getSafeMode());
    Assert.assertEquals(2, count.value.intValue());
    eventBus.unregister(eventListener);
  }
}
