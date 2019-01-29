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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.servicecomb.config.spi.ConfigCenterConfigurationSource;
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

import com.google.common.collect.Lists;
import com.netflix.config.ConcurrentCompositeConfiguration;
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

    ConfigUtil.addExtraConfig(EXTRA_CONFIG_SOURCE_PREFIX + environmentName, extraConfig);

    ConfigUtil.installDynamicConfig();

    setUpSpringPropertySource(environment);
  }

  private void setUpSpringPropertySource(Environment environment) {
    if (environment instanceof ConfigurableEnvironment) {
      ConfigurableEnvironment ce = (ConfigurableEnvironment) environment;
      ConfigCenterConfigurationSource configCenterConfigurationSource =
          SPIServiceUtils.getTargetService(ConfigCenterConfigurationSource.class);
      if (configCenterConfigurationSource != null) {
        try {
          ce.getPropertySources()
              .addFirst(new MapPropertySource("dynamic-source", configCenterConfigurationSource.getCurrentData()));
        } catch (Exception e) {
          LOGGER.warn("set up spring property source failed. msg: {}", e.getMessage());
        }
      }
      ConcurrentCompositeConfiguration concurrentCompositeConfiguration = ConfigUtil.createLocalConfig();
      ce.getPropertySources().addLast(
          new EnumerablePropertySource<ConcurrentCompositeConfiguration>("microservice.yaml",
              concurrentCompositeConfiguration) {
            private String[] propertyNames = null;

            @Override
            public String[] getPropertyNames() {
              if (propertyNames == null) {
                List<String> keyList = Lists.newArrayList(this.source.getKeys());
                propertyNames = keyList.toArray(new String[keyList.size()]);
              }
              return propertyNames;
            }

            @Override
            public Object getProperty(String s) {
              return this.source.getProperty(s);
            }
          });
    }
  }
  @Override
  protected Properties mergeProperties() throws IOException {
    Properties properties = super.mergeProperties();

    AbstractConfiguration config = ConfigurationManager.getConfigInstance();
    Iterator<String> iter = config.getKeys();
    while (iter.hasNext()) {
      String key = iter.next();
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
        configFromSpringBoot.put(propertyName, environment.getProperty(propertyName, Object.class));
      }
      return;
    }

    LOGGER.debug("a none EnumerablePropertySource is ignored, propertySourceName = [{}]", propertySource.getName());
  }
}
