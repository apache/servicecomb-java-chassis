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

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.servicecomb.config.ConfigMapping;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.YAMLUtil;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.config.spi.ConfigCenterConfigurationSource;
import org.apache.servicecomb.foundation.bootstrap.BootStrapService;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;

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
public class ConfigurationSpringInitializer extends PropertyPlaceholderConfigurer implements EnvironmentAware {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationSpringInitializer.class);

  public static final String EXTRA_CONFIG_SOURCE_PREFIX = "extraConfig-";

  private final List<BootStrapService> bootStrapServices =
      SPIServiceUtils.getSortedService(BootStrapService.class);

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
    String environmentName = generateNameForEnvironment(environment);
    LOGGER.info("Environment received, will get configurations from [{}].", environmentName);

    Map<String, Object> extraConfig = getAllProperties(environment);

    addMicroserviceYAMLToSpring(environment);

    addMappingToString(environment);

    startupBootStrapService(environment);

    ConfigUtil.addExtraConfig(EXTRA_CONFIG_SOURCE_PREFIX + environmentName, extraConfig);

    ConfigCenterConfigurationSource configCenterConfigurationSource = ConfigUtil.installDynamicConfig();

    addDynamicConfigurationToSpring(environment, configCenterConfigurationSource);
  }

  private void startupBootStrapService(Environment environment) {
    bootStrapServices.forEach(bootStrapService -> bootStrapService.startup(environment));
  }

  private void addMicroserviceYAMLToSpring(Environment environment) {
    if (environment instanceof ConfigurableEnvironment) {
      ConfigurableEnvironment ce = (ConfigurableEnvironment) environment;
      MicroserviceConfigLoader loader = new MicroserviceConfigLoader();
      loader.loadAndSort();

      ce.getPropertySources()
          .addLast(new EnumerablePropertySource<MicroserviceConfigLoader>("microservice.yaml",
              loader) {

            private boolean parsed = false;

            private Map<String, Object> values = null;

            private String[] propertyNames = null;

            @Override
            public String[] getPropertyNames() {
              if (!parsed) {
                parseData();
              }

              return propertyNames;
            }

            @Override
            public Object getProperty(String s) {
              if (!parsed) {
                parseData();
              }

              return this.values.get(s);
            }

            private void parseData() {
              values = new HashMap<>();
              loader.getConfigModels()
                  .forEach(configModel -> values.putAll(YAMLUtil.retrieveItems("", configModel.getConfig())));
              propertyNames = values.keySet().toArray(new String[values.size()]);
            }
          });
    }
  }

  private void addMappingToString(Environment environment) {
    if (environment instanceof ConfigurableEnvironment) {
      ConfigurableEnvironment ce = (ConfigurableEnvironment) environment;
      Map<String, Object> mappings = ConfigMapping.getConvertedMap(environment);
      ce.getPropertySources().addFirst(new MapPropertySource("mapping.yaml", mappings));
    }
  }

  private void addDynamicConfigurationToSpring(Environment environment,
      ConfigCenterConfigurationSource configCenterConfigurationSource) {
    if (environment instanceof ConfigurableEnvironment) {
      ConfigurableEnvironment ce = (ConfigurableEnvironment) environment;
      if (configCenterConfigurationSource != null) {
        try {
          ce.getPropertySources()
              .addFirst(new MapPropertySource("dynamic-source", configCenterConfigurationSource.getCurrentData()));
        } catch (Exception e) {
          LOGGER.warn("set up spring property source failed. msg: {}", e.getMessage());
        }
      }
    }
  }

  @Override
  protected Properties mergeProperties() throws IOException {
    Properties properties = super.mergeProperties();

    AbstractConfiguration config = ConfigurationManager.getConfigInstance();
    Iterator<String> iterator = config.getKeys();
    while (iterator.hasNext()) {
      String key = iterator.next();
      Object value = config.getProperty(key);
      properties.put(key, value);
    }
    return properties;
  }

  @Override
  protected String resolvePlaceholder(String placeholder, Properties props) {
    String propertyValue = super.resolvePlaceholder(placeholder, props);
    if (propertyValue == null) {
      return DynamicPropertyFactory.getInstance().getStringProperty(placeholder, null).get();
    }
    return propertyValue;
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
