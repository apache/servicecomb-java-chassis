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

package org.apache.servicecomb.config.common;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.CollectionUtils;

public class ConfigConverter {
  private Map<String, Object> currentData = Collections.emptyMap();

  private Map<String, Object> lastRawData;

  private final List<String> fileSources;

  public ConfigConverter(List<String> fileSources) {
    this.fileSources = fileSources;
  }

  public Map<String, Object> getLastRawData() {
    return this.lastRawData;
  }

  public Map<String, Object> getCurrentData() {
    return this.currentData;
  }

  public Map<String, Object> updateData(Map<String, Object> rawData) {
    Map<String, Object> lastData = this.currentData;

    this.lastRawData = rawData;

    if (CollectionUtils.isEmpty(fileSources)) {
      this.currentData = rawData;
      return lastData;
    }

    Map<String, Object> fileProperties = new HashMap<>();
    fileSources.forEach(source -> {
      if (rawData.get(source) != null) {
        fileProperties.put(source, rawData.get(source));
      }
    });

    Map<String, Object> result = new HashMap<>(rawData.size());
    result.putAll(rawData);
    fileProperties.forEach((k, v) -> result.putAll(createFileSource(v)));
    this.currentData = result;
    return lastData;
  }

  private Map<String, Object> createFileSource(Object v) {
    YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
    yamlFactory.setResources(new ByteArrayResource(v.toString().getBytes(StandardCharsets.UTF_8)));
    return propertiesToMap(yamlFactory.getObject());
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> propertiesToMap(Properties properties) {
    if (properties == null) {
      return Collections.emptyMap();
    }
    Map<String, Object> result = new HashMap<>();
    Enumeration<String> keys = (Enumeration<String>) properties.propertyNames();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      Object value = properties.getProperty(key);
      result.put(key, value);
    }
    return result;
  }
}
