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
import org.apache.servicecomb.foundation.common.event.SimpleEventBus;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceDefinition;

import com.google.common.eventbus.EventBus;

public class LocalServiceRegistryFactory {
  private static final String REGISTRY_FILE = "registry.yaml";

  public static ServiceRegistry createLocal() {
    return createLocal(REGISTRY_FILE);
  }

  public static ServiceRegistry createLocal(String localFile) {
    EventBus eventBus = new SimpleEventBus();
    ServiceRegistryConfig serviceRegistryConfig = ServiceRegistryConfig.INSTANCE;
    MicroserviceConfigLoader loader = new MicroserviceConfigLoader();
    loader.loadAndSort();

    MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(loader.getConfigModels());
    return new LocalServiceRegistry(eventBus, serviceRegistryConfig, microserviceDefinition).localFile(localFile);
  }

  public static ServiceRegistry createLocal(EventBus eventBus, ServiceRegistryConfig serviceRegistryConfig,
      MicroserviceDefinition microserviceDefinition) {
    if (null == eventBus) {
      eventBus = new SimpleEventBus();
    }
    return new LocalServiceRegistry(eventBus, serviceRegistryConfig, microserviceDefinition).localFile(REGISTRY_FILE);
  }
}
