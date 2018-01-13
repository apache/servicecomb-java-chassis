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

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by   on 2017/1/5.
 */
public final class ConfigMapping {
  private static Map<String, Object> configMap = null;

  static {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    InputStream is = loader.getResourceAsStream("mapping.yaml");
    configMap = YAMLUtil.yaml2Properties(is);
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
        String newKey = (String) entry.getValue();
        retMap.put(newKey, configValue);
        retMap.remove(key);
      }
    }
    return retMap;
  }
}
