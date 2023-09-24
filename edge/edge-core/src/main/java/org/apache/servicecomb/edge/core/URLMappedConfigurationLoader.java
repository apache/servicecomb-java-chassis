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
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

public class URLMappedConfigurationLoader {
  private static final Logger LOG = LoggerFactory.getLogger(URLMappedConfigurationLoader.class);

  private static final String KEY_MAPPING_PATH = ".path";

  private static final String KEY_MAPPING_SERVICE_NAME = "%s.%s.microserviceName";

  private static final String KEY_MAPPING_VERSION_RULE = "%s.%s.versionRule";

  private static final String KEY_MAPPING_PREFIX_SEGMENT_COUNT = "%s.%s.prefixSegmentCount";

  public static Map<String, URLMappedConfigurationItem> loadConfigurations(
      Environment environment, String configPrefix) {
    Map<String, URLMappedConfigurationItem> configurations = new HashMap<>();
    Set<String> configsItems = ConfigUtil.propertiesWithPrefix((ConfigurableEnvironment) environment, configPrefix);
    for (String pathKey : configsItems) {
      if (pathKey.endsWith(KEY_MAPPING_PATH)) {
        URLMappedConfigurationItem configurationItem = new URLMappedConfigurationItem();
        String pattern = environment.getProperty(pathKey);
        if (StringUtils.isEmpty(pattern)) {
          continue;
        }
        configurationItem.setPattern(Pattern.compile(pattern));
        configurationItem.setStringPattern(pattern);
        String pathKeyItem = pathKey
            .substring(configPrefix.length() + 1, pathKey.length() - KEY_MAPPING_PATH.length());
        configurationItem.setMicroserviceName(environment.getProperty(
            String.format(KEY_MAPPING_SERVICE_NAME, configPrefix, pathKeyItem)));
        if (StringUtils.isEmpty(configurationItem.getMicroserviceName())) {
          continue;
        }
        configurationItem.setPrefixSegmentCount(environment.getProperty(
            String.format(KEY_MAPPING_PREFIX_SEGMENT_COUNT, configPrefix, pathKeyItem), int.class, 0));
        configurationItem.setVersionRule(environment.getProperty(
            String.format(KEY_MAPPING_VERSION_RULE, configPrefix, pathKeyItem), "0.0.0+"));
        configurations.put(pathKeyItem, configurationItem);
      }
    }
    logConfigurations(configurations);
    return configurations;
  }

  private static void logConfigurations(Map<String, URLMappedConfigurationItem> configurations) {
    configurations.forEach((key, item) -> LOG.info("config item: key=" + key + ";pattern=" + item.getStringPattern()
        + ";service=" + item.getMicroserviceName() + ";versionRule=" + item.getVersionRule()));
  }
}
