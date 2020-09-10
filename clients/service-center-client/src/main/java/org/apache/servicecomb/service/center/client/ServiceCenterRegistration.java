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

package org.apache.servicecomb.service.center.client;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.service.center.client.RegistrationEvents.HeartBeatEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceInstanceRegistrationEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceRegistrationEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.SchemaRegistrationEvent;
import org.apache.servicecomb.service.center.client.model.CreateSchemaRequest;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.RegisteredMicroserviceInstanceResponse;
import org.apache.servicecomb.service.center.client.model.RegisteredMicroserviceResponse;
import org.apache.servicecomb.service.center.client.model.SchemaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class ServiceCenterRegistration {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterRegistration.class);

  private final ServiceCenterClient serviceCenterClient;

  private final EventBus eventBus;

  private final ExecutorService taskPool;

  private Microservice microservice;

  private MicroserviceInstance microserviceInstance;

  private List<SchemaInfo> schemaInfos;

  public ServiceCenterRegistration(ServiceCenterClient serviceCenterClient, EventBus eventBus) {
    this.serviceCenterClient = serviceCenterClient;
    this.eventBus = eventBus;
    this.taskPool = Executors.newSingleThreadExecutor((task) ->
        new Thread(task, "service-center-registration-task"));
  }

  public void setMicroserviceInstance(MicroserviceInstance microserviceInstance) {
    this.microserviceInstance = microserviceInstance;
  }

  public void setMicroservice(Microservice microservice) {
    this.microservice = microservice;
  }

  public void setSchemaInfos(List<SchemaInfo> schemaInfos) {
    this.schemaInfos = schemaInfos;
  }

  public void startRegistration() {
    startTask(new RegisterMicroserviceTask(0));
  }

  private void startTask(Task task) {
    try {
      this.taskPool.execute(() -> {
        try {
          task.execute();
        } catch (Throwable e) {
          LOGGER.error("unexpected error execute task {}", task.getClass().getName(), e);
        }
      });
    } catch (RejectedExecutionException e) {
      LOGGER.error("execute task rejected {}", task.getClass().getName(), e);
    }
  }


  interface Task {
    void execute();
  }

  class RegisterMicroserviceTask implements Task {
    int failedCount;

    RegisterMicroserviceTask(int failedCount) {
      this.failedCount = failedCount;
    }

    @Override
    public void execute() {
      try {
        RegisteredMicroserviceResponse serviceResponse = serviceCenterClient.queryServiceId(microservice);
        if (serviceResponse == null) {
          RegisteredMicroserviceResponse response = serviceCenterClient.registerMicroservice(microservice);
          if (StringUtils.isEmpty(response.getServiceId())) {
            LOGGER.error("register microservice failed, and will try again.");
            eventBus.post(new MicroserviceRegistrationEvent(false));
            startTask(new BackOffSleepTask(failedCount + 1, new RegisterMicroserviceTask(failedCount + 1)));
          } else {
            microservice.setServiceId(response.getServiceId());
            microserviceInstance.setServiceId(response.getServiceId());
            eventBus.post(new MicroserviceRegistrationEvent(true));
            startTask(new RegisterSchemaTask(0));
          }
        } else {
          LOGGER.info("service has already registered, will not register schema.");
          microservice.setServiceId(serviceResponse.getServiceId());
          microserviceInstance.setServiceId(serviceResponse.getServiceId());
          eventBus.post(new MicroserviceRegistrationEvent(true));
          startTask(new RegisterMicroserviceInstanceTask(0));
        }
      } catch (Exception e) {
        LOGGER.error("register microservice failed, and will try again.", e);
        eventBus.post(new MicroserviceRegistrationEvent(false));
        startTask(new BackOffSleepTask(failedCount + 1, new RegisterMicroserviceTask(failedCount + 1)));
      }
    }
  }

  class RegisterSchemaTask implements Task {
    int failedCount;

    RegisterSchemaTask(int failedCount) {
      this.failedCount = failedCount;
    }

    @Override
    public void execute() {
      try {
        if (schemaInfos == null || schemaInfos.isEmpty()) {
          LOGGER.warn("no schemas defined for this microservice.");
          eventBus.post(new SchemaRegistrationEvent(true));
          startTask(new RegisterMicroserviceInstanceTask(0));
          return;
        }

        for (SchemaInfo schemaInfo : schemaInfos) {
          CreateSchemaRequest request = new CreateSchemaRequest();
          request.setSchema(schemaInfo.getSchema());
          request.setSummary(schemaInfo.getSummary());
          if (!serviceCenterClient.registerSchema(microservice.getServiceId(), schemaInfo.getSchemaId(), request)) {
            LOGGER.error("register schema content failed, and will try again.");
            eventBus.post(new SchemaRegistrationEvent(false));
            // back off by multiply
            startTask(new BackOffSleepTask(failedCount + 1, new RegisterSchemaTask((failedCount + 1) * 2)));
            return;
          }
        }

        eventBus.post(new SchemaRegistrationEvent(true));
        startTask(new RegisterMicroserviceInstanceTask(0));
      } catch (Exception e) {
        LOGGER.error("register schema content failed, and will try again.", e);
        eventBus.post(new SchemaRegistrationEvent(false));
        // back off by multiply
        startTask(new BackOffSleepTask(failedCount + 1, new RegisterSchemaTask((failedCount + 1) * 2)));
      }
    }
  }

  class RegisterMicroserviceInstanceTask implements Task {
    int failedCount;

    RegisterMicroserviceInstanceTask(int failedCount) {
      this.failedCount = failedCount;
    }

    @Override
    public void execute() {
      try {
        RegisteredMicroserviceInstanceResponse instance = serviceCenterClient
            .registerMicroserviceInstance(microserviceInstance);
        if (instance == null) {
          LOGGER.error("register microservice instance failed, and will try again.");
          eventBus.post(new MicroserviceInstanceRegistrationEvent(false));
          startTask(new BackOffSleepTask(failedCount + 1, new RegisterMicroserviceInstanceTask(failedCount + 1)));
        } else {
          microserviceInstance.setInstanceId(instance.getInstanceId());
          eventBus.post(new MicroserviceInstanceRegistrationEvent(true));
          startTask(new SendHeartBeatTask(0));
        }
      } catch (Exception e) {
        LOGGER.error("register microservice instance failed, and will try again.", e);
        eventBus.post(new MicroserviceInstanceRegistrationEvent(false));
        startTask(new BackOffSleepTask(failedCount + 1, new RegisterMicroserviceInstanceTask(failedCount + 1)));
      }
    }
  }

  class SendHeartBeatTask implements Task {
    final int failedRetry = 3;

    final long heartBeatInterval = 30000;

    final long heartBeatRequestTimeout = 5000;

    int failedCount;

    SendHeartBeatTask(int failedCount) {
      this.failedCount = failedCount;
    }

    @Override
    public void execute() {
      try {
        if (failedCount >= failedRetry) {
          eventBus.post(new HeartBeatEvent(false));
          startTask(new RegisterMicroserviceTask(0));
          return;
        }

        if (!serviceCenterClient.sendHeartBeat(microservice.getServiceId(), microserviceInstance.getInstanceId())) {
          LOGGER.error("send heart failed, and will try again.");
          eventBus.post(new HeartBeatEvent(false));
          startTask(new BackOffSleepTask(failedCount + 1, new SendHeartBeatTask(failedCount + 1)));
        } else {
          // wait 10 * 3000 ms and send heart beat again.
          eventBus.post(new HeartBeatEvent(true));
          startTask(
              new BackOffSleepTask(Math.max(heartBeatInterval, heartBeatRequestTimeout), new SendHeartBeatTask(0)));
        }
      } catch (Exception e) {
        LOGGER.error("send heart failed, and will try again.", e);
        eventBus.post(new HeartBeatEvent(false));
        startTask(new BackOffSleepTask(failedCount + 1, new SendHeartBeatTask(failedCount + 1)));
      }
    }
  }

  class BackOffSleepTask implements Task {
    final long base = 3000;

    final long max = 60000;

    long waitTime;

    Task nextTask;

    BackOffSleepTask(int failedCount, Task nextTask) {
      this.waitTime = failedCount * base;
      this.nextTask = nextTask;
    }

    BackOffSleepTask(long waitTime, Task nextTask) {
      this.waitTime = waitTime;
      this.nextTask = nextTask;
    }

    @Override
    public void execute() {
      long time = Math.min(max, waitTime);
      try {
        Thread.sleep(time);
      } catch (InterruptedException e) {
        LOGGER.error("unexpected interrupt during sleep", e);
      }
      startTask(nextTask);
    }
  }
}
