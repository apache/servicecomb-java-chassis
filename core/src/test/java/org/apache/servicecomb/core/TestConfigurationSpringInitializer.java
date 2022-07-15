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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.jndi.JndiPropertySource;

import com.netflix.config.ConfigurationManager;

import mockit.Deencapsulation;

public class TestConfigurationSpringInitializer {
  @BeforeEach
  public void beforeTest() {
    Logger.getRootLogger().setLevel(Level.OFF);

    ConfigUtil.clearExtraConfig();
    ArchaiusUtils.resetConfig();

    Logger.getRootLogger().setLevel(Level.INFO);
  }

  @AfterEach
  public void afterTest() {
    ConfigUtil.clearExtraConfig();
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testAll() {
    ConfigurationSpringInitializer configurationSpringInitializer = new ConfigurationSpringInitializer();
    ConfigUtil.installDynamicConfig();

    Object o = ConfigUtil.getProperty("zq");
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> listO = (List<Map<String, Object>>) o;
    Assertions.assertEquals(3, listO.size());
    Assertions.assertNull(ConfigUtil.getProperty("notExist"));

    Configuration instance = ConfigurationManager.getConfigInstance();
    ConfigUtil.installDynamicConfig();
    // must not reinstall
    Assertions.assertEquals(instance, ConfigurationManager.getConfigInstance());
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

    new ConfigurationSpringInitializer().setEnvironment(environment);

    Map<String, Map<String, Object>> extraLocalConfig = getExtraConfigMapFromConfigUtil();
    Assertions.assertFalse(extraLocalConfig.isEmpty());
    Map<String, Object> extraProperties = extraLocalConfig
        .get(ConfigurationSpringInitializer.EXTRA_CONFIG_SOURCE_PREFIX + environment.getClass().getName() + "@"
            + environment.hashCode());
    Assertions.assertNotNull(extraLocalConfig);
    for (Entry<String, String> entry : propertyMap.entrySet()) {
      Assertions.assertEquals(entry.getValue(), extraProperties.get(entry.getKey()));
    }
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

    ConfigurationSpringInitializer configurationSpringInitializer = new ConfigurationSpringInitializer();
    configurationSpringInitializer.setEnvironment(environment0);
    configurationSpringInitializer.setEnvironment(environment1);
    configurationSpringInitializer.setEnvironment(environment2);

    Map<String, Map<String, Object>> extraConfig = getExtraConfigMapFromConfigUtil();
    Assertions.assertEquals(3, extraConfig.size());

    Map<String, Object> extraProperties = extraConfig
        .get(ConfigurationSpringInitializer.EXTRA_CONFIG_SOURCE_PREFIX + "application");
    Assertions.assertEquals(1, extraProperties.size());
    Assertions.assertEquals("application", extraProperties.get("spring.config.name"));

    extraProperties = extraConfig.get(ConfigurationSpringInitializer.EXTRA_CONFIG_SOURCE_PREFIX + "bootstrap");
    Assertions.assertEquals(1, extraProperties.size());
    Assertions.assertEquals("bootstrap", extraProperties.get("spring.application.name"));

    extraProperties = extraConfig.get(
        ConfigurationSpringInitializer.EXTRA_CONFIG_SOURCE_PREFIX + environment2.getClass().getName() + "@"
            + environment2.hashCode());
    Assertions.assertEquals(1, extraProperties.size());
    Assertions.assertEquals("value2", extraProperties.get("key2"));
  }

  @Test
  public void should_throw_exception_when_given_ignoreResolveFailure_false() {
    Assertions.assertThrows(RuntimeException.class, () -> {
      StandardEnvironment environment = newStandardEnvironment();

      ConfigurationSpringInitializer configurationSpringInitializer = new ConfigurationSpringInitializer();
      configurationSpringInitializer.setEnvironment(environment);
    });
  }

  private Map<String, Map<String, Object>> getExtraConfigMapFromConfigUtil() {
    return Deencapsulation
        .getField(ConfigUtil.class, "EXTRA_CONFIG_MAP");
  }

  private StandardEnvironment newStandardEnvironment() {
    Map<String, Object> envProperties = new HashMap<>();
    envProperties.put("IFS-X", "${IFS-X}");
    PropertySource<Map<String, Object>> systemEnvironmentPropertySource = new SystemEnvironmentPropertySource("system-env", envProperties);

    StandardEnvironment environment = new StandardEnvironment();
    environment.getPropertySources()
            .addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, systemEnvironmentPropertySource);
    return environment;
  }
}
