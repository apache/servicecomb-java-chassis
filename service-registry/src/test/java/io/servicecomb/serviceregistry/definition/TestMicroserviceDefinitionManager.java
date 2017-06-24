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

package io.servicecomb.serviceregistry.definition;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.config.archaius.sources.ConfigModel;
import io.servicecomb.config.archaius.sources.MicroserviceConfigLoader;

public class TestMicroserviceDefinitionManager {
    private MicroserviceDefinitionManager createMicroserviceDefinitionManager(ConfigModel... configs) {
        MicroserviceConfigLoader loader = new MicroserviceConfigLoader();
        if (configs != null) {
            for (ConfigModel config : configs) {
                loader.getConfigModels().add(config);
            }
        }
        loader.loadAndSort();

        MicroserviceDefinitionManager microserviceDefinitionManager = new MicroserviceDefinitionManager();
        microserviceDefinitionManager.init(loader.getConfigModels());

        return microserviceDefinitionManager;
    }

    private ConfigModel createConfigModel(String app, String name) throws MalformedURLException {
        ConfigModel config = new ConfigModel();
        config.setUrl(new URL(String.format("jar:file:/file.jar!/%s/%s", app, name)));
        config.setConfig(new HashMap<>());
        config.getConfig().put(DefinitionConst.appIdKey, app);

        Map<String, Object> map = new HashMap<>();
        map.put(DefinitionConst.nameKey, name);
        config.getConfig().put(DefinitionConst.serviceDescriptionKey, map);

        return config;
    }

    @Test
    public void testDefaultAppId() {
        MicroserviceDefinitionManager microserviceDefinitionManager = createMicroserviceDefinitionManager();
        Assert.assertEquals("default", microserviceDefinitionManager.getAppId());
        //        Assert.assertEquals(new File(this.getClass().getClassLoader().getResource("").getPath()).getPath(),
        //                microserviceDefinitionManager.getDefinitionMap().get("default").getRootPath());
    }

    @Test
    public void testOneAppId() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL test1URL = classLoader.getResource("app1.yaml");
        System.setProperty("cse.configurationSource.additionalUrls", test1URL.toString());

        MicroserviceDefinitionManager microserviceDefinitionManager = createMicroserviceDefinitionManager();
        Assert.assertEquals("app1", microserviceDefinitionManager.getAppId());

        System.clearProperty("cse.configurationSource.additionalUrls");
    }

    @Test
    public void testMultiAppId() throws MalformedURLException {
        ConfigModel c1 = createConfigModel("app1", "ms1");
        ConfigModel c2 = createConfigModel("app2", "ms2");

        try {
            createMicroserviceDefinitionManager(c1, c2);
            Assert.assertEquals(1, 2);
        } catch (Throwable e) {
            Assert.assertEquals("Not allowed multiple appId in one process, but have app1 and app2.",
                    e.getMessage());
        }
    }

    @Test
    public void testDuplicateMicroserviceName() throws MalformedURLException {
        ConfigModel c1 = createConfigModel("app1", "${ms1}");
        c1.getConfig().put("ms1", "ms");

        ConfigModel c2 = createConfigModel("app1", "${ms2}");
        c2.getConfig().put("ms2", "ms");

        try {
            createMicroserviceDefinitionManager(c1, c2);
            Assert.fail("should throw exception");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Create from [default, ${ms1}, ${ms2}], but get [default, ms]",
                    e.getMessage());
        }
    }

    @Test
    public void testInvalidMicroserviceName() throws MalformedURLException {
        ConfigModel c1 = createConfigModel("app1", "${var}");

        try {
            createMicroserviceDefinitionManager(c1);
            Assert.assertEquals(1, 2);
        } catch (Throwable e) {
            Assert.assertEquals("MicroserviceName ${var} is invalid.",
                    e.getMessage());
        }
    }
}
