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

package com.huawei.paas.cse.serviceregistry.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import com.huawei.paas.config.archaius.scheduler.NeverStartPollingScheduler;
import com.huawei.paas.config.archaius.sources.YAMLConfigurationSource;
import com.huawei.paas.cse.serviceregistry.RegistryUtils;
import com.huawei.paas.cse.serviceregistry.api.registry.Microservice;
import com.huawei.paas.cse.serviceregistry.api.registry.MicroserviceInstance;
import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicConfiguration;

public class TestPropertiesLoader {
    @BeforeClass
    public static void init() {
        ConcurrentCompositeConfiguration finalConfig = new ConcurrentCompositeConfiguration();
        YAMLConfigurationSource yamlConfigurationSource = new YAMLConfigurationSource();
        DynamicConfiguration configFromYamlFile =
            new DynamicConfiguration(yamlConfigurationSource, new NeverStartPollingScheduler());
        finalConfig.addConfiguration(configFromYamlFile, "configFromYamlFile");
        ConfigurationManager.install(finalConfig);
    }

    @Test
    public void testMergeStrings() {
        Assert.assertEquals("abc123efg", AbstractPropertiesLoader.mergeStrings("abc", "123", "efg"));
    }

    @Test
    public void testMicroservicePropertiesLoader() throws Exception {
        Method method = ReflectionUtils.findMethod(RegistryUtils.class, "createMicroserviceFromDefinition");
        ReflectionUtils.makeAccessible(method);
        Microservice microservice = (Microservice) method.invoke(null);
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("key1", "value1");
        expectedMap.put("key2", "value2");
        Assert.assertEquals(expectedMap, microservice.getProperties());
    }

    @Test
    public void testInstancePropertiesLoader() {
        MicroserviceInstance instance = RegistryUtils.getMicroserviceInstance();
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("key0", "value0");
        Assert.assertEquals(expectedMap, instance.getProperties());
    }

}
