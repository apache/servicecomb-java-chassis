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

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by   on 2017/1/5.
 */
public final class ConfigMapping {
  private static Map<String, Object> configMap = null;

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigMapping.class);

  static {
    ClassLoader loader = JvmUtils.findClassLoader();
    List<URL> urlList = new ArrayList<>();
    configMap = new HashMap<String, Object>();
    Enumeration<URL> urls;
    try {
      urls = loader.getResources("mapping.yaml");
      while (urls.hasMoreElements()) {
        urlList.add(urls.nextElement());
      }
      for (URL url : urlList) {
        try (InputStream in = url.openStream()) {
          configMap.putAll(YAMLUtil.yaml2Properties(in));
        }
      }
    } catch (IOException e) {
      LOGGER.error("get config mapping file error!", e);
    }
  }

  private ConfigMapping() {
  }

  public static String map(String key) {
    return (String) configMap.get(key);
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
    for (Map.Entry<String, Object> entry : configMap.entrySet()) {
      String key = entry.getKey();
      Object configValue = oldMap.get(key);
      if (configValue != null) {
        if (entry.getValue() instanceof List) {
          @SuppressWarnings("unchecked")
          List<String> newKeys = (List<String>) entry.getValue();
          for (String newKey : newKeys) {
            retMap.put(newKey, configValue);
          }
        } else {
          String newKey = (String) entry.getValue();
          retMap.put(newKey, configValue);
        }
      }
    }
    return retMap;
  }

  public static Map<String, Object> getConvertedMap(Configuration config) {
    if (configMap == null) {
      return new LinkedHashMap<>();
    }
    Map<String, Object> retMap = new LinkedHashMap<>();
    for (Map.Entry<String, Object> entry : configMap.entrySet()) {
      String key = entry.getKey();
      Object configValue = config.getProperty(key);
      if (configValue != null) {
        if (entry.getValue() instanceof List) {
          @SuppressWarnings("unchecked")
          List<String> newKeys = (List<String>) entry.getValue();
          for (String newKey : newKeys) {
            retMap.put(newKey, configValue);
          }
        } else {
          String newKey = (String) entry.getValue();
          retMap.put(newKey, configValue);
        }
      }
    }
    return retMap;
  }
}
