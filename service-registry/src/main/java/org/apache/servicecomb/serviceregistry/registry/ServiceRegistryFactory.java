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

package org.apache.servicecomb.serviceregistry.registry;

import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.client.LocalServiceRegistryClientImpl;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.eventbus.EventBus;

/**
 * Created by   on 2017/3/31.
 */
public final class ServiceRegistryFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistryFactory.class);

  private static final Object LOCK = new Object();

  private static volatile ServiceRegistry serviceRegistry;

  private ServiceRegistryFactory() {
  }

  public static ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

  public static ServiceRegistry getOrCreate(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig,
      MicroserviceDefinition microserviceDefinition) {
    if (serviceRegistry == null) {
      synchronized (LOCK) {
        if (serviceRegistry == null) {
          serviceRegistry = create(eventBus, serviceRegistryConfig, microserviceDefinition);
        }
      }
    }
    return serviceRegistry;
  }

  public static ServiceRegistry createLocal() {
    EventBus eventBus = new EventBus();
    ServiceRegistryConfig serviceRegistryConfig = ServiceRegistryConfig.INSTANCE;
    MicroserviceConfigLoader loader = new MicroserviceConfigLoader();
    loader.loadAndSort();

    MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(loader.getConfigModels());
    return new LocalServiceRegistry(eventBus, serviceRegistryConfig, microserviceDefinition);
  }

  public static ServiceRegistry create(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig,
      MicroserviceDefinition microserviceDefinition) {
    String localModeFile = System.getProperty(LocalServiceRegistryClientImpl.LOCAL_REGISTRY_FILE_KEY);
    if (!StringUtils.isEmpty(localModeFile)) {
      LOGGER.info(
          "It is running in the local development mode, the local file {} is using as the local registry",
          localModeFile);

      return new LocalServiceRegistry(eventBus, serviceRegistryConfig, microserviceDefinition);
    }

    LOGGER.info("It is running in the normal mode, a separated service registry is required");
    return new RemoteServiceRegistry(eventBus, serviceRegistryConfig, microserviceDefinition);
  }
}
