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

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.http.client.task.AbstractTask;
import org.apache.servicecomb.http.client.task.Task;
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
import org.apache.servicecomb.service.center.client.model.ServiceCenterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class ServiceCenterRegistration extends AbstractTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterRegistration.class);

  public static final int MAX_INTERVAL = 600000;

  public static final int MIN_INTERVAL = 1000;

  private final ServiceCenterClient serviceCenterClient;

  private final EventBus eventBus;

  private Microservice microservice;

  private MicroserviceInstance microserviceInstance;

  private List<SchemaInfo> schemaInfos;

  private final ServiceCenterConfiguration serviceCenterConfiguration;

  private long heartBeatInterval = 15000;

  private long heartBeatRequestTimeout = 5000;

  public ServiceCenterRegistration(ServiceCenterClient serviceCenterClient, ServiceCenterConfiguration
      serviceCenterConfiguration, EventBus eventBus) {
    super("service-center-registration-task");
    this.serviceCenterClient = serviceCenterClient;
    this.serviceCenterConfiguration = serviceCenterConfiguration;
    this.eventBus = eventBus;
  }

  public ServiceCenterRegistration setMicroserviceInstance(MicroserviceInstance microserviceInstance) {
    this.microserviceInstance = microserviceInstance;
    return this;
  }

  public ServiceCenterRegistration setMicroservice(Microservice microservice) {
    this.microservice = microservice;
    return this;
  }

  public ServiceCenterRegistration setHeartBeatInterval(long interval) {
    if (interval > MAX_INTERVAL || interval < MIN_INTERVAL) {
      return this;
    }
    this.heartBeatInterval = interval;
    return this;
  }

  public ServiceCenterRegistration setHeartBeatRequestTimeout(long timeout) {
    if (timeout > MAX_INTERVAL || timeout < MIN_INTERVAL) {
      return this;
    }
    this.heartBeatRequestTimeout = timeout;
    return this;
  }

  public ServiceCenterRegistration setSchemaInfos(List<SchemaInfo> schemaInfos) {
    this.schemaInfos = schemaInfos;
    return this;
  }

  public void startRegistration() {
    startTask(new RegisterMicroserviceTask(0));
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
            return;
          }
          microservice.setServiceId(response.getServiceId());
          microserviceInstance.setServiceId(response.getServiceId());
          microserviceInstance.setMicroservice(microservice);
          eventBus.post(new MicroserviceRegistrationEvent(true));
          startTask(new RegisterSchemaTask(0));
        } else {
          Microservice newMicroservice = serviceCenterClient.getMicroserviceByServiceId(serviceResponse.getServiceId());
          boolean swaggerDifferent = isSwaggerDifferent(newMicroservice);
          if (swaggerDifferent){
            boolean canRewriteSwagger = canRewriteSwagger();
            if (canRewriteSwagger){
              LOGGER.warn("Service has already registered, but schema ids not equal. However, it will continue to register");
              startTask(new RegisterSchemaTask(0));
            }
          }
          microservice.setServiceId(serviceResponse.getServiceId());
          microserviceInstance.setServiceId(serviceResponse.getServiceId());
          microserviceInstance.setMicroservice(microservice);
          eventBus.post(new MicroserviceRegistrationEvent(true));
          startTask(new RegisterMicroserviceInstanceTask(0));
        }
      } catch (IllegalStateException e) {
        throw e;
      } catch (Exception e) {
        LOGGER.error("register microservice failed, and will try again.", e);
        eventBus.post(new MicroserviceRegistrationEvent(false));
        startTask(new BackOffSleepTask(failedCount + 1, new RegisterMicroserviceTask(failedCount + 1)));
      }
    }
  }

  private boolean isSwaggerDifferent(Microservice newMicroservice) {
    if (!isListEquals(newMicroservice.getSchemas(),microservice.getSchemas())){
      if (!serviceCenterConfiguration.isIgnoreSwaggerDifferent()){
        return true;
      }else {
        LOGGER.warn("Service has already registered, but schema ids not equal. However, it will continue to register. "
                + "If you want to eliminate this warning. you can Change the microservice version or delete the old " +
                "microservice info and try again. eg: version:1.0.0 -> 1.0.1");
        return false;
      }
    }
    return false;
    }

  private boolean canRewriteSwagger(){
    if (serviceCenterConfiguration.canRewriteSwagger()){
      return true;
    }else {
      LOGGER.warn("Service has already registered, but schema ids not equal. However, it will continue to register. "
              + "If you want to eliminate this warning. you can Change the microservice version or delete the old " +
              "microservice info and try again. eg: version:1.0.0 -> 1.0.1");
    }
    return false;
  }

  private boolean isListEquals(List<?> one, List<?> two) {
    return one.size() == two.size() && one.containsAll(two) && two.containsAll(one);
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
          LOGGER.info("register microservice successfully, service id={}, instance id={}", microservice.getServiceId(),
              microserviceInstance.getInstanceId());
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
    private static final int FAILED_RETRY = 3;

    int failedCount;

    SendHeartBeatTask(int failedCount) {
      this.failedCount = failedCount;
    }

    @Override
    public void execute() {
      try {
        if (failedCount >= FAILED_RETRY) {
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
}
