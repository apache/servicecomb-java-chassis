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
import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.config.archaius.sources.ConfigModel;
import io.servicecomb.config.archaius.sources.MicroserviceConfigLoader;

public class TestConfigUtil {
    @Test
    public void testCreateDynamicConfig() {
        AbstractConfiguration dynamicConfig = ConfigUtil.createDynamicConfig();
        MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader(dynamicConfig);
        List<ConfigModel> list = loader.getConfigModels();
        Assert.assertEquals(loader, ConfigUtil.getMicroserviceConfigLoader(dynamicConfig));
        Assert.assertEquals(1, list.size());
        //        Assert.assertTrue(new File(list.get(0).getRootPath()).exists());
    }
}
