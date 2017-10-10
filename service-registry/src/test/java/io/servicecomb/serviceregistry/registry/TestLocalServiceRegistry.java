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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

import io.servicecomb.serviceregistry.ServiceRegistry;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.task.event.ExceptionEvent;
import io.servicecomb.serviceregistry.task.event.RecoveryEvent;

public class TestLocalServiceRegistry {
  @Test
  public void testCacheAvaiable() {
    AbstractServiceRegistry serviceRegistry = (AbstractServiceRegistry) ServiceRegistryFactory.createLocal();
    EventBus eventBus = serviceRegistry.getEventBus();
    serviceRegistry.init();

    Assert.assertEquals(false, serviceRegistry.cacheAvailable);
    eventBus.post(new RecoveryEvent());
    Assert.assertEquals(true, serviceRegistry.cacheAvailable);

    eventBus.post(new ExceptionEvent(null));
    Assert.assertEquals(false, serviceRegistry.cacheAvailable);
  }

  @Test
  public void testLifeCycle() {
    ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
    serviceRegistry.init();

    Assert.assertNull(serviceRegistry.getMicroserviceInstance().getInstanceId());
    serviceRegistry.run();
    Assert.assertNotNull(serviceRegistry.getMicroserviceInstance().getInstanceId());

    serviceRegistry.destroy();
    Assert.assertTrue(serviceRegistry.getServiceRegistryClient()
        .getMicroserviceInstance("", serviceRegistry.getMicroservice().getServiceId())
        .isEmpty());
  }

  @Test
  public void testUpdateProperties() {
    ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
    serviceRegistry.init();
    serviceRegistry.run();

    Microservice microservice = serviceRegistry.getMicroservice();
    Map<String, String> properties = new HashMap<>();
    properties.put("k", "v");

    try {
      serviceRegistry.getServiceRegistryClient().updateInstanceProperties(microservice.getServiceId(),
          "notExist",
          properties);
      Assert.fail("must throw exception");
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Invalid argument. microserviceId=" + microservice.getServiceId()
          + ", microserviceInstanceId=notExist.",
          e.getMessage());
    }

    serviceRegistry.updateMicroserviceProperties(properties);
    Assert.assertEquals(properties, microservice.getProperties());
    serviceRegistry.updateInstanceProperties(properties);
    Assert.assertEquals(properties, microservice.getIntance().getProperties());

    properties.put("k1", "v1");
    serviceRegistry.updateMicroserviceProperties(properties);
    Assert.assertEquals(properties, microservice.getProperties());
    serviceRegistry.updateInstanceProperties(properties);
    Assert.assertEquals(properties, microservice.getIntance().getProperties());
  }

  @Test
  public void testSchema() {
    ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
    Microservice microservice = serviceRegistry.getMicroservice();
    microservice.addSchema("s1", "s1-content");
    serviceRegistry.init();
    serviceRegistry.run();

    try {
      serviceRegistry.getServiceRegistryClient().isSchemaExist("notExist", "s1");
      Assert.fail("must throw exception");
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Invalid serviceId, serviceId=notExist", e.getMessage());
    }
    try {
      serviceRegistry.getServiceRegistryClient().getSchema("notExist", "s1");
      Assert.fail("must throw exception");
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Invalid serviceId, serviceId=notExist", e.getMessage());
    }

    Assert.assertEquals(true,
        serviceRegistry.getServiceRegistryClient().isSchemaExist(microservice.getServiceId(), "s1"));
    String content = serviceRegistry.getServiceRegistryClient().getSchema(microservice.getServiceId(), "s1");
    Assert.assertEquals("s1-content", content);
  }
}
