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

import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.api.response.HeartbeatResponse;
import org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient;
import org.apache.servicecomb.serviceregistry.task.event.HeartbeatFailEvent;
import org.apache.servicecomb.serviceregistry.task.event.HeartbeatSuccEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class MicroserviceInstanceHeartbeatTask extends AbstractTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceInstanceHeartbeatTask.class);

  private MicroserviceInstance microserviceInstance;

  private HeartbeatResult heartbeatResult;

  public MicroserviceInstanceHeartbeatTask(EventBus eventBus, ServiceRegistryClient srClient,
      Microservice microservice) {
    super(eventBus, srClient, microservice);
    this.microserviceInstance = microservice.getInstance();
  }

  @Subscribe
  public void onMicroserviceWatchTask(MicroserviceWatchTask task) {
    if (task.taskStatus == TaskStatus.READY && isSameMicroservice(task.getMicroservice())) {
      this.taskStatus = TaskStatus.READY;
    }
  }

  public HeartbeatResult getHeartbeatResult() {
    return heartbeatResult;
  }

  // only got service center response, and result is not ok, means need to register instance again.
  public boolean isNeedRegisterInstance() {
    return HeartbeatResult.INSTANCE_NOT_REGISTERED.equals(heartbeatResult);
  }

  @Override
  public void doRun() {
    // will always run heartbeat when it is ready
    heartbeatResult = heartbeat();
  }

  private HeartbeatResult heartbeat() {
    HeartbeatResponse response =
        srClient.heartbeat(microserviceInstance.getServiceId(), microserviceInstance.getInstanceId());
    if (response == null) {
      LOGGER.error("Disconnected from service center and heartbeat failed for microservice instance={}/{}",
          microserviceInstance.getServiceId(),
          microserviceInstance.getInstanceId());
      EventManager.post(new HeartbeatFailEvent());
      return HeartbeatResult.DISCONNECTED;
    }

    if (!response.isOk()) {
      LOGGER.error("Update heartbeat to service center failed, microservice instance={}/{} does not exist",
          microserviceInstance.getServiceId(),
          microserviceInstance.getInstanceId());
      EventManager.post(new HeartbeatFailEvent());
      return HeartbeatResult.INSTANCE_NOT_REGISTERED;
    }
    EventManager.post(new HeartbeatSuccEvent());
    return HeartbeatResult.SUCCESS;
  }
}
