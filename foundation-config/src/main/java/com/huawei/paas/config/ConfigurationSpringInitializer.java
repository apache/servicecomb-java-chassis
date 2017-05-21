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

package com.huawei.paas.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.huawei.paas.config.archaius.scheduler.NeverStartPollingScheduler;
import com.huawei.paas.config.archaius.sources.YAMLConfigurationSource;
import io.servicecomb.foundation.common.utils.SPIServiceUtils;
import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.DynamicWatchedConfiguration;
import com.netflix.config.PollResult;
import com.netflix.config.WatchedConfigurationSource;

public class ConfigurationSpringInitializer extends PropertyPlaceholderConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationSpringInitializer.class);

    private final String configCenterUrlKey = "cse.config.client.serverUri";

    public ConfigurationSpringInitializer() {
        if (ConfigurationManager.isConfigurationInstalled()) {
            LOGGER.warn("Configuration installed by others, will igore this configuration.");
        } else {
            YAMLConfigurationSource yamlConfigurationSource = new YAMLConfigurationSource();
            // configuration from yaml files: default microservice.yaml
            DynamicConfiguration configFromYamlFile =
                new DynamicConfiguration(yamlConfigurationSource, new NeverStartPollingScheduler());
            // configuration from system properties
            ConcurrentMapConfiguration configFromEnvProperties =
                new ConcurrentMapConfiguration(new EnvironmentConfiguration());

            ConcurrentMapConfiguration configFromSystemProperties =
                new ConcurrentMapConfiguration(new SystemConfiguration());
            // configuration from config center
            // Need to check whether the config center has been defined
            Map<String, Object> configMap;
            try {
                PollResult result = yamlConfigurationSource.poll(true, null);
                configMap = result.getComplete();
            } catch (Exception e) {
                configMap = new HashMap<String, Object>();
            }
            DynamicWatchedConfiguration configFromConfigCenter = null;
            if (configMap.get(configCenterUrlKey) != null) {
                WatchedConfigurationSource configCenterConfigurationSource =
                    SPIServiceUtils.getTargetService(WatchedConfigurationSource.class);
                if (null != configCenterConfigurationSource) {
                    // configuration from config center
                    configFromConfigCenter =
                        new DynamicWatchedConfiguration(configCenterConfigurationSource);
                } else {
                    LOGGER.info(
                            "config center SPI service can not find, skip to load configuration from config center");
                }
            } else {
                LOGGER.info("config center URL is missing, skip to load configuration from config center");
            }

            // create a hierarchy of configuration that makes
            // 1) dynamic configuration source override system properties
            ConcurrentCompositeConfiguration finalConfig = new ConcurrentCompositeConfiguration();
            if (configFromConfigCenter != null) {
                finalConfig.addConfiguration(configFromConfigCenter, "configCenterConfig");
            }
            finalConfig.addConfiguration(configFromSystemProperties, "systemConfig");
            finalConfig.addConfiguration(configFromEnvProperties, "systemEnvConfig");
            finalConfig.addConfiguration(configFromYamlFile, "configFromYamlFile");

            ConfigurationManager.install(finalConfig);
        }
    }
}
