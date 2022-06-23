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

package org.apache.servicecomb.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.ConfigMapping;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.YAMLUtil;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.config.event.DynamicConfigurationChangedEvent;
import org.apache.servicecomb.config.event.RefreshGovernanceConfigurationEvent;
import org.apache.servicecomb.config.spi.ConfigCenterConfigurationSource;
import org.apache.servicecomb.foundation.bootstrap.BootStrapService;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;


import com.google.common.eventbus.Subscribe;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.WatchedUpdateResult;

/**
 *  Adapt spring PropertySource and Archaius Configuration
 *  spring     vs      archaius
 *        (add)     |      dynamic(configcenter)
 *  system property           |      system property
 *  environment               |      environment
 *  *properties/*.yml        |       (add)
 *       (add)                |      microservice.yaml
 *
 *  add dynamic configuration, microserive.yaml to spring, add *properties/*.yml to archaius
 *
 *  NOTICE: we are not duplicate spring system property and environment property source, this will cause some problem
 *  related to precedence of a KEY-VAlUE. That is cse.test in dynamic config may not override servicecomb.test in yml.
 *  Users need to use the same key as what is in config file to override.
 */
public class ConfigurationSpringInitializer extends PropertySourcesPlaceholderConfigurer implements EnvironmentAware {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationSpringInitializer.class);

  public static final String EXTRA_CONFIG_SOURCE_PREFIX = "extraConfig-";

  public static final String MICROSERVICE_PROPERTY_SOURCE_NAME = "microservice.yaml";

  public static final String MAPPING_PROPERTY_SOURCE_NAME = "mapping.yaml";

  public static final String EXTERNAL_INIT = "scb-config-external-init";

  public static void setExternalInit(boolean value) {
    System.setProperty(EXTERNAL_INIT, String.valueOf(value));
  }

  public static boolean isExternalInit() {
    return Boolean.getBoolean(EXTERNAL_INIT);
  }

  private final List<BootStrapService> bootStrapServices = SPIServiceUtils.getSortedService(BootStrapService.class);

  private final Map<String, Object> dynamicData = new ConcurrentHashMap<>();

  private ConfigCenterConfigurationSource configCenterConfigurationSource;

  public ConfigurationSpringInitializer() {
    setOrder(Ordered.LOWEST_PRECEDENCE / 2);
    setIgnoreUnresolvablePlaceholders(true);
  }

  /**
   * Get configurations from Spring, merge them into the configurations of ServiceComb.
   * @param environment From which to get the extra config.
   */
  @Override
  public void setEnvironment(Environment environment) {
    super.setEnvironment(environment);
    if (isExternalInit()) {
      return;
    }

    syncFromSpring(environment);
    syncToSpring(environment);

    startupBootStrapService(environment);

    // watch configuration changes
    EventManager.register(this);
    configCenterConfigurationSource = ConfigUtil.installDynamicConfig();
    addDynamicConfigurationToSpring(environment, configCenterConfigurationSource);
  }

  @Subscribe
  public void onConfigurationDataChanged(DynamicConfigurationChangedEvent event) {
    try {
      WatchedUpdateResult data = event.getEvent();
      if (data.getDeleted() != null) {
        data.getDeleted().forEach((k, v) -> dynamicData.remove(k));
      }
      if (data.getAdded() != null) {
        dynamicData.putAll(data.getAdded());
      }
      if (data.getChanged() != null) {
        dynamicData.putAll(data.getChanged());
      }
    } catch (Exception e) {
      LOGGER.error("", e);
    }
    EventManager.post(new RefreshGovernanceConfigurationEvent(event.getEvent()));
  }

  private void syncFromSpring(Environment environment) {
    String environmentName = generateNameForEnvironment(environment);
    LOGGER.info("Environment received, will get configurations from [{}].", environmentName);

    Map<String, Object> extraConfig = getAllProperties(environment);
    ConfigUtil.addExtraConfig(EXTRA_CONFIG_SOURCE_PREFIX + environmentName, extraConfig);
  }

  public static void syncToSpring(Environment environment) {
    if (isExternalInit()) {
      return;
    }

    addMicroserviceYAMLToSpring(environment);
    addMappingToSpring(environment);
  }

  private void startupBootStrapService(Environment environment) {
    bootStrapServices.forEach(bootStrapService -> bootStrapService.startup(environment));
  }

  /**
   * make springboot have a change to add microservice.yaml source earlier<br>
   * to affect {@link Conditional}
   * @param environment environment
   */
  private static void addMicroserviceYAMLToSpring(Environment environment) {
    if (!(environment instanceof ConfigurableEnvironment)) {
      return;
    }

    MutablePropertySources propertySources = ((ConfigurableEnvironment) environment).getPropertySources();
    if (propertySources.contains(MICROSERVICE_PROPERTY_SOURCE_NAME)) {
      return;
    }

    propertySources.addLast(new EnumerablePropertySource<MicroserviceConfigLoader>(MICROSERVICE_PROPERTY_SOURCE_NAME) {
      private final Map<String, Object> values = new HashMap<>();

      private final String[] propertyNames;

      {
        MicroserviceConfigLoader loader = new MicroserviceConfigLoader();
        loader.loadAndSort();

        loader.getConfigModels()
            .forEach(configModel -> values.putAll(YAMLUtil.retrieveItems("", configModel.getConfig())));

        propertyNames = values.keySet().toArray(new String[0]);
      }

      @Override
      public String[] getPropertyNames() {
        return propertyNames;
      }

      @SuppressWarnings("unchecked")
      @Override
      public Object getProperty(String name) {
        Object value = this.values.get(name);

        // spring will not resolve nested placeholder of list, so try to fix the problem
        if (value instanceof List) {
          value = ((List<Object>) value).stream()
              .filter(item -> item instanceof String)
              .map(item -> environment.resolvePlaceholders((String) item))
              .collect(Collectors.toList());
        }
        return value;
      }
    });
  }

  private static void addMappingToSpring(Environment environment) {
    if (!(environment instanceof ConfigurableEnvironment)) {
      return;
    }

    MutablePropertySources propertySources = ((ConfigurableEnvironment) environment).getPropertySources();
    if (propertySources.contains(MAPPING_PROPERTY_SOURCE_NAME)) {
      return;
    }

    Map<String, Object> mappings = ConfigMapping.getConvertedMap(environment);
    propertySources.addFirst(new MapPropertySource(MAPPING_PROPERTY_SOURCE_NAME, mappings));
  }

  private void addDynamicConfigurationToSpring(Environment environment,
      ConfigCenterConfigurationSource configCenterConfigurationSource) {
    if (!(environment instanceof ConfigurableEnvironment)) {
      return;
    }
    ConfigurableEnvironment ce = (ConfigurableEnvironment) environment;
    if (configCenterConfigurationSource == null) {
      return;
    }
    try {
      ce.getPropertySources().addFirst(new MapPropertySource("dynamic-source", dynamicData));
    } catch (Exception e) {
      if (DynamicPropertyFactory.getInstance().getBooleanProperty(Const.PRINT_SENSITIVE_ERROR_MESSAGE,
          false).get()) {
        LOGGER.warn("set up spring property source failed.", e);
      } else {
        LOGGER.warn("set up spring property source failed. msg: {}", e.getMessage());
      }
    }
  }

  @Override
  protected Properties mergeProperties() throws IOException {
    Properties properties = super.mergeProperties();
    if (isExternalInit()) {
      return properties;
    }

    AbstractConfiguration config = ConfigurationManager.getConfigInstance();
    Iterator<String> iterator = config.getKeys();
    while (iterator.hasNext()) {
      String key = iterator.next();
      Object value = config.getProperty(key);
      properties.put(key, value);
    }
    return properties;
  }

  /**
   * Try to get a name for identifying the environment.
   * @param environment the target that the name is generated for.
   * @return The generated name for the environment.
   */
  private String generateNameForEnvironment(Environment environment) {
    String environmentName = environment.getProperty("spring.config.name");
    if (!StringUtils.isEmpty(environmentName)) {
      return environmentName;
    }

    environmentName = environment.getProperty("spring.application.name");
    if (!StringUtils.isEmpty(environmentName)) {
      return environmentName;
    }

    return environment.getClass().getName() + "@" + environment.hashCode();
  }

  /**
   * Traversal all {@link PropertySource} of {@link ConfigurableEnvironment}, and try to get all properties.
   */
  private Map<String, Object> getAllProperties(Environment environment) {
    Map<String, Object> configFromSpringBoot = new HashMap<>();

    if (!(environment instanceof ConfigurableEnvironment)) {
      return configFromSpringBoot;
    }

    ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;

    if (ignoreResolveFailure()) {
      configurableEnvironment.setIgnoreUnresolvableNestedPlaceholders(true);
    }

    for (PropertySource<?> propertySource : configurableEnvironment.getPropertySources()) {
      if (MICROSERVICE_PROPERTY_SOURCE_NAME.equals(propertySource.getName())
          || MAPPING_PROPERTY_SOURCE_NAME.equals(propertySource.getName())) {
        continue;
      }
      getProperties(configurableEnvironment, propertySource, configFromSpringBoot);
    }
    return configFromSpringBoot;
  }

  /**
   * Get property names from {@link EnumerablePropertySource}, and get property value from {@link ConfigurableEnvironment#getProperty(String)}
   */
  private void getProperties(ConfigurableEnvironment environment, PropertySource<?> propertySource,
      Map<String, Object> configFromSpringBoot) {
    if (propertySource instanceof CompositePropertySource) {
      // recursively get EnumerablePropertySource
      CompositePropertySource compositePropertySource = (CompositePropertySource) propertySource;
      compositePropertySource.getPropertySources().forEach(ps -> getProperties(environment, ps, configFromSpringBoot));
      return;
    }
    if (propertySource instanceof EnumerablePropertySource) {
      EnumerablePropertySource<?> enumerablePropertySource = (EnumerablePropertySource<?>) propertySource;
      for (String propertyName : enumerablePropertySource.getPropertyNames()) {
        try {
          configFromSpringBoot.put(propertyName, environment.getProperty(propertyName, Object.class));
        } catch (Exception e) {
          throw new RuntimeException(
              "set up spring property source failed.If you still want to start up the application and ignore errors, you can set servicecomb.config.ignoreResolveFailure to true.",
              e);
        }
      }
      return;
    }

    LOGGER.debug("a none EnumerablePropertySource is ignored, propertySourceName = [{}]", propertySource.getName());
  }

  private boolean ignoreResolveFailure() {
    return ConfigUtil
        .createLocalConfig()
        .getBoolean("servicecomb.config.ignoreResolveFailure", false);
  }
}
