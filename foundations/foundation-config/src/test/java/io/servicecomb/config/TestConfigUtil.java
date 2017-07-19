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

package io.servicecomb.config;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import io.servicecomb.config.archaius.sources.ConfigModel;
import io.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import java.util.List;
import mockit.Deencapsulation;
import org.apache.commons.configuration.AbstractConfiguration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestConfigUtil {

  private static final String systemPropertyName = "servicecomb.system.setting";
  private static final String systemExpected = uniquify("ran");

  private static final String environmentPropertyName = "servicecomb.environment.setting";
  private static final String environmentExpected = uniquify("ran");

  private final MapBasedConfigurationSource configurationSource = new MapBasedConfigurationSource();

  @BeforeClass
  public static void beforeTest() {
    System.setProperty(systemPropertyName, systemExpected);

    System.setProperty(environmentPropertyName, environmentExpected);

    ConfigUtil.installDynamicConfig();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    Deencapsulation.setField(ConfigurationManager.class, "instance", null);
    Deencapsulation.setField(ConfigurationManager.class, "customConfigurationInstalled", false);
    Deencapsulation.setField(DynamicPropertyFactory.class, "config", null);
  }

    @Test
    public void testCreateDynamicConfig() {
        AbstractConfiguration dynamicConfig = ConfigUtil.createDynamicConfig();
        MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader(dynamicConfig);
        List<ConfigModel> list = loader.getConfigModels();
        Assert.assertEquals(loader, ConfigUtil.getMicroserviceConfigLoader(dynamicConfig));
        Assert.assertEquals(1, list.size());
    }

  @Test
  public void propertiesFromFileIsDuplicatedToCse() throws Exception {
    String expected = "http://localhost:9411/api/v1/spans";

    assertThat(DynamicPropertyFactory
            .getInstance().getStringProperty("servicecomb.tracing.collector.address", null).get(),
        equalTo(expected));

    assertThat(DynamicPropertyFactory
            .getInstance().getStringProperty("cse.tracing.collector.address", null).get(),
        equalTo(expected));
  }

  @Test
  public void propertiesFromSystemIsDuplicatedToCse() throws Exception {
    assertThat(DynamicPropertyFactory
            .getInstance().getStringProperty(systemPropertyName, null).get(),
        equalTo(systemExpected));

    assertThat(DynamicPropertyFactory
            .getInstance().getStringProperty("cse.system.setting", null).get(),
        equalTo(systemExpected));
  }

  @Test
  public void propertiesFromEnvironmentIsDuplicatedToCse() throws Exception {
    assertThat(DynamicPropertyFactory
            .getInstance().getStringProperty(environmentPropertyName, null).get(),
        equalTo(environmentExpected));

    assertThat(DynamicPropertyFactory
            .getInstance().getStringProperty("cse.environment.setting", null).get(),
        equalTo(environmentExpected));
  }

  @Test
  public void propertiesAddFromDynamicConfigSourceIsDuplicated() throws Exception {
    String serviceCombProperty = "servicecomb.whatever.property.add";
    String cseProperty = "cse.whatever.property.add";

    String expected = uniquify("ran");

    configurationSource.addProperty(serviceCombProperty, expected);

    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(serviceCombProperty, null).get(),
        equalTo(expected));
    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(cseProperty, null).get(),
        equalTo(expected));

    String changed = uniquify("changed");
    configurationSource.addProperty(serviceCombProperty, changed);

    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(serviceCombProperty, null).get(),
        equalTo(changed));
    assertThat(DynamicPropertyFactory.getInstance().getStringProperty(cseProperty, null).get(),
        equalTo(changed));
  }

  @Test
  public void propertiesChangeFromDynamicConfigSourceIsDuplicated() throws Exception {
    String expected = uniquify("ran");
    String someProperty = "servicecomb.whatever.property.change";
    String injectProperty = "cse.whatever.property.change";
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

  }

  @Test
  public void propertiesDeleteFromDynamicConfigSourceIsDuplicated() throws Exception {
    String expected = uniquify("ran");
    String someProperty = "servicecomb.whatever.property.delete";
    String injectProperty = "cse.whatever.property.delete";
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
}
