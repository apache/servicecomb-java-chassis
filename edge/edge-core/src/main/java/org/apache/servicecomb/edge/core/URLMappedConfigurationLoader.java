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

package org.apache.servicecomb.edge.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicPropertyFactory;

public class URLMappedConfigurationLoader {
  private static final Logger LOG = LoggerFactory.getLogger(URLMappedConfigurationLoader.class);

  private static final String KEY_MAPPING_PATH = ".path";

  private static final String KEY_MAPPING_SERVICE_NAME = "%s.%s.microserviceName";

  private static final String KEY_MAPPING_VERSION_RULE = "%s.%s.versionRule";

  private static final String KEY_MAPPING_PREFIX_SEGMENT_COUNT = "%s.%s.prefixSegmentCount";

  public static Map<String, URLMappedConfigurationItem> loadConfigurations(
      ConcurrentCompositeConfiguration config, String configPrefix) {
    Map<String, URLMappedConfigurationItem> configurations = new HashMap<>();
    Iterator<String> configsItems = config.getKeys(configPrefix);
    while (configsItems.hasNext()) {
      String pathKey = configsItems.next();
      if (pathKey.endsWith(KEY_MAPPING_PATH)) {
        URLMappedConfigurationItem configurationItem = new URLMappedConfigurationItem();
        String pattern = DynamicPropertyFactory.getInstance()
            .getStringProperty(pathKey, null).get();
        if (StringUtils.isEmpty(pattern)) {
          continue;
        }
        configurationItem.setPattern(Pattern.compile(pattern));
        configurationItem.setStringPattern(pattern);
        String pathKeyItem = pathKey
            .substring(configPrefix.length() + 1, pathKey.length() - KEY_MAPPING_PATH.length());
        configurationItem.setMicroserviceName(DynamicPropertyFactory.getInstance()
            .getStringProperty(String.format(KEY_MAPPING_SERVICE_NAME, configPrefix, pathKeyItem), null).get());
        if (StringUtils.isEmpty(configurationItem.getMicroserviceName())) {
          continue;
        }
        configurationItem.setPrefixSegmentCount(DynamicPropertyFactory.getInstance()
            .getIntProperty(String.format(KEY_MAPPING_PREFIX_SEGMENT_COUNT, configPrefix, pathKeyItem), 0).get());
        configurationItem.setVersionRule(DynamicPropertyFactory.getInstance()
            .getStringProperty(String.format(KEY_MAPPING_VERSION_RULE, configPrefix, pathKeyItem), "0.0.0+").get());
        configurations.put(pathKeyItem, configurationItem);
      }
    }
    logConfigurations(configurations);
    return configurations;
  }

  private static void logConfigurations(Map<String, URLMappedConfigurationItem> configurations) {
    configurations.entrySet().forEach(stringConfigurationItemEntry -> {
      URLMappedConfigurationItem item = stringConfigurationItemEntry.getValue();
      LOG.info("config item: key=" + stringConfigurationItemEntry.getKey() + ";pattern=" + item.getStringPattern()
          + ";service=" + item.getMicroserviceName() + ";versionRule=" + item.getVersionRule());
    });
  }
}
