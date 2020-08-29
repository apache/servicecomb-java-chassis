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

package org.apache.servicecomb.serviceregistry.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.api.registry.MicroserviceFactory;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.registry.LocalServiceRegistryFactory;
import org.junit.Assert;
import org.junit.Test;

public class TestPropertiesLoader {
  private static MicroserviceFactory microserviceFactory = new MicroserviceFactory();

  @Test
  public void testEmptyExtendedClass() {
    Configuration configuration = ConfigUtil.createLocalConfig();
    configuration.setProperty(BootStrapProperties.CONFIG_SERVICE_NAME, "emptyExtendedClass");
    configuration.clearProperty(BootStrapProperties.CONFIG_SERVICE_PROPERTIES);
    configuration.clearProperty(BootStrapProperties.OLD_CONFIG_SERVICE_PROPERTIES);
    Microservice microservice = microserviceFactory.create(configuration);
    // microservice.yaml has 3 properties
    Assert.assertEquals(3, microservice.getProperties().size());
  }

  @Test
  public void testInvalidExtendedClass() {
    Configuration configuration = ConfigUtil.createLocalConfig();
    configuration.setProperty(BootStrapProperties.CONFIG_SERVICE_NAME, "invalidExtendedClass");
    configuration.setProperty(BootStrapProperties.CONFIG_SERVICE_EXTENDED_CLASS, "invalidClass");

    try {
      microserviceFactory.create(configuration);
      Assert.fail("Must throw exception");
    } catch (Error e) {
      Assert.assertEquals(ClassNotFoundException.class, e.getCause().getClass());
      Assert.assertEquals("invalidClass", e.getCause().getMessage());
    }
  }

  @Test
  public void testCanNotAssignExtendedClass() {
    Configuration configuration = ConfigUtil.createLocalConfig();
    configuration.setProperty(BootStrapProperties.CONFIG_SERVICE_NAME, "invalidExtendedClass");
    configuration.setProperty(BootStrapProperties.CONFIG_SERVICE_EXTENDED_CLASS, "java.lang.String");

    try {
      microserviceFactory.create(configuration);
      Assert.fail("Must throw exception");
    } catch (Error e) {
      Assert.assertEquals(
          "Define propertyExtendedClass java.lang.String in yaml, but not implement the interface PropertyExtended.",
          e.getMessage());
    }
  }

  @Test
  public void testMicroservicePropertiesLoader() throws Exception {
    Microservice microservice = LocalServiceRegistryFactory.createLocal().getMicroservice();
    Map<String, String> expectedMap = new HashMap<>();
    expectedMap.put("key1", "value1");
    expectedMap.put("key2", "value2");
    expectedMap.put("ek0", "ev0");
    Assert.assertEquals(expectedMap, microservice.getProperties());
  }

  @Test
  public void testInstancePropertiesLoader() {
    Microservice microservice = LocalServiceRegistryFactory.createLocal().getMicroservice();
    MicroserviceInstance instance = microservice.getInstance();
    Map<String, String> expectedMap = new HashMap<>();
    expectedMap.put("key0", "value0");
    expectedMap.put("ek0", "ev0");
    Assert.assertEquals(expectedMap, instance.getProperties());
  }
}
