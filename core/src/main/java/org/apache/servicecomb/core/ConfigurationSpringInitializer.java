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
import java.util.List;
import java.util.Properties;

import org.apache.servicecomb.config.DynamicPropertiesSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 *  Add dynamic configuration, microserive.yaml to spring
 */
public class ConfigurationSpringInitializer extends PropertySourcesPlaceholderConfigurer implements EnvironmentAware {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationSpringInitializer.class);

  public static final String MICROSERVICE_PROPERTY_SOURCE_NAME = "microservice.yaml";

  public static final String MAPPING_PROPERTY_SOURCE_NAME = "mapping.yaml";

  public static final String EXTERNAL_INIT = "scb-config-external-init";

  public static void setExternalInit(boolean value) {
    System.setProperty(EXTERNAL_INIT, String.valueOf(value));
  }

  public static boolean isExternalInit() {
    return Boolean.getBoolean(EXTERNAL_INIT);
  }

  private final List<DynamicPropertiesSource<?>> dynamicPropertiesSources;

  public ConfigurationSpringInitializer(List<DynamicPropertiesSource<?>> dynamicPropertiesSources) {
    setOrder(Ordered.LOWEST_PRECEDENCE / 2);
    setIgnoreUnresolvablePlaceholders(true);
    this.dynamicPropertiesSources = dynamicPropertiesSources;
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

    addDynamicConfigurationToSpring(environment);
  }


  private void addDynamicConfigurationToSpring(Environment environment) {
    if (!(environment instanceof ConfigurableEnvironment)) {
      return;
    }
    try {
      for (DynamicPropertiesSource<?> dynamicPropertiesSource : dynamicPropertiesSources) {
        ((ConfigurableEnvironment) environment).getPropertySources()
            .addFirst(dynamicPropertiesSource.create(environment));
      }
    } catch (Exception e) {
      if (environment.getProperty(CoreConst.PRINT_SENSITIVE_ERROR_MESSAGE, boolean.class,
          false)) {
        LOGGER.warn("set up spring property source failed.", e);
      } else {
        LOGGER.warn("set up spring property source failed. msg: {}", e.getMessage());
      }
    }
  }

  @Override
  protected Properties mergeProperties() throws IOException {
    return super.mergeProperties();
  }
}
