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

package org.apache.servicecomb.serviceregistry.api.registry;

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_DESCRIPTION_KEY;
import static org.apache.servicecomb.serviceregistry.definition.DefinitionConst.CONFIG_ALLOW_CROSS_APP_KEY;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceDefinition;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import mockit.Deencapsulation;

public class TestMicroserviceFactory {
  @Test
  public void testAllowCrossApp() {
    MicroserviceFactory factory = new MicroserviceFactory();
    Map<String, String> propertiesMap = new HashMap<>();
    Assert.assertFalse(Deencapsulation.invoke(factory, "allowCrossApp", propertiesMap));

    propertiesMap.put(CONFIG_ALLOW_CROSS_APP_KEY, "true");
    Assert.assertTrue(Deencapsulation.invoke(factory, "allowCrossApp", propertiesMap));

    propertiesMap.put(CONFIG_ALLOW_CROSS_APP_KEY, "false");
    Assert.assertFalse(Deencapsulation.invoke(factory, "allowCrossApp", propertiesMap));

    propertiesMap.put(CONFIG_ALLOW_CROSS_APP_KEY, "asfas");
    Assert.assertFalse(Deencapsulation.invoke(factory, "allowCrossApp", propertiesMap));
  }

  @Test
  public void testInit() {
    MicroserviceConfigLoader loader = new MicroserviceConfigLoader();
    loader.loadAndSort();

    MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(loader.getConfigModels());
    MicroserviceFactory factory = new MicroserviceFactory();
    Microservice microservice = factory.create(microserviceDefinition);

    String microserviceName = "default";

    Assert.assertEquals(microserviceName, microservice.getServiceName());
  }

  @Test
  public void testSetDescription() {
    Microservice microservice = new Microservice();
    MicroserviceFactory factory = new MicroserviceFactory();
    Configuration configuration = Mockito.mock(Configuration.class);

    Mockito.when(configuration.getStringArray(CONFIG_QUALIFIED_MICROSERVICE_DESCRIPTION_KEY))
        .thenReturn(new String[] {"test1", "test2"});

    Deencapsulation.invoke(factory, "setDescription", configuration, microservice);

    Assert.assertEquals("test1,test2", microservice.getDescription());
  }

  @Test
  public void testSetDescriptionOnNullDescription() {
    Microservice microservice = new Microservice();
    MicroserviceFactory factory = new MicroserviceFactory();
    Configuration configuration = Mockito.mock(Configuration.class);

    Mockito.when(configuration.getStringArray(CONFIG_QUALIFIED_MICROSERVICE_DESCRIPTION_KEY))
        .thenReturn(null);

    Deencapsulation.invoke(factory, "setDescription", configuration, microservice);

    Assert.assertNull(microservice.getDescription());

    Mockito.when(configuration.getStringArray(CONFIG_QUALIFIED_MICROSERVICE_DESCRIPTION_KEY))
        .thenReturn(new String[] {});

    Deencapsulation.invoke(factory, "setDescription", configuration, microservice);

    Assert.assertNull(microservice.getDescription());
  }

  @Test
  public void testSetDescriptionOnEmptyDescription() {
    Microservice microservice = new Microservice();
    MicroserviceFactory factory = new MicroserviceFactory();
    Configuration configuration = Mockito.mock(Configuration.class);

    Mockito.when(configuration.getStringArray(CONFIG_QUALIFIED_MICROSERVICE_DESCRIPTION_KEY))
        .thenReturn(new String[] {"", ""});

    Deencapsulation.invoke(factory, "setDescription", configuration, microservice);

    Assert.assertEquals(",", microservice.getDescription());
  }

  @Test
  public void testSetDescriptionOnBlankDescription() {
    Microservice microservice = new Microservice();
    MicroserviceFactory factory = new MicroserviceFactory();
    Configuration configuration = Mockito.mock(Configuration.class);

    Mockito.when(configuration.getStringArray(CONFIG_QUALIFIED_MICROSERVICE_DESCRIPTION_KEY))
        .thenReturn(new String[] {" ", " "});

    Deencapsulation.invoke(factory, "setDescription", configuration, microservice);

    Assert.assertEquals(" , ", microservice.getDescription());
  }
}
