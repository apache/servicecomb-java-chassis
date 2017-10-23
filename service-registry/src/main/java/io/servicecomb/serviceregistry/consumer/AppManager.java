/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.consumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.eventbus.EventBus;

public class AppManager {
  private EventBus eventBus;

  private MicroserviceVersionFactory microserviceVersionFactory = new DefaultMicroserviceVersionFactory();

  // key: appId
  private Map<String, MicroserviceManager> apps = new ConcurrentHashMap<>();

  public AppManager(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public EventBus getEventBus() {
    return eventBus;
  }

  public MicroserviceVersionFactory getMicroserviceVersionFactory() {
    return microserviceVersionFactory;
  }

  public void setMicroserviceVersionFactory(MicroserviceVersionFactory microserviceVersionFactory) {
    this.microserviceVersionFactory = microserviceVersionFactory;
  }

  // microserviceName maybe normal name or alias name
  public MicroserviceVersionRule getOrCreateMicroserviceVersionRule(String appId, String microserviceName,
      String versionRule) {
    MicroserviceManager microserviceManager = apps.computeIfAbsent(appId, id -> {
      return new MicroserviceManager(this, appId);
    });

    return microserviceManager.getOrCreateMicroserviceVersionRule(microserviceName, versionRule);
  }
}
