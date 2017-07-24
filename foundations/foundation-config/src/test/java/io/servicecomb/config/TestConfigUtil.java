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

import java.util.List;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicWatchedConfiguration;
import com.netflix.config.WatchedConfigurationSource;

import io.servicecomb.config.archaius.sources.ConfigModel;
import io.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import io.servicecomb.foundation.common.utils.SPIServiceUtils;
import mockit.Expectations;
import mockit.Mocked;

public class TestConfigUtil {
    @Test
    public void testCreateConfigFromConfigCenterNoUrl(@Mocked Configuration localConfiguration) {
        AbstractConfiguration configFromConfigCenter = ConfigUtil.createConfigFromConfigCenter(localConfiguration);
        Assert.assertNull(configFromConfigCenter);
    }

    @Test
    public void testCreateDynamicConfigNoConfigCenterSPI() {
        AbstractConfiguration dynamicConfig = ConfigUtil.createDynamicConfig();
        MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader(dynamicConfig);
        List<ConfigModel> list = loader.getConfigModels();
        Assert.assertEquals(loader, ConfigUtil.getMicroserviceConfigLoader(dynamicConfig));
        Assert.assertEquals(1, list.size());
        Assert.assertNotEquals(DynamicWatchedConfiguration.class,
                ((ConcurrentCompositeConfiguration) dynamicConfig).getConfiguration(0).getClass());

    }

    @Test
    public void testCreateDynamicConfigHasConfigCenter(
            @Mocked WatchedConfigurationSource watchedConfigurationSource) {
        new Expectations(SPIServiceUtils.class) {
            {
                SPIServiceUtils.getTargetService(WatchedConfigurationSource.class);
                result = watchedConfigurationSource;
            }
        };

        AbstractConfiguration dynamicConfig = ConfigUtil.createDynamicConfig();
        Assert.assertEquals(DynamicWatchedConfiguration.class,
                ((ConcurrentCompositeConfiguration) dynamicConfig).getConfiguration(0).getClass());
    }

    @Test
    public void testGetPropertyInvalidConfig() {
        Assert.assertNull(ConfigUtil.getProperty(null, "any"));
        Assert.assertNull(ConfigUtil.getProperty(new Object(), "any"));
    }
}
