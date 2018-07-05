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

package org.apache.servicecomb.config.archaius.sources;

import java.io.IOException;
import java.net.URL;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class MicroserviceConfigLoader extends YAMLConfigLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceConfigLoader.class);

  private static final String ADDITIONAL_CONFIG_URL = "servicecomb.configurationSource.additionalUrls";

  private static final String DEFAULT_FILE_NAME = "servicecomb.configurationSource.defaultFileName";

  /**
   * Default configuration file name to be used by default constructor. This file should
   * be on the classpath. The file name can be overridden by the value of system property
   * <code>configurationSource.defaultFileName</code>
   */
  private static final String DEFAULT_CONFIG_FILE_NAME = "microservice.yaml";

  public MicroserviceConfigLoader() {
    // Help to resolve incompatible changes. Can be deleted in future.
    if (!StringUtils.isEmpty(System.getProperty("cse.configurationSource.additionalUrls"))) {
      throw new IllegalArgumentException("-Dcse.configurationSource.additionalUrls"
          + " has been replaced with -D" + ADDITIONAL_CONFIG_URL + ", please change it and restart.");
    }
    if (!StringUtils.isEmpty(System.getProperty("cse.configurationSource.defaultFileName"))) {
      throw new IllegalArgumentException("-Dcse.configurationSource.additionalUrls"
          + " has been replaced with -D" + DEFAULT_FILE_NAME + ", please change it and restart.");
    }
  }

  public void loadAndSort() {
    try {
      String configFileFromClasspath =
          System.getProperty(DEFAULT_FILE_NAME) == null ? DEFAULT_CONFIG_FILE_NAME
              : System.getProperty(DEFAULT_FILE_NAME);
      super.load(configFileFromClasspath);
      loadAdditionalConfig();

      if (configModels.isEmpty()) {
        LOGGER.warn("No URLs will be polled as dynamic configuration sources.");
        LOGGER.warn(
            "To enable URLs as dynamic configuration sources, define System property {} or make {} available on classpath.",
            ADDITIONAL_CONFIG_URL,
            configFileFromClasspath);
      }

      sort();
    } catch (IOException e) {
      throw new ServiceCombException("Failed to load microservice config", e);
    }
  }

  private void loadAdditionalConfig() throws IOException {
    String strUrls = System.getProperty(ADDITIONAL_CONFIG_URL);
    if (StringUtils.isEmpty(strUrls)) {
      return;
    }

    for (String strUrl : strUrls.split(",")) {
      URL url = new URL(strUrl);
      ConfigModel configModel = load(url);
      configModels.add(configModel);
    }
  }
}
