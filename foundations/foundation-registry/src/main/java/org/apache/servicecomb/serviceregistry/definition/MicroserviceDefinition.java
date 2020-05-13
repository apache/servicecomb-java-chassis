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

package org.apache.servicecomb.serviceregistry.definition;

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_APPLICATION_ID_KEY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_MICROSERVICE_NAME_KEY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_SERVICE_DESCRIPTION_KEY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.DEFAULT_APPLICATION_ID;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.DEFAULT_MICROSERVICE_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.archaius.sources.ConfigModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class MicroserviceDefinition {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceDefinition.class);

  // microservice maybe combined from many microservices
  // if a and b combined to ab, then combinedFrom value is a,b
  // if not combined, just only one microservice, then combinedFrom is empty
  private Set<String> combinedFrom = new HashSet<>();

  private List<ConfigModel> configModels;

  private Configuration configuration;

  private String microserviceName;

  private String applicationId;

  public String getMicroserviceName() {
    return microserviceName;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public static MicroserviceDefinition create(String appId, String microserviceName) {
    ConfigModel configModel = createConfigModel(appId, microserviceName);
    return new MicroserviceDefinition(Arrays.asList(configModel));
  }

  public static ConfigModel createConfigModel(String appId, String microserviceName) {
    Map<String, Object> descMap = new HashMap<>();
    descMap.put(CONFIG_MICROSERVICE_NAME_KEY, microserviceName);

    Map<String, Object> config = new HashMap<>();
    config.put(CONFIG_APPLICATION_ID_KEY, appId);
    config.put(CONFIG_SERVICE_DESCRIPTION_KEY, descMap);

    ConfigModel configModel = new ConfigModel();
    configModel.setConfig(config);
    return configModel;
  }

  public MicroserviceDefinition(List<ConfigModel> configModels) {
    if (configModels == null) {
      configModels = Collections.emptyList();
    }

    this.configModels = configModels;
    this.configuration = ConfigUtil.createLocalConfig(configModels);
    this.microserviceName =
        configuration.getString(CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY, DEFAULT_MICROSERVICE_NAME);

    this.applicationId = configuration.getString(CONFIG_APPLICATION_ID_KEY, DEFAULT_APPLICATION_ID);

    // log paths first, even microserviceName is invalid, this can help user to find problems
    logConfigPath();
    checkMicroserviceName(microserviceName);

    initCombinedFrom(configModels);
  }

  public Set<String> getCombinedFrom() {
    return combinedFrom;
  }

  private void initCombinedFrom(List<ConfigModel> configModels) {
    for (ConfigModel model : configModels) {
      Configuration conf = ConfigUtil.createLocalConfig(Arrays.asList(model));
      String name =
          conf.getString(CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY, DEFAULT_MICROSERVICE_NAME);
      if (!StringUtils.isEmpty(name)) {
        checkMicroserviceName(name);
        combinedFrom.add(name);
      }
    }

    combinedFrom.remove(microserviceName);
  }

  private void checkMicroserviceName(String name) {
    // the configuration we used
    // when resolve placeholder failed
    // the result will remains ${var}
    if (StringUtils.isEmpty(name) || name.contains("${")) {
      throw new IllegalArgumentException(String.format(
          "MicroserviceName '%s' is invalid. you must configure '%s' or set the placeholder value.",
          name,
          CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY));
    }
  }

  // microserviceName maybe null
  public void logConfigPath() {
    List<String> pathList = new ArrayList<>();
    for (ConfigModel configModel : configModels) {
      if (configModel.getUrl() != null) {
        pathList.add(configModel.getUrl().toString());
      }
    }
    LOGGER.info("load microservice config, name={}, paths={}",
        microserviceName,
        pathList);
  }

  public List<ConfigModel> getConfigModels() {
    return configModels;
  }

  public Configuration getConfiguration() {
    return configuration;
  }
}
