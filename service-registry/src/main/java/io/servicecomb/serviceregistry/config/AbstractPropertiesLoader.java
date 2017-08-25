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

package io.servicecomb.serviceregistry.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.servicecomb.serviceregistry.api.PropertyExtended;

/**
 * 加载微服务和微服务实例的properties
 */
public abstract class AbstractPropertiesLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPropertiesLoader.class);

  protected static final String PROPERTIES = ".properties";

  protected static final String EXTENDED_CLASS_FOR_COMPATIBLE = ".propertyExtentedClass";

  protected static final String EXTENDED_CLASS = ".propertyExtendedClass";

  protected abstract String getConfigOptionPrefix();

  public Map<String, String> loadProperties(Configuration configuration) {
    Map<String, String> propertiesMap = new HashMap<>();
    loadPropertiesFromConfigMap(configuration, propertiesMap);
    loadPropertiesFromExtendedClass(configuration, propertiesMap);

    return propertiesMap;
  }

  protected void loadPropertiesFromConfigMap(Configuration configuration, Map<String, String> propertiesMap) {
    String configKeyPrefix = mergeStrings(getConfigOptionPrefix(), PROPERTIES);
    propertiesMap.putAll(ConfigurePropertyUtils.getPropertiesWithPrefix(configuration, configKeyPrefix));
  }

  protected void loadPropertiesFromExtendedClass(Configuration configuration, Map<String, String> propertiesMap) {
    String extendedPropertyClass = readExtendedPropertyClassName(configuration, EXTENDED_CLASS);
    if (StringUtils.isEmpty(extendedPropertyClass)) {
      extendedPropertyClass = readExtendedPropertyClassName(configuration, EXTENDED_CLASS_FOR_COMPATIBLE);
      if (StringUtils.isEmpty(extendedPropertyClass)) {
        return;
      }
    }

    try {
      Class<?> classExtenalProperty = Class.forName(extendedPropertyClass);
      if (!PropertyExtended.class.isAssignableFrom(classExtenalProperty)) {
        String errMsg = String.format(
            "Define propertyExtendedClass %s in yaml, but not implement the interface PropertyExtended.",
            extendedPropertyClass);
        LOGGER.error(errMsg);
        throw new Error(errMsg);
      }

      PropertyExtended instance = (PropertyExtended) classExtenalProperty.newInstance();
      Map<String, String> extendedPropertiesMap = instance.getExtendedProperties();
      if (extendedPropertiesMap != null && !extendedPropertiesMap.isEmpty()) {
        propertiesMap.putAll(extendedPropertiesMap);
      }
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      String errMsg = "Fail to create instance of class: " + extendedPropertyClass;
      LOGGER.error(errMsg);
      throw new Error(errMsg, e);
    }
  }

  private String readExtendedPropertyClassName(Configuration configuration, String keyName) {
    String configKey = mergeStrings(getConfigOptionPrefix(), keyName);
    return configuration.getString(configKey, "");
  }

  protected static String mergeStrings(String... strArr) {
    return String.join("", strArr);
  }
}
