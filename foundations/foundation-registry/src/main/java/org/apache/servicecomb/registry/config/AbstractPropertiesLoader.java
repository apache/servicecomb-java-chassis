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

package org.apache.servicecomb.registry.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.registry.api.PropertyExtended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loading microservice properties
 */
public abstract class AbstractPropertiesLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPropertiesLoader.class);

  public Map<String, String> loadProperties(Configuration configuration) {
    Map<String, String> propertiesMap = new HashMap<>();
    loadPropertiesFromConfigMap(configuration, propertiesMap);
    loadPropertiesFromExtendedClass(configuration, propertiesMap);

    return propertiesMap;
  }

  abstract protected Map<String, String> readProperties(Configuration configuration);

  abstract protected String readPropertiesExtendedClass(Configuration configuration);

  private void loadPropertiesFromConfigMap(Configuration configuration, Map<String, String> propertiesMap) {
    propertiesMap.putAll(readProperties(configuration));
  }

  private void loadPropertiesFromExtendedClass(Configuration configuration, Map<String, String> propertiesMap) {
    String extendedPropertyClass = readPropertiesExtendedClass(configuration);

    if (StringUtils.isEmpty(extendedPropertyClass)) {
      return;
    }

    try {
      Class<?> classExternalProperty = Class.forName(extendedPropertyClass);
      if (!PropertyExtended.class.isAssignableFrom(classExternalProperty)) {
        String errMsg = String.format(
            "Define propertyExtendedClass %s in yaml, but not implement the interface PropertyExtended.",
            extendedPropertyClass);
        LOGGER.error(errMsg);
        throw new Error(errMsg);
      }

      PropertyExtended instance = (PropertyExtended) classExternalProperty.newInstance();
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
}
