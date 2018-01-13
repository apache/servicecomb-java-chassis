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

import org.apache.servicecomb.serviceregistry.task.event.ExceptionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ServiceCenterTask implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterTask.class);

  private EventBus eventBus;

  private int interval;

  private MicroserviceServiceCenterTask microserviceServiceCenterTask;

  private boolean registerInstanceSuccess = false;

  private ServiceCenterTaskMonitor serviceCenterTaskMonitor = new ServiceCenterTaskMonitor();

  public ServiceCenterTask(EventBus eventBus, int interval,
      MicroserviceServiceCenterTask microserviceServiceCenterTask) {
    this.eventBus = eventBus;
    this.interval = interval;
    this.microserviceServiceCenterTask = microserviceServiceCenterTask;

    this.eventBus.register(this);
  }

  // messages given in register error
  @Subscribe
  public void onRegisterTask(AbstractRegisterTask task) {
    LOGGER.info("read {} status is {}", task.getClass().getSimpleName(), task.taskStatus);
    if (task.getTaskStatus() == TaskStatus.FINISHED) {
      registerInstanceSuccess = true;
    } else {
      onException();
    }
  }

  // messages given in heartbeat
  @Subscribe
  public void onMicroserviceInstanceHeartbeatTask(MicroserviceInstanceHeartbeatTask task) {
    if (task.getHeartbeatResult() != HeartbeatResult.SUCCESS) {
      LOGGER.info("read MicroserviceInstanceHeartbeatTask status is {}", task.taskStatus);
      onException();
    }
  }

  // messages given in watch error
  @Subscribe
  public void onExceptionEvent(ExceptionEvent event) {
    LOGGER.info("read exception event, message is :{}", event.getThrowable().getMessage());
    onException();
  }

  private void onException() {
    if (registerInstanceSuccess) {
      registerInstanceSuccess = false;
    }
  }

  public void init() {
    microserviceServiceCenterTask.run();
  }

  @Override
  public void run() {
    try {
      serviceCenterTaskMonitor.beginCycle(interval);
      microserviceServiceCenterTask.run();
      serviceCenterTaskMonitor.endCycle();
    } catch (Throwable e) {
      LOGGER.error("unexpected exception caught from service center task. ", e);
    }
  }
}
