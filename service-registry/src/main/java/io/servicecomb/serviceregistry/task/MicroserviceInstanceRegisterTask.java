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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.client.ServiceRegistryClient;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;

public class MicroserviceInstanceRegisterTask extends AbstractRegisterTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceInstanceRegisterTask.class);

  private ServiceRegistryConfig serviceRegistryConfig;

  private MicroserviceInstance microserviceInstance;

  public MicroserviceInstanceRegisterTask(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig,
      ServiceRegistryClient srClient,
      Microservice microservice) {
    super(eventBus, srClient, microservice);

    this.serviceRegistryConfig = serviceRegistryConfig;
    this.microserviceInstance = microservice.getIntance();
  }

  @Subscribe
  public void onMicroserviceRegisterTask(MicroserviceRegisterTask task) {
    if (task.taskStatus == TaskStatus.FINISHED && isSameMicroservice(task.getMicroservice())) {
      this.taskStatus = TaskStatus.READY;
      this.registered = false;
    } else {
      this.taskStatus = TaskStatus.INIT;
    }
  }

  @Override
  protected boolean doRegister() {
    LOGGER.info("running microservice instance register task.");
    String hostName = "";
    if (serviceRegistryConfig.isPreferIpAddress()) {
      hostName = RegistryUtils.getPublishAddress();
    } else {
      hostName = RegistryUtils.getPublishHostName();
    }
    microserviceInstance.setHostName(hostName);
    microserviceInstance.getHealthCheck().setInterval(serviceRegistryConfig.getHeartbeatInterval());
    microserviceInstance.getHealthCheck().setTimes(serviceRegistryConfig.getResendHeartBeatTimes());

    String instanceId = srClient.registerMicroserviceInstance(microserviceInstance);
    if (StringUtils.isEmpty(instanceId)) {
      LOGGER.error("Register microservice instance failed. microserviceId={}",
          microserviceInstance.getServiceId());
      return false;
    }
    microserviceInstance.setInstanceId(instanceId);
    LOGGER.info(
        "Register microservice instance success. microserviceId={} instanceId={} endpoints={} lease {}s",
        microserviceInstance.getServiceId(),
        instanceId,
        microserviceInstance.getEndpoints(),
        microserviceInstance.getHealthCheck().getTTL());

    return true;
  }
}
