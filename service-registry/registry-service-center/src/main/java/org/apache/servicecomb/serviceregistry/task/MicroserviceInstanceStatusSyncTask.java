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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.lang.StringUtils;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicroserviceInstanceStatusSyncTask extends AbstractTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceInstanceStatusSyncTask.class);

  public MicroserviceInstanceStatusSyncTask(EventBus eventBus, ServiceRegistryClient srClient,
      Microservice microservice) {
    super(eventBus, srClient, microservice);
  }

  @Subscribe
  public void onMicroserviceRegisterTask(MicroserviceInstanceRegisterTask task) {
    if (task.taskStatus == TaskStatus.FINISHED && isSameMicroservice(task.getMicroservice())) {
      LOGGER.info("start synchronizing instance status");
      this.taskStatus = TaskStatus.READY;
    }
  }

  @Override
  public void run() {
    if (taskStatus == TaskStatus.READY) {
      doRun();
    }
  }

  @Override
  protected void doRun() {
    if (isInstanceNotRegistered()) {
      return;
    }
    MicroserviceInstance serviceInstance = queryMicroserviceInstance();
    if (serviceInstance == null) {
      return;
    }

    if (RegistryUtils.getMicroserviceInstance().getStatus().equals(serviceInstance.getStatus())) {
      return;
    }

    LOGGER.info("sync instance status from sc, current status is [{}], changed to [{}]",
      RegistryUtils.getMicroserviceInstance().getStatus(),
      serviceInstance.getStatus());
    RegistryUtils.getMicroserviceInstance().setStatus(serviceInstance.getStatus());
  }

  private boolean isInstanceNotRegistered() {
    if (StringUtils.isEmpty(microservice.getServiceId()) || StringUtils.isEmpty(RegistryUtils.getMicroserviceInstance().getInstanceId())) {
      LOGGER.warn("instance status synchronization condition not met, serviceId = [{}], instanceId = [{}]",
        microservice.getServiceId(), RegistryUtils.getMicroserviceInstance().getInstanceId());
      return true;
    }
    return false;
  }

  private MicroserviceInstance queryMicroserviceInstance() {
    MicroserviceInstance serviceInstance = srClient.findServiceInstance(
      microservice.getServiceId(),
      RegistryUtils.getMicroserviceInstance().getInstanceId());
    if (serviceInstance == null) {
      LOGGER.warn("failed to find this instance in sc, waiting for instance registration");
    }
    return serviceInstance;
  }
}