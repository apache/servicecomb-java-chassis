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

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_CSE_PREFIX;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_SERVICECOMB_PREFIX;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public final class YAMLUtil {
  private YAMLUtil() {
  }

  private static Yaml safeParser() {
    // Yaml instance is not thread safe, create a new instance for each parser
    return new Yaml(new SafeConstructor(new LoaderOptions()));
  }

  /**
   * load a input {@link InputStream} to be a map {@link Map}, you have to close the inputStream by yourself, such as:<br>
   * <p>try (InputStream in = url.openStream()) {<br>
   *   &nbsp;&nbsp;&nbsp;&nbsp;    configMap.putAll(YAMLUtil.yaml2Properties(in));<br>
   *     }<br>
   * </p>
   * @param input the stream to be loaded
   * @return a config map
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> yaml2Properties(InputStream input) {
    Map<String, Object> configurations = new LinkedHashMap<>();
    safeParser().loadAll(input).forEach(data -> {
      if (data instanceof Map && isValidMap((Map<Object, Object>) data)) {
        configurations.putAll(retrieveItems("", (Map<String, Object>) data));
      } else {
        throw new IllegalArgumentException("input cannot be convert to map");
      }
    });
    return configurations;
  }

  @SuppressWarnings("unchecked")
  private static boolean isValidMap(Map<Object, Object> data) {
    for (Map.Entry<Object, Object> entry : data.entrySet()) {
      Object key = entry.getKey();
      Object value = entry.getValue();
      if (key instanceof String) {
        if (value instanceof Map) {
          return isValidMap((Map<Object, Object>) value);
        }
        continue;
      }
      return false;
    }
    return true;
  }

  /**
   * load a input {@link String} to be a map {@link Map}
   * @param input the String to be loaded
   * @return a config map
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> yaml2Properties(String input) {
    Map<String, Object> configurations = new LinkedHashMap<>();
    safeParser().loadAll(input).forEach(data -> {
      if (data instanceof Map && isValidMap((Map<Object, Object>) data)) {
        configurations.putAll(retrieveItems("", (Map<String, Object>) data));
      } else {
        throw new IllegalArgumentException("input cannot be convert to map");
      }
    });
    return configurations;
  }

  public static <T> T parserObject(String yamlContent, Class<T> clazz) {
    Yaml parser = new Yaml(new Constructor(new TypeDescription(clazz, clazz), new LoaderOptions()));
    return parser.loadAs(yamlContent, clazz);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> retrieveItems(String prefix, Map<String, Object> propertieMap) {
    Map<String, Object> result = new LinkedHashMap<>();
    if (!prefix.isEmpty()) {
      prefix += ".";
    }

    for (Map.Entry<String, Object> entry : propertieMap.entrySet()) {
      if (entry.getValue() instanceof Map) {
        result.putAll(retrieveItems(prefix + entry.getKey(), (Map<String, Object>) entry.getValue()));
      } else {
        String key = prefix + entry.getKey();
        if (key.startsWith(CONFIG_CSE_PREFIX)) {
          String servicecombKey = CONFIG_SERVICECOMB_PREFIX + key.substring(key.indexOf(".") + 1);
          result.put(servicecombKey, entry.getValue());
        }
        result.put(key, entry.getValue());
      }
    }
    return result;
  }
}
