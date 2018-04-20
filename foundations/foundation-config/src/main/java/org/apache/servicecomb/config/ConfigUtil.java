/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.config;

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_CSE_PREFIX;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_KEY_SPLITER;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_SERVICECOMB_PREFIX;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.servicecomb.config.archaius.scheduler.NeverStartPollingScheduler;
import org.apache.servicecomb.config.archaius.sources.ConfigModel;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigurationSource;
import org.apache.servicecomb.config.spi.ConfigCenterConfigurationSource;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicWatchedConfiguration;
import com.netflix.config.WatchedUpdateListener;
import com.netflix.config.WatchedUpdateResult;

public final class ConfigUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);

  private static final String MICROSERVICE_CONFIG_LOADER_KEY = "cse-microservice-config-loader";

  private static Map<String, Object> localConfig = new HashMap<>();

  /**
   * <p>The configurations not read by ServiceComb.</p>
   * <p>
   * For example, this map can store the configurations read by SpringBoot from application.properties,
   * If users write the configurations of ServiceComb into application.yml instead of microservice.yaml,
   * this can help {@link ConfigUtil} load config correctly.
   * </p>
   */
  private static final Map<String, Map<String, Object>> EXTRA_CONFIG_MAP = new LinkedHashMap<>();

  private ConfigUtil() {
  }

  public static void setConfigs(Map<String, Object> config) {
    localConfig = config;
  }

  public static void addConfig(String key, Object value) {
    localConfig.put(key, value);
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

  public static ConcurrentCompositeConfiguration createLocalConfig() {
    MicroserviceConfigLoader loader = new MicroserviceConfigLoader();
    loader.loadAndSort();
    if (localConfig.size() > 0) {
      ConfigModel model = new ConfigModel();
      model.setConfig(localConfig);
      loader.getConfigModels().add(model);
    }

    LOGGER.info("create local config:");
    for (ConfigModel configModel : loader.getConfigModels()) {
      LOGGER.info(" {}.", configModel.getUrl());
    }

    ConcurrentCompositeConfiguration config = ConfigUtil.createLocalConfig(loader.getConfigModels());
    ConfigUtil.setMicroserviceConfigLoader(config, loader);
    return config;
  }

  public static ConcurrentCompositeConfiguration createLocalConfig(List<ConfigModel> configModelList) {
    ConcurrentCompositeConfiguration config = new ConcurrentCompositeConfiguration();

    duplicateServiceCombConfigToCse(config,
        new ConcurrentMapConfiguration(new SystemConfiguration()),
        "configFromSystem");
    duplicateServiceCombConfigToCse(config,
        convertEnvVariable(new ConcurrentMapConfiguration(new EnvironmentConfiguration())),
        "configFromEnvironment");
    duplicateServiceCombConfigToCse(config,
        new DynamicConfiguration(
            new MicroserviceConfigurationSource(configModelList), new NeverStartPollingScheduler()),
        "configFromYamlFile");
    // If there is extra configurations, add it into config. Extra config has lowest priority.
    EXTRA_CONFIG_MAP.entrySet().stream()
        .filter(mapEntry -> !mapEntry.getValue().isEmpty())
        .forEachOrdered(configMapEntry ->
            duplicateServiceCombConfigToCse(config,
                new ConcurrentMapConfiguration(configMapEntry.getValue()),
                configMapEntry.getKey()));

    return config;
  }

  public static AbstractConfiguration convertEnvVariable(AbstractConfiguration source) {
    Iterator<String> keys = source.getKeys();
    while (keys.hasNext()) {
      String key = keys.next();
      String[] separatedKey = key.split(CONFIG_KEY_SPLITER);
      if (separatedKey.length == 1) {
        continue;
      }
      String newKey = String.join(".", separatedKey);
      source.addProperty(newKey, source.getProperty(key));
    }
    return source;
  }

  //inject a copy of cse.xxx for servicecomb.xxx
  private static void duplicateServiceCombConfigToCse(AbstractConfiguration source) {
    Iterator<String> keys = source.getKeys();
    while (keys.hasNext()) {
      String key = keys.next();
      if (!key.startsWith(CONFIG_SERVICECOMB_PREFIX)) {
        continue;
      }

      String cseKey = CONFIG_CSE_PREFIX + key.substring(key.indexOf(".") + 1);
      source.addProperty(cseKey, source.getProperty(key));
    }
  }

  private static void duplicateServiceCombConfigToCse(ConcurrentCompositeConfiguration compositeConfiguration,
      AbstractConfiguration source,
      String sourceName) {
    duplicateServiceCombConfigToCse(source);

    compositeConfiguration.addConfiguration(source, sourceName);
  }

  public static DynamicWatchedConfiguration createConfigFromConfigCenter(Configuration localConfiguration) {
    ConfigCenterConfigurationSource configCenterConfigurationSource =
        SPIServiceUtils.getTargetService(ConfigCenterConfigurationSource.class);
    if (null == configCenterConfigurationSource) {
      LOGGER.info(
          "config center SPI service can not find, skip to load configuration from config center");
      return null;
    }

    if (!configCenterConfigurationSource.isValidSource(localConfiguration)) {
      LOGGER.info("Config Source serverUri is not correctly configured.");
      return null;
    }

    configCenterConfigurationSource.init(localConfiguration);
    return new DynamicWatchedConfiguration(configCenterConfigurationSource);
  }

  public static AbstractConfiguration createDynamicConfig() {
    LOGGER.info("create dynamic config:");
    ConcurrentCompositeConfiguration config = ConfigUtil.createLocalConfig();
    DynamicWatchedConfiguration configFromConfigCenter = createConfigFromConfigCenter(config);
    if (configFromConfigCenter != null) {
      ConcurrentMapConfiguration injectConfig = new ConcurrentMapConfiguration();
      config.addConfigurationAtFront(injectConfig, "extraInjectConfig");

      duplicateServiceCombConfigToCse(configFromConfigCenter);
      config.addConfigurationAtFront(configFromConfigCenter, "configCenterConfig");

      configFromConfigCenter.getSource().addUpdateListener(new ServiceCombPropertyUpdateListener(injectConfig));
    }

    return config;
  }

  public static void installDynamicConfig() {
    if (ConfigurationManager.isConfigurationInstalled()) {
      LOGGER.warn("Configuration installed by others, will ignore this configuration.");
      return;
    }

    AbstractConfiguration dynamicConfig = ConfigUtil.createDynamicConfig();
    ConfigurationManager.install(dynamicConfig);
  }

  public static void addExtraConfig(String extraConfigName, Map<String, Object> extraConfig) {
    EXTRA_CONFIG_MAP.put(extraConfigName, extraConfig);
  }

  public static void clearExtraConfig() {
    EXTRA_CONFIG_MAP.clear();
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
            String key = CONFIG_CSE_PREFIX + add.substring(add.indexOf(".") + 1);
            injectConfig.addProperty(key, adds.get(add));
          }
        }
      }

      Map<String, Object> deletes = watchedUpdateResult.getDeleted();
      if (deletes != null) {
        for (String delete : deletes.keySet()) {
          if (delete.startsWith(CONFIG_SERVICECOMB_PREFIX)) {
            injectConfig.clearProperty(CONFIG_CSE_PREFIX + delete.substring(delete.indexOf(".") + 1));
          }
        }
      }

      Map<String, Object> changes = watchedUpdateResult.getChanged();
      if (changes != null) {
        for (String change : changes.keySet()) {
          if (change.startsWith(CONFIG_SERVICECOMB_PREFIX)) {
            String key = CONFIG_CSE_PREFIX + change.substring(change.indexOf(".") + 1);
            injectConfig.setProperty(key, changes.get(change));
          }
        }
      }
    }
  }
}
