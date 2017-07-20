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

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import java.util.List;
import java.util.Map;

import mockit.Deencapsulation;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

public class TestConfigurationSpringInitializer {
    @Test
    public void testAll() {
        new ConfigurationSpringInitializer();

        Object o = ConfigUtil.getProperty("zq");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> listO = (List<Map<String, Object>>) o;
        Assert.assertEquals(3, listO.size());
        Assert.assertEquals(null, ConfigUtil.getProperty("notExist"));
    }

//    @AfterClass
//    public static void tearDown() throws Exception {
//        Deencapsulation.setField(ConfigurationManager.class, "instance", null);
//        Deencapsulation.setField(ConfigurationManager.class, "customConfigurationInstalled", false);
//        Deencapsulation.setField(DynamicPropertyFactory.class, "config", null);
//    }
}
