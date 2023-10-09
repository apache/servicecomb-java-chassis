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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;


/**
 * This class holds configurations that need to be configured
 * through property files or environment variables.
 */
public class BootStrapProperties {
  // start of : service definition keys
  // service definition keys of old version
  public static final String OLD_CONFIG_SERVICE_APPLICATION = "APPLICATION_ID";

  public static final String OLD_CONFIG_SERVICE_NAME = "service_description.name";

  public static final String OLD_CONFIG_SERVICE_VERSION = "service_description.version";

  public static final String OLD_CONFIG_SERVICE_ROLE = "service_description.role";

  public static final String OLD_CONFIG_SERVICE_DESCRIPTION = "service_description.description";

  public static final String OLD_CONFIG_SERVICE_ENVIRONMENT = "service_description.environment";

  public static final String OLD_CONFIG_SERVICE_EXTENDED_CLASS = "service_description.propertyExtendedClass";

  public static final String OLD_CONFIG_SERVICE_PROPERTIES = "service_description.properties";

  // service instance definition keys of old version
  public static final String OLD_CONFIG_SERVICE_INSTANCE_PROPERTIES = "instance_description.properties";

  public static final String OLD_CONFIG_SERVICE_INSTANCE_EXTENDED_CLASS = "instance_description.propertyExtendedClass";

  public static final String OLD_CONFIG_SERVICE_INSTANCE_INITIAL_STATUS = "instance_description.initialStatus";

  // service definition keys of new version
  public static final String CONFIG_SERVICE_APPLICATION = "servicecomb.service.application";

  public static final String CONFIG_SERVICE_NAME = "servicecomb.service.name";

  public static final String CONFIG_SERVICE_VERSION = "servicecomb.service.version";

  public static final String CONFIG_SERVICE_ROLE = "servicecomb.service.role";

  public static final String CONFIG_SERVICE_DESCRIPTION = "servicecomb.service.description";

  public static final String CONFIG_SERVICE_ENVIRONMENT = "servicecomb.service.environment";

  public static final String CONFIG_SERVICE_EXTENDED_CLASS = "servicecomb.service.propertyExtendedClass";

  public static final String CONFIG_SERVICE_PROPERTIES = "servicecomb.service.properties";

  // service instance definition keys of new version
  public static final String CONFIG_SERVICE_INSTANCE_PROPERTIES = "servicecomb.instance.properties";

  public static final String CONFIG_SERVICE_INSTANCE_EXTENDED_CLASS = "servicecomb.instance.propertyExtendedClass";

  public static final String CONFIG_SERVICE_INSTANCE_INITIAL_STATUS = "servicecomb.instance.initialStatus";

  // configuration default values
  public static final String DEFAULT_APPLICATION = "default";

  public static final String DEFAULT_MICROSERVICE_NAME = "defaultMicroservice";

  public static final String DEFAULT_MICROSERVICE_VERSION = "1.0.0.0";

  public static final String DEFAULT_MICROSERVICE_ROLE = "FRONT";

  public static final String DEFAULT_MICROSERVICE_ENVIRONMENT = "";

  public static final String DEFAULT_MICROSERVICE_INSTANCE_INITIAL_STATUS = "UP";

  public static String readApplication(Environment environment) {
    return readStringValue(environment, CONFIG_SERVICE_APPLICATION,
        OLD_CONFIG_SERVICE_APPLICATION, DEFAULT_APPLICATION);
  }

  public static String readServiceName(Environment environment) {
    String result = readStringValue(environment, CONFIG_SERVICE_NAME, OLD_CONFIG_SERVICE_NAME,
        DEFAULT_MICROSERVICE_NAME);
    checkMicroserviceName(result);
    return result;
  }

  public static String readServiceVersion(Environment environment) {
    return readStringValue(environment, CONFIG_SERVICE_VERSION, OLD_CONFIG_SERVICE_VERSION,
        DEFAULT_MICROSERVICE_VERSION);
  }

  public static String readServiceRole(Environment environment) {
    return readStringValue(environment, CONFIG_SERVICE_ROLE, OLD_CONFIG_SERVICE_ROLE, DEFAULT_MICROSERVICE_ROLE);
  }

  public static String readServiceDescription(Environment environment) {
    String[] descriptionArray = environment.getProperty(CONFIG_SERVICE_DESCRIPTION, String[].class);
    if (null == descriptionArray || descriptionArray.length < 1) {
      descriptionArray = environment.getProperty(OLD_CONFIG_SERVICE_DESCRIPTION, String[].class);
    }

    if (null == descriptionArray || descriptionArray.length < 1) {
      return null;
    }

    StringBuilder rawDescriptionBuilder = new StringBuilder();
    for (String desc : descriptionArray) {
      rawDescriptionBuilder.append(desc).append(",");
    }

    return rawDescriptionBuilder.substring(0, rawDescriptionBuilder.length() - 1);
  }

  public static String readServiceEnvironment(Environment environment) {
    return readStringValue(environment, CONFIG_SERVICE_ENVIRONMENT, OLD_CONFIG_SERVICE_ENVIRONMENT,
        DEFAULT_MICROSERVICE_ENVIRONMENT);
  }

  public static String readServiceExtendedClass(Environment environment) {
    return readStringValue(environment, CONFIG_SERVICE_EXTENDED_CLASS, OLD_CONFIG_SERVICE_EXTENDED_CLASS,
        null);
  }

  public static Map<String, String> readServiceProperties(Environment environment) {
    return readProperties(environment, CONFIG_SERVICE_PROPERTIES, OLD_CONFIG_SERVICE_PROPERTIES);
  }

  public static Map<String, String> readServiceInstanceProperties(Environment environment) {
    return readProperties(environment, CONFIG_SERVICE_INSTANCE_PROPERTIES, OLD_CONFIG_SERVICE_INSTANCE_PROPERTIES);
  }

  public static String readServiceInstanceExtendedClass(Environment environment) {
    return readStringValue(environment, CONFIG_SERVICE_INSTANCE_EXTENDED_CLASS,
        OLD_CONFIG_SERVICE_INSTANCE_EXTENDED_CLASS,
        null);
  }

  public static String readServiceInstanceInitialStatus(Environment environment) {
    return readStringValue(environment, CONFIG_SERVICE_INSTANCE_INITIAL_STATUS,
        OLD_CONFIG_SERVICE_INSTANCE_INITIAL_STATUS,
        DEFAULT_MICROSERVICE_INSTANCE_INITIAL_STATUS);
  }

  private static String readStringValue(Environment environment, String newKey, String oldKey,
      String defaultValue) {
    String result = environment.getProperty(newKey);
    if (result == null) {
      return environment.getProperty(oldKey, defaultValue);
    }
    return result;
  }

  private static Map<String, String> readProperties(Environment environment, String newKey, String oldKey) {
    String prefix = newKey;
    Set<String> keys = ConfigUtil.propertiesWithPrefix(environment, prefix);
    if (keys.isEmpty()) {
      prefix = oldKey;
      keys = ConfigUtil.propertiesWithPrefix(environment, oldKey);
    }
    Map<String, String> result = new HashMap<>(keys.size());
    for (String key : keys) {
      result.put(key.substring(prefix.length() + 1), environment.getProperty(key));
    }
    return result;
  }

  private static void checkMicroserviceName(String name) {
    // the configuration we used
    // when resolve placeholder failed
    // the result will remains ${var}
    if (StringUtils.isEmpty(name) || name.contains("${")) {
      throw new IllegalArgumentException(String.format(
          "MicroserviceName '%s' is invalid. you must configure '%s' or set the placeholder value.",
          name,
          CONFIG_SERVICE_NAME));
    }
  }
}
