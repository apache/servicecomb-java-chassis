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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class YAMLConfigLoader extends AbstractConfigLoader {
    protected String orderKey = "config-order";

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ConfigModel load(URL url) throws IOException {
        Yaml yaml = new Yaml();

        try (InputStream inputStream = url.openStream()) {
            ConfigModel configModel = new ConfigModel();
            configModel.setUrl(url);
            configModel.setConfig(yaml.loadAs(inputStream, Map.class));

            Object objOrder = configModel.getConfig().get(orderKey);
            if (objOrder != null) {
                if (Integer.class.isInstance(objOrder)) {
                    configModel.setOrder((int) objOrder);
                } else {
                    configModel.setOrder(Integer.parseInt(String.valueOf(objOrder)));
                }
            }
            return configModel;
        }
    }
}
