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
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.servicecomb.config.ConfigUtil;

public class MicroserviceConfigLoader extends YAMLConfigLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceConfigLoader.class);

    private static final String ADDITIONAL_CONFIG_URL = "cse.configurationSource.additionalUrls";

    /**
     * Default configuration file name to be used by default constructor. This file should
     * be on the classpath. The file name can be overridden by the value of system property
     * <code>configurationSource.defaultFileName</code>
     */
    private static final String DEFAULT_CONFIG_FILE_NAME = "microservice.yaml";

    public MicroserviceConfigLoader() {
        setOrderKey("cse-config-order");
    }

    public void loadAndSort() {
        try {
            String configFileFromClasspath =
                System.getProperty("cse.configurationSource.defaultFileName") == null ? DEFAULT_CONFIG_FILE_NAME
                        : System.getProperty("cse.configurationSource.defaultFileName");
            super.load(configFileFromClasspath);
            loadAdditionalConfig();

            if (configModelList.isEmpty()) {
                LOGGER.warn("No URLs will be polled as dynamic configuration sources.");
                LOGGER.info("To enable URLs as dynamic configuration sources, define System property "
                        + ADDITIONAL_CONFIG_URL + " or make " + configFileFromClasspath
                        + " available on classpath.");
            }

            sort();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load microservice config.", e);
        }
    }

    protected void loadAdditionalConfig() throws IOException {
        String strUrls = System.getProperty(ADDITIONAL_CONFIG_URL);
        if (StringUtils.isEmpty(strUrls)) {
            return;
        }

        for (String strUrl : strUrls.split(",")) {
            URL url = new URL(strUrl);
            ConfigModel configModel = load(url, null);
            ConfigUtil.setToAdditionalConfig(configModel);
            configModelList.add(configModel);
        }
    }
}
