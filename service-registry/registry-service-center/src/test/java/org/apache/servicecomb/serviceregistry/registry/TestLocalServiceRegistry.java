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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;

import mockit.Deencapsulation;

public class TestLocalServiceRegistry {
  private static final AbstractConfiguration inMemoryConfig = new ConcurrentMapConfiguration();

  @BeforeClass
  public static void initSetup() throws Exception {
    AbstractConfiguration localConfig = ConfigUtil.createLocalConfig();
    ConcurrentCompositeConfiguration configuration = new ConcurrentCompositeConfiguration();
    configuration.addConfiguration(localConfig);
    configuration.addConfiguration(inMemoryConfig);

    ConfigurationManager.install(configuration);
  }

  @AfterClass
  public static void classTeardown() {
    Deencapsulation.setField(ConfigurationManager.class, "instance", null);
    Deencapsulation.setField(ConfigurationManager.class, "customConfigurationInstalled", false);
    Deencapsulation.setField(DynamicPropertyFactory.class, "config", null);
    RegistryUtils.setServiceRegistry(null);
  }

  @Before
  public void setUp() throws Exception {
    inMemoryConfig.clear();
  }

  @Test
  public void testLifeCycle() {
    ServiceRegistry serviceRegistry = LocalServiceRegistryFactory.createLocal();
    serviceRegistry.init();
    RegistryUtils.init();

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
    ServiceRegistry serviceRegistry = LocalServiceRegistryFactory.createLocal();
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
    Assert.assertEquals(properties, microservice.getInstance().getProperties());

    properties.put("k1", "v1");
    serviceRegistry.updateMicroserviceProperties(properties);
    Assert.assertEquals(properties, microservice.getProperties());
    serviceRegistry.updateInstanceProperties(properties);
    Assert.assertEquals(properties, microservice.getInstance().getProperties());
  }

  @Test
  public void testSchema() {
    ServiceRegistry serviceRegistry = LocalServiceRegistryFactory.createLocal();
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

  @Test
  public void registerMicroservice() {
    ServiceRegistry serviceRegistry = LocalServiceRegistryFactory.createLocal();
    serviceRegistry.init();
    serviceRegistry.run();

    Microservice microservice = new Microservice();
    microservice.setAppId("appId");
    microservice.setServiceName("msName");

    String serviceId = serviceRegistry.getServiceRegistryClient().registerMicroservice(microservice);
    Microservice remoteMicroservice = serviceRegistry.getRemoteMicroservice(serviceId);

    Assert.assertEquals(serviceId, remoteMicroservice.getServiceId());
  }
}
