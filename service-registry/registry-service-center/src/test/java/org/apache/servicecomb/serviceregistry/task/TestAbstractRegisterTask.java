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

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

import mockit.Mocked;

public class TestAbstractRegisterTask {
  private EventBus eventBus;

  private Microservice microservice;

  @Before
  public void setup() {
    eventBus = new EventBus();

    microservice = new Microservice();
    microservice.setAppId("app");
    microservice.setServiceName("ms");

    microservice.setInstance(new MicroserviceInstance());
  }

  @Test
  public void testHeartbeatSuccess(@Mocked ServiceRegistryClient srClient) {
    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    ReflectUtils.setField(registerTask, "registered", true);

    MicroserviceInstanceHeartbeatTask heartbeatTask =
        new MicroserviceInstanceHeartbeatTask(eventBus, srClient, microservice);
    ReflectUtils.setField(heartbeatTask, "heartbeatResult", HeartbeatResult.SUCCESS);

    Assert.assertEquals(true, registerTask.isRegistered());
    eventBus.post(heartbeatTask);
    Assert.assertEquals(true, registerTask.isRegistered());
    Assert.assertEquals(eventBus, registerTask.getEventBus());
  }

  @Test
  public void testHeartbeatFailed(@Mocked ServiceRegistryClient srClient) {
    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    ReflectUtils.setField(registerTask, "registered", true);

    MicroserviceInstanceHeartbeatTask heartbeatTask =
        new MicroserviceInstanceHeartbeatTask(eventBus, srClient, microservice);
    ReflectUtils.setField(heartbeatTask, "heartbeatResult", HeartbeatResult.INSTANCE_NOT_REGISTERED);

    Assert.assertEquals(true, registerTask.isRegistered());
    eventBus.post(heartbeatTask);
    Assert.assertEquals(false, registerTask.isRegistered());
  }

  @Test
  public void testHeartbeatOtherFailed(@Mocked ServiceRegistryClient srClient) {
    MicroserviceRegisterTask registerTask = new MicroserviceRegisterTask(eventBus, srClient, microservice);
    ReflectUtils.setField(registerTask, "registered", true);

    Microservice otherMicroservice = new Microservice();
    otherMicroservice.setAppId(microservice.getAppId());
    otherMicroservice.setServiceName("ms1");

    MicroserviceInstanceHeartbeatTask heartbeatTask =
        new MicroserviceInstanceHeartbeatTask(eventBus, srClient, otherMicroservice);
    ReflectUtils.setField(heartbeatTask, "heartbeatResult", HeartbeatResult.INSTANCE_NOT_REGISTERED);

    Assert.assertEquals(true, registerTask.isRegistered());
    eventBus.post(heartbeatTask);
    Assert.assertEquals(true, registerTask.isRegistered());
  }
}
