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

package org.apache.servicecomb.serviceregistry.consumer;

import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public class AppManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppManager.class);

  private EventBus eventBus;

  private MicroserviceVersionFactory microserviceVersionFactory = new DefaultMicroserviceVersionFactory();

  // key: appId
  private Map<String, MicroserviceManager> apps = new ConcurrentHashMapEx<>();

  private volatile StaticMicroserviceVersionFactory staticMicroserviceVersionFactory;

  public AppManager(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public EventBus getEventBus() {
    return eventBus;
  }

  public MicroserviceVersionFactory getMicroserviceVersionFactory() {
    return microserviceVersionFactory;
  }

  public Map<String, MicroserviceManager> getApps() {
    return apps;
  }

  public void setMicroserviceVersionFactory(MicroserviceVersionFactory microserviceVersionFactory) {
    this.microserviceVersionFactory = microserviceVersionFactory;
  }

  // microserviceName maybe normal name or alias name
  public MicroserviceVersionRule getOrCreateMicroserviceVersionRule(String appId, String microserviceName,
      String versionRule) {
    MicroserviceManager microserviceManager = getOrCreateMicroserviceManager(appId);

    return microserviceManager.getOrCreateMicroserviceVersionRule(microserviceName, versionRule);
  }

  public MicroserviceManager getOrCreateMicroserviceManager(String appId) {
    return apps.computeIfAbsent(appId, id -> new MicroserviceManager(this, appId));
  }

  public MicroserviceVersions getOrCreateMicroserviceVersions(String appId, String microserviceName) {
    MicroserviceManager microserviceManager = getOrCreateMicroserviceManager(appId);
    return microserviceManager.getOrCreateMicroserviceVersions(microserviceName);
  }

  public StaticMicroserviceVersionFactory getStaticMicroserviceVersionFactory() {
    if (null == staticMicroserviceVersionFactory) {
      synchronized (this) {
        if (null == staticMicroserviceVersionFactory) {
          loadStaticMicroserviceVersionFactory();
        }
      }
    }
    return staticMicroserviceVersionFactory;
  }

  public void setStaticMicroserviceVersionFactory(StaticMicroserviceVersionFactory staticMicroserviceVersionFactory) {
    this.staticMicroserviceVersionFactory = staticMicroserviceVersionFactory;
  }

  private void loadStaticMicroserviceVersionFactory() {
    String staticMicroserviceVersionFactoryClass = ServiceRegistryConfig.INSTANCE
        .getStaticMicroserviceVersionFactory();
    try {
      staticMicroserviceVersionFactory = (StaticMicroserviceVersionFactory) Class
          .forName(staticMicroserviceVersionFactoryClass).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      LOGGER.info("unable to load StaticMicroserviceVersionFactory", e);
      // interrupt this loading process because this error cannot be covered by us.
      throw new IllegalStateException("unable to load StaticMicroserviceVersionFactory", e);
    }
    LOGGER.info("staticMicroserviceVersionFactory is {}.", staticMicroserviceVersionFactoryClass);
  }
}
