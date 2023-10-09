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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.PropertyConverter;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

public final class ConfigUtil {
  private ConfigUtil() {
  }

  public static List<String> parseArrayValue(String value) {
    return PropertyConverter.split(value, ',', true);
  }

  public static Set<String> propertiesWithPrefix(Environment environment, String prefix) {
    Set<String> result = new HashSet<>();
    for (PropertySource<?> propertySource : ((ConfigurableEnvironment) environment).getPropertySources()) {
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

  public static Map<String, String> stringPropertiesWithPrefix(Environment environment, String prefix) {
    Map<String, String> result = new HashMap<>();
    for (PropertySource<?> propertySource : ((ConfigurableEnvironment) environment).getPropertySources()) {
      if (propertySource instanceof EnumerablePropertySource) {
        for (String key : ((EnumerablePropertySource<?>) propertySource).getPropertyNames()) {
          if (key.startsWith(prefix)) {
            result.put(key, environment.getProperty(key));
          }
        }
      }
    }
    return result;
  }
}
