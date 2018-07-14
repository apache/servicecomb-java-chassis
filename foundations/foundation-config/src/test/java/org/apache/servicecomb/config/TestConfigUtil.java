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

package org.apache.servicecomb.config;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.servicecomb.config.archaius.sources.ConfigModel;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.config.spi.ConfigCenterConfigurationSource;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicWatchedConfiguration;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;

public class TestConfigUtil {

  private static final String systemPropertyName = "servicecomb.cse.servicecomb.system.setting";

  private static final String systemExpected = uniquify("ran");

  private static final String environmentPropertyName = "servicecomb.cse.servicecomb.environment.setting";

  private static final String environmentExpected = uniquify("ran");

  private final MapBasedConfigurationSource configurationSource = new MapBasedConfigurationSource();

  @BeforeClass
  public static void beforeTest() {
    ArchaiusUtils.resetConfig();

    System.setProperty(systemPropertyName, systemExpected);
    try {
      setEnv(environmentPropertyName, environmentExpected);
      setEnv("MY_SERVICES_ENDPOINT", "https://myhost:8888");
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

    ConfigUtil.installDynamicConfig();
  }

  @AfterClass
  public static void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testAddConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put("service_description.name", "service_name_test");
    ConfigUtil.setConfigs(config);
    ConfigUtil.addConfig("service_description.version", "1.0.2");
    ConfigUtil.addConfig("cse.test.enabled", true);
    ConfigUtil.addConfig("cse.test.num", 10);
    AbstractConfiguration configuration = ConfigUtil.createDynamicConfig();
    Assert.assertEquals(configuration.getString("service_description.name"), "service_name_test");
    Assert.assertTrue(configuration.getBoolean("cse.test.enabled"));
    Assert.assertEquals(configuration.getInt("cse.test.num"), 10);
  }

  @Test
  public void testCreateDynamicConfigNoConfigCenterSPI() {
    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getTargetService(ConfigCenterConfigurationSource.class);
        result = null;
      }
    };

    AbstractConfiguration dynamicConfig = ConfigUtil.createDynamicConfig();
    MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader(dynamicConfig);
    List<ConfigModel> list = loader.getConfigModels();
    Assert.assertEquals(loader, ConfigUtil.getMicroserviceConfigLoader(dynamicConfig));
    Assert.assertEquals(1, list.size());
    Assert.assertNotEquals(DynamicWatchedConfiguration.class,
        ((ConcurrentCompositeConfiguration) dynamicConfig).getConfiguration(0).getClass());
  }

  @Test
  public void testCreateDynamicConfigHasConfigCenter() {
    AbstractConfiguration dynamicConfig = ConfigUtil.createDynamicConfig();
    Assert.assertEquals(DynamicWatchedConfiguration.class,
        ((ConcurrentCompositeConfiguration) dynamicConfig).getConfiguration(0).getClass());
  }

  @Test
  public void testGetPropertyInvalidConfig() {
    Assert.assertNull(ConfigUtil.getProperty(null, "any"));
    Assert.assertNull(ConfigUtil.getProperty(new Object(), "any"));
  }

  @Test
  public void propertiesFromFileIsDuplicatedToCse() {
    String expected = "value";

    assertThat(DynamicPropertyFactory
        .getInstance()
        .getStringProperty("cse.cse.servicecomb.file", null)
        .get(),
        equalTo(null));

    assertThat(DynamicPropertyFactory
        .getInstance()
        .getStringProperty("servicecomb.cse.servicecomb.file", null)
        .get(),
        equalTo(expected));
  }

  @Test
  public void propertiesFromSystemIsDuplicatedToCse() {
    assertThat(DynamicPropertyFactory
        .getInstance()
        .getStringProperty(systemPropertyName, null)
        .get(),
        equalTo(systemExpected));

    assertThat(DynamicPropertyFactory
        .getInstance()
        .getStringProperty("servicecomb.cse.servicecomb.system.setting", null)
        .get(),
        equalTo(systemExpected));
  }

  @Test
  public void propertiesFromEnvironmentIsDuplicatedToCse() {
    assertThat(DynamicPropertyFactory
        .getInstance()
        .getStringProperty(environmentPropertyName, null)
        .get(),
        equalTo(environmentExpected));

    assertThat(DynamicPropertyFactory
        .getInstance()
        .getStringProperty("servicecomb.cse.servicecomb.environment.setting", null)
        .get(),
        equalTo(environmentExpected));
  }

  @Test
  public void duplicateServiceCombConfigToCseListValue() {
    List<String> list = Arrays.asList("a", "b");

    AbstractConfiguration config = new DynamicConfiguration();
    config.addProperty("cse.list", list);
    Deencapsulation.invoke(ConfigUtil.class, "duplicateCseConfigToServicecomb", config);

    Object result = config.getProperty("servicecomb.list");
    assertThat(result, instanceOf(List.class));
    assertThat(result, equalTo(list));
  }

  @Test
  public void propertiesAddFromDynamicConfigSourceIsDuplicated() {
    String expected = uniquify("ran");
    String someProperty = "cse.cse.servicecomb.add";
    String injectProperty = "servicecomb.cse.servicecomb.add";

    configurationSource.addProperty(someProperty, expected);

    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(expected));
    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(expected));

    String changed = uniquify("changed");
    configurationSource.addProperty(someProperty, changed);

    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(changed));
    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(changed));

    expected = uniquify("ran");
    someProperty = "cse.servicecomb.cse.add";
    injectProperty = "servicecomb.servicecomb.cse.add";

    configurationSource.addProperty(someProperty, expected);

    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(expected));
    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(expected));

    changed = uniquify("changed");
    configurationSource.addProperty(someProperty, changed);

    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(changed));
    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(changed));
  }

  @Test
  public void propertiesChangeFromDynamicConfigSourceIsDuplicated() {
    String expected = uniquify("ran");
    String someProperty = "cse.cse.servicecomb.change";
    String injectProperty = "servicecomb.cse.servicecomb.change";
    configurationSource.addProperty(someProperty, expected);

    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(expected));
    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(expected));

    String changed = uniquify("changed");
    configurationSource.setProperty(someProperty, changed);

    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(changed));
    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(changed));

    expected = uniquify("ran");
    someProperty = "cse.servicecomb.cse.change";
    injectProperty = "servicecomb.servicecomb.cse.change";
    configurationSource.addProperty(someProperty, expected);
    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(expected));
    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(expected));

    changed = uniquify("changed");
    configurationSource.setProperty(someProperty, changed);

    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(changed));
    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(changed));
  }

  @Test
  public void propertiesDeleteFromDynamicConfigSourceIsDuplicated() {
    String expected = uniquify("ran");
    String someProperty = "cse.cse.servicecomb.delete";
    String injectProperty = "servicecomb.cse.servicecomb.delete";
    configurationSource.addProperty(someProperty, expected);

    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(expected));
    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(expected));

    configurationSource.deleteProperty(someProperty);

    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(null));
    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(null));

    expected = uniquify("ran");
    someProperty = "cse.servicecomb.cse.delete";
    injectProperty = "servicecomb.servicecomb.cse.delete";
    configurationSource.addProperty(someProperty, expected);

    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(expected));
    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(expected));

    configurationSource.deleteProperty(someProperty);

    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(null));
    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(null));
  }

  @Test
  public void testConvertEnvVariable() {
    String someProperty = "cse_service_registry_address";
    AbstractConfiguration config = new DynamicConfiguration();
    config.addProperty(someProperty, "testing");
    AbstractConfiguration result = ConfigUtil.convertEnvVariable(config);
    assertThat(result.getString("cse.service.registry.address"), equalTo("testing"));
    assertThat(result.getString("cse_service_registry_address"), equalTo("testing"));
  }

  @Test
  public void testCreateLocalConfigWithExtraConfig() {
    Map<String, Object> extraConfig = new ConcurrentHashMapEx<>(1);
    String extraConfigKey = "extraConfigKey";
    String extraConfigValue = "value";
    String overriddenConfigKey = "servicecomb.cse.servicecomb.file";
    extraConfig.put(extraConfigKey, extraConfigValue);
    final String propertyHigherPriority = "higher_priority";
    String mapedKey1 = "servicecomb.service.mapping.address";
    String mapedKey2 = "servicecomb.service1.mapping.address";
    extraConfig.put(overriddenConfigKey, propertyHigherPriority);

    ConfigUtil.addExtraConfig("testExtraConfig", extraConfig);

    ConcurrentCompositeConfiguration localConfiguration = ConfigUtil.createLocalConfig();

    Assert.assertEquals(extraConfigValue, localConfiguration.getProperty(extraConfigKey));
    Assert.assertEquals(propertyHigherPriority, localConfiguration.getString(overriddenConfigKey));
    // Test mapping key/value from self mappfing.xml
    Assert.assertEquals("https://myhost:8888", localConfiguration.getString(mapedKey1));
    Assert.assertEquals("https://myhost:8888", localConfiguration.getString(mapedKey2));
  }

  @SuppressWarnings("unchecked")
  private static void setEnv(String key, String value) throws IllegalAccessException, NoSuchFieldException {
    Class<?>[] classes = Collections.class.getDeclaredClasses();
    Map<String, String> env = System.getenv();
    for (Class<?> cl : classes) {
      if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
        Field field = cl.getDeclaredField("m");
        field.setAccessible(true);
        Object obj = field.get(env);
        Map<String, String> map = (Map<String, String>) obj;
        map.put(key, value);
      }
    }
  }

  @Test
  public void destroyConfigCenterConfigurationSource() {
    AtomicInteger count = new AtomicInteger();
    ConfigCenterConfigurationSource source = new MockUp<ConfigCenterConfigurationSource>() {
      @Mock
      void destroy() {
        count.incrementAndGet();
      }
    }.getMockInstance();

    new Expectations(SPIServiceUtils.class) {
      {
        SPIServiceUtils.getAllService(ConfigCenterConfigurationSource.class);
        result = Arrays.asList(source, source);
      }
    };

    ConfigUtil.destroyConfigCenterConfigurationSource();

    Assert.assertEquals(2, count.get());
  }
}
