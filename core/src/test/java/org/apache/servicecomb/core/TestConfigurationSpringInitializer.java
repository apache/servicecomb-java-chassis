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
package org.apache.servicecomb.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.jndi.JndiPropertySource;

public class TestConfigurationSpringInitializer {
  @BeforeEach
  public void beforeTest() {
    Configurator.setRootLevel(Level.OFF);

    ConfigUtil.clearExtraConfig();
    ArchaiusUtils.resetConfig();

    Configurator.setRootLevel(Level.INFO);
  }

  @AfterEach
  public void afterTest() {
    ConfigUtil.clearExtraConfig();
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testSetEnvironment() {
    ConfigurableEnvironment environment = Mockito.mock(ConfigurableEnvironment.class);
    MutablePropertySources propertySources = new MutablePropertySources();
    Map<String, String> propertyMap = new HashMap<>();
    final String map0Key0 = "map0-Key0";
    final String map1Key0 = "map1-Key0";
    final String map2Key0 = "map2-Key0";
    final String map3Key0 = "map3-Key0";
    propertyMap.put(map0Key0, "map0-Value0");
    propertyMap.put(map1Key0, "map1-Value0");
    propertyMap.put(map2Key0, "map2-Value0");
    propertyMap.put(map3Key0, "map3-Value0");

    /*
    propertySources
    |- compositePropertySource0
    |  |- mapPropertySource0
    |  |  |- map0-Key0 = map0-Value0
    |  |- compositePropertySource1
    |     |- mapPropertySource1
    |     |  |- map1-Key0 = map1-Value0
    |     |- mapPropertySource2
    |        |- map2-Key0 = map2-Value0
    |     |- JndiPropertySource(mocked)
    |- mapPropertySource3
      |- map3-Key0 = map3-Value0
     */
    CompositePropertySource compositePropertySource0 = new CompositePropertySource("compositePropertySource0");
    propertySources.addFirst(compositePropertySource0);

    HashMap<String, Object> map0 = new HashMap<>();
    map0.put(map0Key0, propertyMap.get(map0Key0));
    MapPropertySource mapPropertySource0 = new MapPropertySource("mapPropertySource0", map0);
    compositePropertySource0.addFirstPropertySource(mapPropertySource0);

    CompositePropertySource compositePropertySource1 = new CompositePropertySource("compositePropertySource1");
    compositePropertySource0.addPropertySource(compositePropertySource1);
    HashMap<String, Object> map1 = new HashMap<>();
    map1.put(map1Key0, propertyMap.get(map1Key0));
    MapPropertySource mapPropertySource1 = new MapPropertySource("mapPropertySource1", map1);
    compositePropertySource1.addPropertySource(mapPropertySource1);
    HashMap<String, Object> map2 = new HashMap<>();
    map2.put(map2Key0, propertyMap.get(map2Key0));
    MapPropertySource mapPropertySource2 = new MapPropertySource("mapPropertySource2", map2);
    compositePropertySource1.addPropertySource(mapPropertySource2);
    compositePropertySource1.addPropertySource(Mockito.mock(JndiPropertySource.class));

    HashMap<String, Object> map3 = new HashMap<>();
    map3.put(map3Key0, propertyMap.get(map3Key0));
    MapPropertySource mapPropertySource3 = new MapPropertySource("mapPropertySource3", map3);
    compositePropertySource0.addPropertySource(mapPropertySource3);

    Mockito.when(environment.getPropertySources()).thenReturn(propertySources);
    Mockito.doAnswer((Answer<String>) invocation -> {
      Object[] args = invocation.getArguments();
      String propertyName = (String) args[0];

      if ("spring.config.name".equals(propertyName) || "spring.application.name".equals(propertyName)) {
        return null;
      }

      String value = propertyMap.get(propertyName);
      if (null == value) {
        Assertions.fail("get unexpected property name: " + propertyName);
      }
      return value;
    }).when(environment).getProperty(ArgumentMatchers.anyString(), ArgumentMatchers.eq(Object.class));
  }

  @Test
  public void testSetEnvironmentOnEnvironmentName() {
    // get environment name from spring.config.name
    ConfigurableEnvironment environment0 = Mockito.mock(ConfigurableEnvironment.class);
    MutablePropertySources propertySources0 = new MutablePropertySources();
    Mockito.when(environment0.getPropertySources()).thenReturn(propertySources0);
    Map<String, Object> map0 = new HashMap<>(1);
    map0.put("spring.config.name", "application");
    propertySources0.addFirst(new MapPropertySource("mapPropertySource0", map0));
    Mockito.when(environment0.getProperty("spring.config.name", Object.class)).thenReturn("application");
    Mockito.when(environment0.getProperty("spring.config.name")).thenReturn("application");

    // get environment name from spring.application.name
    ConfigurableEnvironment environment1 = Mockito.mock(ConfigurableEnvironment.class);
    MutablePropertySources propertySources1 = new MutablePropertySources();
    Mockito.when(environment1.getPropertySources()).thenReturn(propertySources1);
    Map<String, Object> map1 = new HashMap<>(1);
    map1.put("spring.application.name", "bootstrap");
    propertySources1.addFirst(new MapPropertySource("mapPropertySource1", map1));
    Mockito.when(environment1.getProperty("spring.application.name", Object.class)).thenReturn("bootstrap");
    Mockito.when(environment1.getProperty("spring.application.name")).thenReturn("bootstrap");

    // get environment name from className+hashcode
    ConfigurableEnvironment environment2 = Mockito.mock(ConfigurableEnvironment.class);
    MutablePropertySources propertySources2 = new MutablePropertySources();
    Mockito.when(environment2.getPropertySources()).thenReturn(propertySources2);
    Map<String, Object> map2 = new HashMap<>(1);
    map2.put("key2", "value2");
    propertySources2.addFirst(new MapPropertySource("mapPropertySource2", map2));
    Mockito.when(environment2.getProperty("key2", Object.class)).thenReturn("value2");
    Mockito.when(environment2.getProperty("key2")).thenReturn("value2");
  }
}
