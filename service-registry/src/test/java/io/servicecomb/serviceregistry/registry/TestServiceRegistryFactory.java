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

package io.servicecomb.serviceregistry.registry;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

import io.servicecomb.serviceregistry.ServiceRegistry;
import io.servicecomb.serviceregistry.client.LocalServiceRegistryClientImpl;
import io.servicecomb.serviceregistry.client.ServiceRegistryClient;
import io.servicecomb.serviceregistry.client.http.ServiceRegistryClientImpl;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.servicecomb.serviceregistry.definition.MicroserviceDefinition;
import mockit.Deencapsulation;

/**
 * Created by   on 2017/3/31.
 */
public class TestServiceRegistryFactory {
  @Test
  public void testGetRemoteRegistryClient() {
    EventBus eventBus = new EventBus();
    ServiceRegistryConfig serviceRegistryConfig = ServiceRegistryConfig.INSTANCE;
    MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(Collections.emptyList());

    ServiceRegistry serviceRegistry =
        ServiceRegistryFactory.create(eventBus, serviceRegistryConfig, microserviceDefinition);
    serviceRegistry.init();
    ServiceRegistryClient client = serviceRegistry.getServiceRegistryClient();
    Assert.assertTrue(client instanceof ServiceRegistryClientImpl);

    serviceRegistry = ServiceRegistryFactory.getOrCreate(eventBus,
        serviceRegistryConfig,
        microserviceDefinition);
    Assert.assertTrue(serviceRegistry instanceof RemoteServiceRegistry);
    Assert.assertEquals(serviceRegistry, ServiceRegistryFactory.getServiceRegistry());

    Deencapsulation.setField(ServiceRegistryFactory.class, "serviceRegistry", null);

    System.setProperty("local.registry.file", "/tmp/test.yaml");
    serviceRegistry = ServiceRegistryFactory.create(eventBus, serviceRegistryConfig, microserviceDefinition);
    serviceRegistry.init();
    client = serviceRegistry.getServiceRegistryClient();
    Assert.assertTrue(client instanceof LocalServiceRegistryClientImpl);
    Assert.assertTrue(ServiceRegistryFactory.getOrCreate(eventBus,
        serviceRegistryConfig,
        microserviceDefinition) instanceof LocalServiceRegistry);
    System.clearProperty("local.registry.file");
  }
}
