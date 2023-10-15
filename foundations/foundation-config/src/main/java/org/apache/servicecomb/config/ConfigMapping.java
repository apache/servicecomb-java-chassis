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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.config.file.AbstractConfigLoader;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import com.google.common.annotations.VisibleForTesting;

/**
 * Created by   on 2017/1/5.
 */
public final class ConfigMapping {
  private static final Map<String, Object> configMap;

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigMapping.class);

  static {
    ClassLoader loader = JvmUtils.findClassLoader();
    List<Map<String, Object>> mappingList = new ArrayList<>();
    configMap = new HashMap<>();
    Enumeration<URL> urls;
    try {
      urls = loader.getResources("mapping.yaml");
      while (urls.hasMoreElements()) {
        try (InputStream in = urls.nextElement().openStream()) {
          mappingList.add(YAMLUtil.yaml2Properties(in));
        }
      }

      mappingList.sort((a, b) -> {
        int orderA = a.get(AbstractConfigLoader.ORDER_KEY) == null ? 0 : (int) a.get(AbstractConfigLoader.ORDER_KEY);
        int orderB = b.get(AbstractConfigLoader.ORDER_KEY) == null ? 0 : (int) b.get(AbstractConfigLoader.ORDER_KEY);
        return orderA - orderB;
      });

      mappingList.forEach(item -> {
        item.remove(AbstractConfigLoader.ORDER_KEY);
        configMap.putAll(item);
      });
    } catch (IOException e) {
      LOGGER.error("get config mapping file error!", e);
    }
  }

  private ConfigMapping() {
  }

  @VisibleForTesting
  @SuppressWarnings("unchecked")
  static <T> T map(String key) {
    return (T) configMap.get(key);
  }

  public static Map<String, Object> getMapping() {
    return configMap;
  }

  public static Map<String, Object> getConvertedMap(Map<String, Object> oldMap) {
    if (configMap == null) {
      return new LinkedHashMap<>();
    }
    Map<String, Object> retMap = new LinkedHashMap<>();
    retMap.putAll(oldMap);
    configMap.entrySet().forEach(entry -> putConfigsToRetMap(retMap, entry, oldMap.get(entry.getKey())));
    return retMap;
  }

  public static Map<String, Object> getConvertedMap(Environment environment) {
    if (configMap == null) {
      return new LinkedHashMap<>();
    }
    Map<String, Object> retMap = new LinkedHashMap<>();
    configMap.entrySet().forEach(entry -> putConfigsToRetMap(retMap, entry, environment.getProperty(entry.getKey())));
    return retMap;
  }

  private static void putConfigsToRetMap(Map<String, Object> retMap, Map.Entry<String, Object> entry,
      Object configValue) {
    if (configValue != null) {
      if (entry.getValue() instanceof List) {
        @SuppressWarnings("unchecked")
        List<String> newKeys = (List<String>) entry.getValue();
        newKeys.forEach(newKey -> retMap.put(newKey, configValue));
        return;
      }
      String newKey = (String) entry.getValue();
      retMap.put(newKey, configValue);
    }
  }
}
