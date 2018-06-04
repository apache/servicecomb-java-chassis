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
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

/**
 * Created by   on 2017/1/5.
 */
public final class ConfigMapping {
  private static Map<String, Object> configMap = null;

  static {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    List<URL> urlList = new ArrayList<>();
    configMap = new HashMap<String, Object>();
    Enumeration<URL> urls;
    try {
      urls = loader.getResources("mapping.yaml");
      while (urls.hasMoreElements()) {
        urlList.add(urls.nextElement());
      }
      for (URL url : urlList) {
        configMap.putAll(YAMLUtil.yaml2Properties(url.openStream()));
      }
    } catch (IOException e) {
      e.printStackTrace();
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
      String targetMapping = entry.getKey();
      String sourceMapping = (String) entry.getValue();
      Object configValue = oldMap.get(sourceMapping);
      if (configValue != null) {
        retMap.put(targetMapping, configValue);
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
      String targetMapping = entry.getKey();
      String sourceMapping = (String) entry.getValue();
      Object configValue = config.getProperty(sourceMapping);
      if (configValue != null) {
        retMap.put(targetMapping, configValue);
      }
    }
    return retMap;
  }
}
