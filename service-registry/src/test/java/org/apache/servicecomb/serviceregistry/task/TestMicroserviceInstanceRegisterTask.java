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

import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.HealthCheck;
import org.apache.servicecomb.serviceregistry.api.registry.HealthCheckMode;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import mockit.Expectations;
import mockit.Mocked;

public class TestMicroserviceInstanceRegisterTask {
  private EventBus eventBus;

  private Microservice microservice;

  private List<MicroserviceInstanceRegisterTask> taskList;

  @Mocked
  private ServiceRegistryConfig serviceRegistryConfig;

  @Mocked
  private ServiceRegistryClient srClient;

  @Before
  public void setup() {
    eventBus = new EventBus();

    taskList = new ArrayList<>();
    eventBus.register(new Object() {
      @Subscribe
      public void onEvent(MicroserviceInstanceRegisterTask task) {
        taskList.add(task);
      }
    });

    microservice = new Microservice();
    microservice.setAppId("app");
    microservice.setServiceName("ms");
    microservice.setServiceId("serviceId");

    microservice.setInstance(new MicroserviceInstance());

    HealthCheck healthCheck = new HealthCheck();
    healthCheck.setMode(HealthCheckMode.HEARTBEAT);
    microservice.getInstance().setHealthCheck(healthCheck);
  }

  @Test
  public void microserviceNotRegistered() {
    microservice.setServiceId(null);

    MicroserviceInstanceRegisterTask registerTask =
        new MicroserviceInstanceRegisterTask(eventBus, serviceRegistryConfig, null, microservice);
    registerTask.run();

    Assert.assertEquals(false, registerTask.isRegistered());
    Assert.assertEquals(0, taskList.size());
  }

  @Test
  public void registerIpSuccess() {
    MicroserviceInstance instance = microservice.getInstance();
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getPublishAddress();
        result = "127.0.0.1";
        serviceRegistryConfig.isPreferIpAddress();
        result = true;
        serviceRegistryConfig.getHeartbeatInterval();
        result = 10;
        serviceRegistryConfig.getResendHeartBeatTimes();
        result = 20;
        srClient.registerMicroserviceInstance(instance);
        result = "instanceId";
      }
    };
    MicroserviceInstanceRegisterTask registerTask =
        new MicroserviceInstanceRegisterTask(eventBus, serviceRegistryConfig, srClient, microservice);
    registerTask.taskStatus = TaskStatus.READY;
    registerTask.run();

    Assert.assertEquals(true, registerTask.isRegistered());
    Assert.assertEquals("127.0.0.1", instance.getHostName());
    Assert.assertEquals("instanceId", instance.getInstanceId());
    Assert.assertEquals(10, instance.getHealthCheck().getInterval());
    Assert.assertEquals(20, instance.getHealthCheck().getTimes());
    Assert.assertEquals(1, taskList.size());
  }

  @Test
  public void registerHostSuccess() {
    MicroserviceInstance instance = microservice.getInstance();
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getPublishHostName();
        result = "hostName";
        serviceRegistryConfig.isPreferIpAddress();
        result = false;
        serviceRegistryConfig.getHeartbeatInterval();
        result = 10;
        serviceRegistryConfig.getResendHeartBeatTimes();
        result = 20;
        srClient.registerMicroserviceInstance(instance);
        result = "instanceId";
      }
    };
    MicroserviceInstanceRegisterTask registerTask =
        new MicroserviceInstanceRegisterTask(eventBus, serviceRegistryConfig, srClient, microservice);
    registerTask.taskStatus = TaskStatus.READY;
    registerTask.run();

    Assert.assertEquals(true, registerTask.isRegistered());
    Assert.assertEquals("hostName", instance.getHostName());
    Assert.assertEquals("instanceId", instance.getInstanceId());
    Assert.assertEquals(10, instance.getHealthCheck().getInterval());
    Assert.assertEquals(20, instance.getHealthCheck().getTimes());
    Assert.assertEquals(1, taskList.size());
  }

  @Test
  public void registerIpFailed() {
    MicroserviceInstance instance = microservice.getInstance();
    new Expectations(RegistryUtils.class) {
      {
        RegistryUtils.getPublishAddress();
        result = "127.0.0.1";
        serviceRegistryConfig.isPreferIpAddress();
        result = true;
        serviceRegistryConfig.getHeartbeatInterval();
        result = 10;
        serviceRegistryConfig.getResendHeartBeatTimes();
        result = 20;
        srClient.registerMicroserviceInstance(instance);
        result = null;
      }
    };
    MicroserviceInstanceRegisterTask registerTask =
        new MicroserviceInstanceRegisterTask(eventBus, serviceRegistryConfig, srClient, microservice);
    registerTask.taskStatus = TaskStatus.READY;
    registerTask.run();

    Assert.assertEquals(false, registerTask.isRegistered());
    Assert.assertEquals("127.0.0.1", instance.getHostName());
    Assert.assertEquals(10, instance.getHealthCheck().getInterval());
    Assert.assertEquals(20, instance.getHealthCheck().getTimes());
    Assert.assertEquals(1, taskList.size());
  }
}
