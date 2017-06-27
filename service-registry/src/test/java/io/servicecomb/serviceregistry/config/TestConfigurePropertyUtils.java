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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.config.ConfigUtil;
import io.servicecomb.config.archaius.sources.MicroserviceConfigLoader;

public class TestConfigurePropertyUtils {
    @Test
    public void testGetPropertiesWithPrefix() {
        MicroserviceConfigLoader loader = new MicroserviceConfigLoader();
        loader.loadAndSort();
        Configuration configuration = ConfigUtil.createConfig(loader.getConfigModels());

        String prefix = "service_description.properties";
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("key1", "value1");
        expectedMap.put("key2", "value2");
        Assert.assertEquals(expectedMap, ConfigurePropertyUtils.getPropertiesWithPrefix(configuration, prefix));
    }
}
