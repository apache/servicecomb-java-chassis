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

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import mockit.Expectations;
import mockit.Mocked;

public class TestMicroserviceInstanceHeartbeatTask {
  private EventBus eventBus;

  private Microservice microservice;

  private List<MicroserviceInstanceHeartbeatTask> heartbeatTaskList;

  @Before
  public void setup() {
    eventBus = new EventBus();

    heartbeatTaskList = new ArrayList<>();
    eventBus.register(new Object() {
      @Subscribe
      public void onEvent(MicroserviceInstanceHeartbeatTask task) {
        heartbeatTaskList.add(task);
      }
    });

    microservice = new Microservice();
    microservice.setAppId("app");
    microservice.setServiceName("ms");
    microservice.setServiceId("serviceId");

    microservice.setInstance(new MicroserviceInstance());
    microservice.getInstance().setInstanceId("instanceId");
  }

  @Test
  public void testNotRegistered(@Mocked ServiceRegistryClient srClient,
      @Mocked MicroserviceInstanceRegisterTask registerTask) {
    new Expectations() {
      {
      }
    };

    MicroserviceInstanceHeartbeatTask task =
        new MicroserviceInstanceHeartbeatTask(eventBus, srClient, microservice);
    registerTask.taskStatus = TaskStatus.INIT;
    eventBus.post(registerTask);

    task.run();
    Assert.assertNull(task.getHeartbeatResult());
    Assert.assertEquals(false, task.isNeedRegisterInstance());
    Assert.assertEquals(0, heartbeatTaskList.size());
  }

  @Test
  public void testOtherMicroservice(@Mocked ServiceRegistryClient srClient,
      @Mocked ServiceRegistryConfig serviceRegistryConfig,
      @Mocked MicroserviceWatchTask watchTask) {
    Microservice otherMicroservice = new Microservice();
    otherMicroservice.setServiceName("ms1");

    new Expectations() {
      {
        watchTask.getMicroservice();
        result = otherMicroservice;
      }
    };

    MicroserviceInstanceHeartbeatTask heartbeatTask =
        new MicroserviceInstanceHeartbeatTask(eventBus, srClient, microservice);
    watchTask.taskStatus = TaskStatus.READY;
    eventBus.post(watchTask);

    heartbeatTask.run();
    Assert.assertNull(heartbeatTask.getHeartbeatResult());
    Assert.assertEquals(false, heartbeatTask.isNeedRegisterInstance());
    Assert.assertEquals(0, heartbeatTaskList.size());
  }

  @Test
  public void testHeartbeatDisconnect(@Mocked ServiceRegistryClient srClient,
      @Mocked ServiceRegistryConfig serviceRegistryConfig,
      @Mocked MicroserviceWatchTask watchTask) {
    new Expectations() {
      {
        srClient.heartbeat(anyString, anyString);
        result = null;
        watchTask.getMicroservice();
        result = microservice;
      }
    };

    MicroserviceInstanceHeartbeatTask heartbeatTask =
        new MicroserviceInstanceHeartbeatTask(eventBus, srClient, microservice);
    watchTask.taskStatus = TaskStatus.READY;
    eventBus.post(watchTask);

    heartbeatTask.run();
    Assert.assertEquals(HeartbeatResult.DISCONNECTED, heartbeatTask.getHeartbeatResult());
    Assert.assertEquals(false, heartbeatTask.isNeedRegisterInstance());
    Assert.assertEquals(1, heartbeatTaskList.size());
  }

  @Test
  public void testHeartbeatNotRegistered(@Mocked ServiceRegistryClient srClient,
      @Mocked ServiceRegistryConfig serviceRegistryConfig,
      @Mocked MicroserviceWatchTask watchTask) {
    HeartbeatResponse response = new HeartbeatResponse();
    response.setOk(false);
    response.setMessage("FAIL");

    new Expectations() {
      {
        srClient.heartbeat(anyString, anyString);
        result = response;
        watchTask.getMicroservice();
        result = microservice;
      }
    };

    MicroserviceInstanceHeartbeatTask heartbeatTask =
        new MicroserviceInstanceHeartbeatTask(eventBus, srClient, microservice);
    watchTask.taskStatus = TaskStatus.READY;
    eventBus.post(watchTask);

    heartbeatTask.run();
    Assert.assertEquals(HeartbeatResult.INSTANCE_NOT_REGISTERED, heartbeatTask.getHeartbeatResult());
    Assert.assertEquals(true, heartbeatTask.isNeedRegisterInstance());
    Assert.assertEquals(1, heartbeatTaskList.size());
  }

  @Test
  public void testHeartbeatSuccess(@Mocked ServiceRegistryClient srClient,
      @Mocked ServiceRegistryConfig serviceRegistryConfig,
      @Mocked MicroserviceWatchTask watchTask) {
    HeartbeatResponse response = new HeartbeatResponse();
    response.setOk(true);
    response.setMessage("OK");

    new Expectations() {
      {
        srClient.heartbeat(anyString, anyString);
        result = response;
        watchTask.getMicroservice();
        result = microservice;
      }
    };

    MicroserviceInstanceHeartbeatTask heartbeatTask =
        new MicroserviceInstanceHeartbeatTask(eventBus, srClient, microservice);
    watchTask.taskStatus = TaskStatus.READY;
    eventBus.post(watchTask);

    heartbeatTask.run();
    Assert.assertEquals(HeartbeatResult.SUCCESS, heartbeatTask.getHeartbeatResult());
    Assert.assertEquals(false, heartbeatTask.isNeedRegisterInstance());
    Assert.assertEquals(1, heartbeatTaskList.size());
  }
}
