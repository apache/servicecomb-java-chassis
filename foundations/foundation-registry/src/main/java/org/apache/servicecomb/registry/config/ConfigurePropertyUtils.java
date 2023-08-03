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
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

import com.netflix.config.DynamicPropertyFactory;

public final class ConfigurePropertyUtils {
  private ConfigurePropertyUtils() {
  }

  /**
   * 获取key包含prefix前缀的所有配置项
   */
  public static Map<String, String> getPropertiesWithPrefix(String prefix) {
    Object config = DynamicPropertyFactory.getBackingConfigurationSource();
    if (!Configuration.class.isInstance(config)) {
      return new HashMap<>();
    }

    return getPropertiesWithPrefix((Configuration) config, prefix);
  }

  // caller ensure configuration is valid
  public static Map<String, String> getPropertiesWithPrefix(Configuration configuration, String prefix) {
    Map<String, String> propertiesMap = new HashMap<>();

    Iterator<String> keysIterator = configuration.getKeys(prefix);
    while (keysIterator.hasNext()) {
      String key = keysIterator.next();
      propertiesMap.put(key.substring(prefix.length() + 1), String.valueOf(configuration.getProperty(key)));
    }
    return propertiesMap;
  }
}
