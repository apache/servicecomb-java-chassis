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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.servicecomb.config.ConfigMapping;
import org.apache.servicecomb.config.YAMLUtil;
import org.apache.servicecomb.config.file.MicroserviceConfigLoader;
import org.apache.servicecomb.foundation.bootstrap.BootStrapService;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

/**
 * Add microservice.yaml to Environment earlier<br>
 * to affect {@link Conditional}<br>
 */
public class ConfigurationSpringBootInitializer implements EnvironmentPostProcessor {
  public static final String MICROSERVICE_PROPERTY_SOURCE_NAME = "microservice.yaml";

  public static final String MAPPING_PROPERTY_SOURCE_NAME = "mapping.yaml";

  private final List<BootStrapService> bootStrapServices = SPIServiceUtils.getSortedService(BootStrapService.class);

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    addMicroserviceDefinitions(environment);
    startupBootStrapService(environment);
  }

  public static void addMicroserviceDefinitions(Environment environment) {
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
}
