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

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_KEY_SPLITER;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertyConverter;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.archaius.scheduler.NeverStartPollingScheduler;
import org.apache.servicecomb.config.archaius.sources.ConfigModel;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigurationSource;
import org.apache.servicecomb.config.event.DynamicConfigurationChangedEvent;
import org.apache.servicecomb.config.spi.ConfigCenterConfigurationSource;
import org.apache.servicecomb.config.spi.ConfigCenterConfigurationSourceLoader;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.WatchedUpdateListener;
import com.netflix.config.WatchedUpdateResult;

public final class ConfigUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);

  private static final String IS_PRINT_URL = "servicecomb.config.log.verbose";

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
    AbstractConfiguration config = ConfigurationManager.getConfigInstance();
    return getProperty(config, key);
  }

  public static Object getProperty(Object config, String key) {
    if (config instanceof Configuration) {
      Configuration configuration = (Configuration) config;
      return configuration.getProperty(key);
    }
    return null;
  }

  /**
   * get comma separated list values from yaml string
   */
  public static List<String> getStringList(@Nonnull Configuration config, @Nonnull String key) {
    return parseArrayValue(config.getString(key)).stream()
        .map(v -> Objects.toString(v, null))
        .collect(Collectors.toList());
  }

  public static List<String> parseArrayValue(String value) {
    return PropertyConverter.split(value, ',', true);
  }

  public static ConcurrentCompositeConfiguration createLocalConfig() {
    MicroserviceConfigLoader loader = new MicroserviceConfigLoader();
    loader.loadAndSort();
    if (localConfig.size() > 0) {
      ConfigModel model = new ConfigModel();
      model.setConfig(localConfig);
      loader.getConfigModels().add(model);
    }

    boolean isPrintUrl = DynamicPropertyFactory.getInstance()
        .getBooleanProperty(IS_PRINT_URL, true).get();
    if (isPrintUrl) {
      LOGGER.info("create local config from paths=[{}]", StringUtils.join(loader.getConfigModels(), ","));
    }

    ConcurrentCompositeConfiguration config = ConfigUtil.createLocalConfig(loader.getConfigModels());
    return config;
  }

  private static ConcurrentCompositeConfiguration createLocalConfig(List<ConfigModel> configModelList) {
    ConcurrentCompositeConfiguration config = new ConcurrentCompositeConfiguration();

    duplicateCseConfigToServicecomb(config,
        new ConcurrentMapConfigurationExt(new SystemConfiguration()),
        "configFromSystem");
    duplicateCseConfigToServicecomb(config,
        convertEnvVariable(new ConcurrentMapConfigurationExt(new EnvironmentConfiguration())),
        "configFromEnvironment");
    // If there is extra configurations, add it into config.
    EXTRA_CONFIG_MAP.entrySet()
        .stream()
        .filter(mapEntry -> !mapEntry.getValue().isEmpty())
        .forEachOrdered(configMapEntry -> duplicateCseConfigToServicecomb(config,
            new ConcurrentMapConfigurationExt(configMapEntry.getValue()),
            configMapEntry.getKey()));
    // we have already copy the cse config to the serviceComb config when we load the config from local yaml files
    // hence, we do not need duplicate copy it.
    config.addConfiguration(new DynamicConfigurationExt(
            new MicroserviceConfigurationSource(configModelList), new NeverStartPollingScheduler()),
        "configFromYamlFile");
    duplicateCseConfigToServicecombAtFront(config,
        new ConcurrentMapConfigurationExt(ConfigMapping.getConvertedMap(config)),
        "configFromMapping");
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

  private static void duplicateCseConfigToServicecomb(ConcurrentCompositeConfiguration compositeConfiguration,
      AbstractConfiguration source,
      String sourceName) {

    compositeConfiguration.addConfiguration(source, sourceName);
  }

  private static void duplicateCseConfigToServicecombAtFront(ConcurrentCompositeConfiguration compositeConfiguration,
      AbstractConfiguration source,
      String sourceName) {

    compositeConfiguration.addConfigurationAtFront(source, sourceName);
  }

  private static ConfigCenterConfigurationSource createConfigCenterConfigurationSource(
      Configuration localConfiguration) {
    ConfigCenterConfigurationSource configCenterConfigurationSource = ConfigCenterConfigurationSourceLoader
        .getConfigCenterConfigurationSource(localConfiguration);

    if (null == configCenterConfigurationSource) {
      LOGGER.info("none of config center source enabled.");
      return null;
    }

    LOGGER.info("use config center source {}", configCenterConfigurationSource.getClass().getName());
    return configCenterConfigurationSource;
  }

  private static void createDynamicWatchedConfiguration(
      ConcurrentCompositeConfiguration localConfiguration,
      ConfigCenterConfigurationSource configCenterConfigurationSource) {
    ConcurrentMapConfiguration injectConfig = new ConcurrentMapConfigurationExt();
    localConfiguration.addConfigurationAtFront(injectConfig, "extraInjectConfig");
    configCenterConfigurationSource.addUpdateListener(new ServiceCombPropertyUpdateListener(injectConfig));

    DynamicWatchedConfigurationExt configFromConfigCenter =
        new DynamicWatchedConfigurationExt(configCenterConfigurationSource);
    localConfiguration.addConfigurationAtFront(configFromConfigCenter, "configCenterConfig");
  }

  public static ConfigCenterConfigurationSource installDynamicConfig() {
    if (ConfigurationManager.isConfigurationInstalled()) {
      LOGGER.warn("Configuration installed by others, will ignore this configuration.");
      return null;
    }

    ConcurrentCompositeConfiguration compositeConfig = ConfigUtil.createLocalConfig();
    ConfigCenterConfigurationSource configCenterConfigurationSource =
        createConfigCenterConfigurationSource(compositeConfig);
    if (configCenterConfigurationSource != null) {
      // add listeners
      createDynamicWatchedConfiguration(compositeConfig, configCenterConfigurationSource);
      // then init data
      configCenterConfigurationSource.init(compositeConfig);
    }

    ConfigurationManager.install(compositeConfig);

    return configCenterConfigurationSource;
  }

  public static void destroyConfigCenterConfigurationSource() {
    SPIServiceUtils.getAllService(ConfigCenterConfigurationSource.class).forEach(source -> {
      try {
        source.destroy();
      } catch (Throwable e) {
        LOGGER.error("Failed to destroy {}", source.getClass().getName());
      }
    });
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
      EventManager.post(new DynamicConfigurationChangedEvent(watchedUpdateResult));
    }
  }

  @SuppressWarnings("unchecked")
  public static ConcurrentHashMap<String, DynamicProperty> getAllDynamicProperties() {
    try {
      return (ConcurrentHashMap<String, DynamicProperty>) FieldUtils
          .readDeclaredStaticField(DynamicProperty.class, "ALL_PROPS", true);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static CopyOnWriteArraySet<Runnable> getCallbacks(DynamicProperty property) {
    try {
      return (CopyOnWriteArraySet<Runnable>) FieldUtils.readDeclaredField(property, "callbacks", true);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  public static Set<String> propertiesWithPrefix(ConfigurableEnvironment environment, String prefix) {
    Set<String> result = new HashSet<>();
    for (PropertySource<?> propertySource : environment.getPropertySources()) {
      if (propertySource instanceof EnumerablePropertySource) {
        for (String key : ((EnumerablePropertySource<?>) propertySource).getPropertyNames()) {
          if (key.startsWith(prefix)) {
            result.add(key);
          }
        }
      }
    }
    return result;
  }
}
