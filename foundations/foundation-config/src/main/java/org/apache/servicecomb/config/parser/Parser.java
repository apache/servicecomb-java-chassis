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

package org.apache.servicecomb.config.parser;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public interface Parser {
  String CONTENT_TYPE_YAML = "yaml";
  String CONTENT_TYPE_PROPERTIES = "properties";
  String CONTENT_TYPE_RAW = "raw";

  YamlParser yamlParser = new YamlParser();
  PropertiesParser propertiesParser = new PropertiesParser();
  RawParser rawParser = new RawParser();

  Map<String, Object> parse(String content, String prefix, boolean addPrefix);

  static Parser findParser(String contentType) {
    switch (contentType) {
      case CONTENT_TYPE_YAML:
        return yamlParser;
      case CONTENT_TYPE_PROPERTIES:
        return propertiesParser;
      case CONTENT_TYPE_RAW:
        return rawParser;
      default:
        throw new IllegalArgumentException("not supported contentType=" + contentType);
    }
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> propertiesToMap(Properties properties, String prefix, boolean addPrefix) {
    Map<String, Object> result = new HashMap<>();
    Enumeration<String> keys = (Enumeration<String>) properties.propertyNames();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      String value = properties.getProperty(key);

      if (addPrefix && !StringUtils.isEmpty(prefix)) {
        key = prefix + "." + key;
      }

      if (value == null) {
        result.put(key, null);
      } else {
        result.put(key, value.trim());
      }
    }
    return result;
  }
}
