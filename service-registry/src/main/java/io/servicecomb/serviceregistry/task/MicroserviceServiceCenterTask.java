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

import com.google.common.eventbus.EventBus;

import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.client.ServiceRegistryClient;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;

public class MicroserviceServiceCenterTask extends CompositeTask {
  public MicroserviceServiceCenterTask(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig,
      ServiceRegistryClient srClient, Microservice microservice) {
    addTask(new MicroserviceRegisterTask(eventBus, srClient, microservice));
    addTask(new MicroserviceInstanceRegisterTask(eventBus, serviceRegistryConfig, srClient, microservice));
    addTask(new MicroserviceWatchTask(eventBus, serviceRegistryConfig, srClient, microservice));
    addTask(new MicroserviceInstanceHeartbeatTask(eventBus, srClient, microservice));
  }
}
