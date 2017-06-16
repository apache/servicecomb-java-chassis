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

package io.servicecomb.config.archaius.sources;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

public class TestMicroserviceConfigLoader {
    private ConfigModel createConfigModel(String protocol, int order, String file) throws MalformedURLException {
        ConfigModel configModel = new ConfigModel();
        configModel.setUrl(new URL(protocol, null, file));
        configModel.setOrder(order);
        return configModel;
    }

    @Test
    public void testSort() throws MalformedURLException {
        MicroserviceConfigLoader loader = new MicroserviceConfigLoader();

        loader.getConfigModelList().add(createConfigModel("file", 1, "f1"));
        loader.getConfigModelList().add(createConfigModel("jar", 1, "j1"));
        loader.getConfigModelList().add(createConfigModel("file", 0, "f2"));
        loader.getConfigModelList().add(createConfigModel("file", 0, "f3"));
        loader.getConfigModelList().add(createConfigModel("jar", 0, "j2"));
        loader.getConfigModelList().add(createConfigModel("file", 0, "f4"));
        loader.getConfigModelList().add(createConfigModel("jar", 0, "j3"));
        loader.getConfigModelList().add(createConfigModel("jar", 0, "j4"));

        loader.sort();

        StringBuilder sb = new StringBuilder();
        for (ConfigModel configModel : loader.getConfigModelList()) {
            sb.append(configModel.getUrl()).append(",");
        }
        Assert.assertEquals("jar:j2,jar:j3,jar:j4,jar:j1,file:f2,file:f3,file:f4,file:f1,", sb.toString());
    }
}
