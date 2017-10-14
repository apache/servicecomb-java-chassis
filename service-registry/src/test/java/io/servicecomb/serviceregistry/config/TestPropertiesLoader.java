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

package io.servicecomb.serviceregistry.config;

import static io.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_SERVICE_DESCRIPTION_KEY;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

import com.netflix.config.DynamicConfiguration;

import io.servicecomb.config.archaius.sources.ConfigModel;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceFactory;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.definition.MicroserviceDefinition;
import io.servicecomb.serviceregistry.registry.ServiceRegistryFactory;

public class TestPropertiesLoader {
  private static MicroserviceFactory microserviceFactory = new MicroserviceFactory();

  @Test
  public void testMergeStrings() {
    Assert.assertEquals("abc123efg", AbstractPropertiesLoader.mergeStrings("abc", "123", "efg"));
  }

  @Test
  public void testEmptyExtendedClass() {
    Microservice microservice = microserviceFactory.create("default", "emptyExtendedClass");
    Assert.assertEquals(0, microservice.getProperties().size());
  }

  @Test
  public void testInvalidExtendedClass() {
    ConfigModel configModel = MicroserviceDefinition.createConfigModel("default", "invalidExtendedClass");
    @SuppressWarnings("unchecked")
    Map<String, Object> desc =
        (Map<String, Object>) configModel.getConfig().get(CONFIG_SERVICE_DESCRIPTION_KEY);
    desc.put("propertyExtentedClass", "invalidClass");
    MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(Arrays.asList(configModel));
    try {
      microserviceFactory.create(microserviceDefinition);
      Assert.fail("Must throw exception");
    } catch (Error e) {
      Assert.assertEquals(ClassNotFoundException.class, e.getCause().getClass());
      Assert.assertEquals("invalidClass", e.getCause().getMessage());
    }
  }

  @Test
  public void testCanNotAssignExtendedClass() {
    ConfigModel configModel = MicroserviceDefinition.createConfigModel("default", "invalidExtendedClass");
    @SuppressWarnings("unchecked")
    Map<String, Object> desc =
        (Map<String, Object>) configModel.getConfig().get(CONFIG_SERVICE_DESCRIPTION_KEY);
    desc.put("propertyExtentedClass", "java.lang.String");
    MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(Arrays.asList(configModel));
    try {
      microserviceFactory.create(microserviceDefinition);
      Assert.fail("Must throw exception");
    } catch (Error e) {
      Assert.assertEquals(
          "Define propertyExtendedClass java.lang.String in yaml, but not implement the interface PropertyExtended.",
          e.getMessage());
    }
  }

  @Test
  public void testMicroservicePropertiesLoader() throws Exception {
    Microservice microservice = ServiceRegistryFactory.createLocal().getMicroservice();
    Map<String, String> expectedMap = new HashMap<>();
    expectedMap.put("key1", "value1");
    expectedMap.put("key2", "value2");
    expectedMap.put("ek0", "ev0");
    Assert.assertEquals(expectedMap, microservice.getProperties());
  }

  @Test
  public void testInstancePropertiesLoader() {
    Microservice microservice = ServiceRegistryFactory.createLocal().getMicroservice();
    MicroserviceInstance instance = microservice.getIntance();
    Map<String, String> expectedMap = new HashMap<>();
    expectedMap.put("key0", "value0");
    expectedMap.put("ek0", "ev0");
    Assert.assertEquals(expectedMap, instance.getProperties());
  }

  @Test
  public void testExtendedClassCompatible() {
    Configuration configuration = new DynamicConfiguration();
    configuration.setProperty(CONFIG_SERVICE_DESCRIPTION_KEY + AbstractPropertiesLoader.EXTENDED_CLASS, "invalidClass");

    AbstractPropertiesLoader loader = MicroservicePropertiesLoader.INSTANCE;
    try {
      loader.loadProperties(configuration);
      Assert.fail("Must throw exception");
    } catch (Error e) {
      Assert.assertEquals(ClassNotFoundException.class, e.getCause().getClass());
      Assert.assertEquals("invalidClass", e.getCause().getMessage());
    }
  }
}
