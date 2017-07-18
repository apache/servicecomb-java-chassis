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

import static io.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_CSE_PREFIX;
import static io.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_SERVICECOMB_PREFIX;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicWatchedConfiguration;
import com.netflix.config.PollResult;
import com.netflix.config.PolledConfigurationSource;
import com.netflix.config.WatchedConfigurationSource;
import com.netflix.config.WatchedUpdateListener;
import com.netflix.config.WatchedUpdateResult;
import io.servicecomb.config.archaius.scheduler.NeverStartPollingScheduler;
import io.servicecomb.config.archaius.sources.ConfigModel;
import io.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import io.servicecomb.config.archaius.sources.MicroserviceConfigurationSource;
import io.servicecomb.foundation.common.utils.SPIServiceUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);

    private static final String configCenterUrlKey = "cse.config.client.serverUri";

    private static final String MICROSERVICE_CONFIG_LOADER_KEY = "cse-microservice-config-loader";

  private ConfigUtil() {
    }

    public static Object getProperty(String key) {
        Object config = DynamicPropertyFactory.getBackingConfigurationSource();
        return getProperty(config, key);
    }

    public static Object getProperty(Object config, String key) {
        if (null != config && Configuration.class.isInstance(config)) {
            Configuration configuration = (Configuration) config;
            return configuration.getProperty(key);
        }
        return null;
    }

    private static void setMicroserviceConfigLoader(Configuration config, MicroserviceConfigLoader loader) {
        config.setProperty(MICROSERVICE_CONFIG_LOADER_KEY, loader);
    }

    public static MicroserviceConfigLoader getMicroserviceConfigLoader() {
        return (MicroserviceConfigLoader) getProperty(MICROSERVICE_CONFIG_LOADER_KEY);
    }

    public static MicroserviceConfigLoader getMicroserviceConfigLoader(Configuration config) {
        return (MicroserviceConfigLoader) getProperty(config, MICROSERVICE_CONFIG_LOADER_KEY);
    }

    public static DynamicConfiguration createConfigFromYamlFile(List<ConfigModel> configModelList) {
        // configuration from yaml files: default microservice.yaml
        return new DynamicConfiguration(
                new MicroserviceConfigurationSource(configModelList),
                new NeverStartPollingScheduler());
    }

    public static AbstractConfiguration createConfig(List<ConfigModel> configModelList) {
        DynamicConfiguration configFromYamlFile = createConfigFromYamlFile(configModelList);
        return createConfig(configFromYamlFile, null);
    }

  public static AbstractConfiguration createConfig(DynamicConfiguration configFromYamlFile,
      DynamicWatchedConfiguration configFromConfigCenter) {
    // create a hierarchy of configuration that makes
    // 1) dynamic configuration source override system properties
    ConcurrentCompositeConfiguration config = new ConcurrentCompositeConfiguration();

    if (configFromConfigCenter != null) {
      ConcurrentMapConfiguration injectConfig = new ConcurrentMapConfiguration();

      duplicateServiceCombConfigToCse(config, configFromConfigCenter, "configCenterConfig");
      config.addConfiguration(injectConfig, "extraInjectConfig");

      configFromConfigCenter.getSource().addUpdateListener(new ServiceCombPropertyUpdateListener(injectConfig));
    }

    duplicateServiceCombConfigToCse(config, configFromYamlFile, "configFromYamlFile");

    duplicateServiceCombConfigToCse(config, new ConcurrentMapConfiguration(new SystemConfiguration()),
        "configFromSystem");

    duplicateServiceCombConfigToCse(config, new ConcurrentMapConfiguration(new EnvironmentConfiguration()),
        "configFromEnvironment");

    return config;
  }

  //inject a copy of cse.xxx for servicecomb.xxx
  private static void duplicateServiceCombConfigToCse(
      ConcurrentCompositeConfiguration compositeConfiguration,
      AbstractConfiguration source,
      String sourceName) {

    Map<String, String> injects = new HashMap<>();
    Iterator<String> keys = source.getKeys();
    while (keys.hasNext()) {
      String key = keys.next();
      if (key.startsWith(CONFIG_SERVICECOMB_PREFIX)) {
        injects.put(key, key.replace(CONFIG_SERVICECOMB_PREFIX, CONFIG_CSE_PREFIX));
      }
    }

    compositeConfiguration.addConfiguration(source, sourceName);

    for (String key : injects.keySet()) {
      source.addProperty(injects.get(key), source.getString(key));
    }
  }

    public static DynamicWatchedConfiguration createConfigFromConfigCenter(
            PolledConfigurationSource polledConfigurationSource) {
        // configuration from config center
        // Need to check whether the config center has been defined
        Map<String, Object> configMap;
        try {
            PollResult result = polledConfigurationSource.poll(true, null);
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
        return configFromConfigCenter;
    }

    public static AbstractConfiguration createDynamicConfig(MicroserviceConfigLoader loader) {
        LOGGER.info("create dynamic config:");
        for (ConfigModel configModel : loader.getConfigModels()) {
            LOGGER.info(" {}.", configModel.getUrl());
        }
        DynamicConfiguration configFromYamlFile = ConfigUtil.createConfigFromYamlFile(loader.getConfigModels());
        DynamicWatchedConfiguration configFromConfigCenter =
            createConfigFromConfigCenter(configFromYamlFile.getSource());
        return ConfigUtil.createConfig(configFromYamlFile, configFromConfigCenter);
    }

    public static AbstractConfiguration createDynamicConfig() {
        MicroserviceConfigLoader loader = new MicroserviceConfigLoader();
        loader.loadAndSort();

        AbstractConfiguration dynamicConfig = createDynamicConfig(loader);
        ConfigUtil.setMicroserviceConfigLoader(dynamicConfig, loader);

        return dynamicConfig;
    }

    public static void installDynamicConfig() {
        if (ConfigurationManager.isConfigurationInstalled()) {
            LOGGER.warn("Configuration installed by others, will ignore this configuration.");
            return;
        }

        AbstractConfiguration dynamicConfig = ConfigUtil.createDynamicConfig();
        ConfigurationManager.install(dynamicConfig);
    }

  private static class ServiceCombPropertyUpdateListener implements WatchedUpdateListener {

    private final ConcurrentMapConfiguration injectConfig;

    ServiceCombPropertyUpdateListener(ConcurrentMapConfiguration injectConfig) {
      this.injectConfig = injectConfig;
    }

    @Override
    public void updateConfiguration(WatchedUpdateResult watchedUpdateResult) {
      Map<String, Object> adds = watchedUpdateResult.getAdded();
      if (adds != null) {
        for (String add : adds.keySet()) {
          if (add.startsWith(CONFIG_SERVICECOMB_PREFIX)) {
            String key = add.replace(CONFIG_SERVICECOMB_PREFIX, CONFIG_CSE_PREFIX);
            injectConfig.addProperty(key, adds.get(add));
          }
        }
      }

      Map<String, Object> deletes = watchedUpdateResult.getDeleted();
      if (deletes != null) {
        for (String delete : deletes.keySet()) {
          if (delete.startsWith(CONFIG_SERVICECOMB_PREFIX)) {
            injectConfig.clearProperty(delete.replace(CONFIG_SERVICECOMB_PREFIX, CONFIG_CSE_PREFIX));
          }
        }
      }

      Map<String, Object> changes = watchedUpdateResult.getChanged();
      if (changes != null) {
        for (String change : changes.keySet()) {
          if (change.startsWith(CONFIG_SERVICECOMB_PREFIX)) {
            String key = change.replace(CONFIG_SERVICECOMB_PREFIX, CONFIG_CSE_PREFIX);
            injectConfig.setProperty(key, changes.get(change));
          }
        }
      }
    }
  }
}
