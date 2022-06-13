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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.servicecomb.config.spi.ConfigCenterConfigurationSource;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.hamcrest.MatcherAssert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicWatchedConfiguration;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.Assertions;

public class TestConfigUtil {
  private static final String systemPropertyName = "servicecomb.cse.servicecomb.system.setting";

  private static final String systemExpected = uniquify("ran");

  private static final String environmentPropertyName = "servicecomb.cse.servicecomb.environment.setting";

  private static final String environmentExpected = uniquify("ran");

  private final MapBasedConfigurationSource configurationSource = new MapBasedConfigurationSource();

  @BeforeClass
  public static void beforeTest() {
    Logger.getRootLogger().setLevel(Level.OFF);

    ArchaiusUtils.resetConfig();

    System.setProperty(systemPropertyName, systemExpected);
    try {
      setEnv(environmentPropertyName, environmentExpected);
      setEnv("MY_SERVICES_ENDPOINT", "https://myhost:8888");
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

    ConfigUtil.installDynamicConfig();

    Logger.getRootLogger().setLevel(Level.INFO);
  }

  @AfterClass
  public static void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void testArrayData() {
    Configuration configuration = ConfigUtil.createLocalConfig();
    Assertions.assertEquals("a,b,c", configuration.getString("test.commonSeparatedString"));
    Assertions.assertEquals(1, configuration.getStringArray("test.commonSeparatedString").length);
    Assertions.assertEquals("a,b,c", configuration.getStringArray("test.commonSeparatedString")[0]);

    Assertions.assertEquals("b,c,d", configuration.getString("test.commonSeparatedStringHolder"));
    Assertions.assertEquals(1, configuration.getStringArray("test.commonSeparatedStringHolder").length);
    Assertions.assertEquals("b,c,d", configuration.getStringArray("test.commonSeparatedStringHolder")[0]);

    Assertions.assertEquals("m", configuration.getString("test.stringArray")); // first element
    Assertions.assertEquals(2, configuration.getStringArray("test.stringArray").length);
    Assertions.assertEquals("m", configuration.getStringArray("test.stringArray")[0]);
    Assertions.assertEquals("n", configuration.getStringArray("test.stringArray")[1]);
  }

  @Test
  public void testAddConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put("service_description.name", "service_name_test");
    ConfigUtil.setConfigs(config);
    ConfigUtil.addConfig("service_description.version", "1.0.2");
    ConfigUtil.addConfig("cse.test.enabled", true);
    ConfigUtil.addConfig("cse.test.num", 10);
    AbstractConfiguration configuration = ConfigUtil.createLocalConfig();
    Assertions.assertEquals(configuration.getString("service_description.name"), "service_name_test");
    Assertions.assertTrue(configuration.getBoolean("cse.test.enabled"));
    Assertions.assertEquals(configuration.getInt("cse.test.num"), 10);
  }

  @Test
  public void testCreateDynamicConfigNoConfigCenterSPI() {
    AbstractConfiguration dynamicConfig = ConfigUtil.createLocalConfig();
    Assertions.assertNotEquals(DynamicWatchedConfiguration.class,
        ((ConcurrentCompositeConfiguration) dynamicConfig).getConfiguration(0).getClass());
  }

  @Test
  public void testGetPropertyInvalidConfig() {
    Assertions.assertNull(ConfigUtil.getProperty(null, "any"));
    Assertions.assertNull(ConfigUtil.getProperty(new Object(), "any"));
  }

  @Test
  public void propertiesFromFileIsDuplicatedToCse() {
    String expected = "value";

    Assertions.assertNull(DynamicPropertyFactory
            .getInstance()
            .getStringProperty("cse.cse.servicecomb.file", null)
            .get());

    Assertions.assertEquals(expected, DynamicPropertyFactory
            .getInstance()
            .getStringProperty("servicecomb.cse.servicecomb.file", null)
            .get());
  }

  @Test
  public void propertiesFromSystemIsDuplicatedToCse() {
    MatcherAssert.assertThat(DynamicPropertyFactory
            .getInstance()
            .getStringProperty(systemPropertyName, null)
            .get(),
        equalTo(systemExpected));

    MatcherAssert.assertThat(DynamicPropertyFactory
            .getInstance()
            .getStringProperty("servicecomb.cse.servicecomb.system.setting", null)
            .get(),
        equalTo(systemExpected));
  }

  @Test
  public void propertiesFromEnvironmentIsDuplicatedToCse() {
    MatcherAssert.assertThat(DynamicPropertyFactory
            .getInstance()
            .getStringProperty(environmentPropertyName, null)
            .get(),
        equalTo(environmentExpected));

    MatcherAssert.assertThat(DynamicPropertyFactory
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
    ConfigUtil.duplicateCseConfigToServicecomb(config);

    Object result = config.getProperty("servicecomb.list");
    MatcherAssert.assertThat(result, instanceOf(List.class));
    MatcherAssert.assertThat(result, equalTo(list));
  }

  @Test
  public void propertiesAddFromDynamicConfigSourceIsDuplicated() {
    String expected = uniquify("ran");
    String someProperty = "cse.cse.servicecomb.add";
    String injectProperty = "servicecomb.cse.servicecomb.add";

    configurationSource.addProperty(someProperty, expected);

    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(expected));
    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(expected));

    String changed = uniquify("changed");
    configurationSource.addProperty(someProperty, changed);

    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(changed));
    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(changed));

    expected = uniquify("ran");
    someProperty = "cse.servicecomb.cse.add";
    injectProperty = "servicecomb.servicecomb.cse.add";

    configurationSource.addProperty(someProperty, expected);

    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(expected));
    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(expected));

    changed = uniquify("changed");
    configurationSource.addProperty(someProperty, changed);

    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(changed));
    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(changed));
  }

  @Test
  public void propertiesChangeFromDynamicConfigSourceIsDuplicated() {
    String expected = uniquify("ran");
    String someProperty = "cse.cse.servicecomb.change";
    String injectProperty = "servicecomb.cse.servicecomb.change";
    configurationSource.addProperty(someProperty, expected);

    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(expected));
    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(expected));

    String changed = uniquify("changed");
    configurationSource.setProperty(someProperty, changed);

    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(changed));
    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(changed));

    expected = uniquify("ran");
    someProperty = "cse.servicecomb.cse.change";
    injectProperty = "servicecomb.servicecomb.cse.change";
    configurationSource.addProperty(someProperty, expected);
    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(expected));
    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(expected));

    changed = uniquify("changed");
    configurationSource.setProperty(someProperty, changed);

    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(changed));
    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(changed));
  }

  @Test
  public void propertiesDeleteFromDynamicConfigSourceIsDuplicated() {
    String expected = uniquify("ran");
    String someProperty = "cse.cse.servicecomb.delete";
    String injectProperty = "servicecomb.cse.servicecomb.delete";
    configurationSource.addProperty(someProperty, expected);

    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(expected));
    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(expected));

    configurationSource.deleteProperty(someProperty);

    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(null));
    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(null));

    expected = uniquify("ran");
    someProperty = "cse.servicecomb.cse.delete";
    injectProperty = "servicecomb.servicecomb.cse.delete";
    configurationSource.addProperty(someProperty, expected);

    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(expected));
    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(expected));

    configurationSource.deleteProperty(someProperty);

    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(someProperty, null).get(),
        equalTo(null));
    MatcherAssert.assertThat(DynamicPropertyFactory.getInstance().getStringProperty(injectProperty, null).get(),
        equalTo(null));
  }

  @Test
  public void testConvertEnvVariable() {
    String someProperty = "cse_service_registry_address";
    AbstractConfiguration config = new DynamicConfiguration();
    config.addProperty(someProperty, "testing");
    AbstractConfiguration result = ConfigUtil.convertEnvVariable(config);
    Assertions.assertEquals("testing", result.getString("cse.service.registry.address"));
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

    Assertions.assertEquals(extraConfigValue, localConfiguration.getProperty(extraConfigKey));
    Assertions.assertEquals(propertyHigherPriority, localConfiguration.getString(overriddenConfigKey));
    // Test mapping key/value from self mappfing.xml
    Assertions.assertEquals("https://myhost:8888", localConfiguration.getString(mapedKey1));
    Assertions.assertEquals("https://myhost:8888", localConfiguration.getString(mapedKey2));
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

    Assertions.assertEquals(2, count.get());
  }
}
