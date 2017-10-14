/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.serviceregistry.task;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

import io.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
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
        new ServiceCenterTask(eventBus, ServiceRegistryConfig.INSTANCE.getHeartbeatInterval(), microserviceServiceCenterTask);
  }

  @Test
  public void testLifeCycleException(@Mocked MicroserviceInstanceRegisterTask instanceEvent, @Mocked MicroserviceInstanceHeartbeatTask heartBeatEvent) {
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
  public void testLifeCycleSuccess(@Mocked MicroserviceInstanceRegisterTask instanceEvent, @Mocked MicroserviceInstanceHeartbeatTask heartBeatEvent) {
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
  
}
