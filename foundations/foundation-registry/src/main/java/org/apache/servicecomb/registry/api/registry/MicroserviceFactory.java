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
package org.apache.servicecomb.registry.api.registry;

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_APPLICATION_ID_KEY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_DESCRIPTION_KEY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_ROLE_KEY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_VERSION_KEY;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.DEFAULT_MICROSERVICE_NAME;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.DEFAULT_SERVICECOMB_ENV;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.SERVICECOMB_ENV;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.APP_MAPPING;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.SERVICE_MAPPING;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.VERSION_MAPPING;

import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.registry.config.ConfigurePropertyUtils;
import org.apache.servicecomb.registry.config.MicroservicePropertiesLoader;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.apache.servicecomb.registry.definition.MicroserviceDefinition;
import org.apache.servicecomb.registry.version.Version;

public class MicroserviceFactory {
  public Microservice create(String appId, String microserviceName) {
    MicroserviceDefinition microserviceDefinition = MicroserviceDefinition.create(appId, microserviceName);
    return create(microserviceDefinition);
  }

  public Microservice create(MicroserviceDefinition microserviceDefinition) {
    Configuration configuration = microserviceDefinition.getConfiguration();
    Microservice microservice = createMicroserviceFromDefinition(configuration);
    microservice.setInstance(MicroserviceInstance.createFromDefinition(configuration));
    return microservice;
  }

  private Microservice createMicroserviceFromDefinition(Configuration configuration) {
    Microservice microservice = new Microservice();

    EnvironmentConfiguration envConfig = new EnvironmentConfiguration();
    if (!StringUtils.isEmpty(envConfig.getString(APP_MAPPING)) &&
        !StringUtils.isEmpty(envConfig.getString(envConfig.getString(APP_MAPPING)))) {
      microservice.setAppId(envConfig.getString(envConfig.getString(APP_MAPPING)));
    } else {
      microservice.setAppId(configuration
          .getString(CONFIG_APPLICATION_ID_KEY, DefinitionConst.DEFAULT_APPLICATION_ID));
    }
    if (!StringUtils.isEmpty(envConfig.getString(SERVICE_MAPPING)) &&
        !StringUtils.isEmpty(envConfig.getString(envConfig.getString(SERVICE_MAPPING)))) {
      microservice.setServiceName(envConfig.getString(envConfig.getString(SERVICE_MAPPING)));
    } else {
      microservice.setServiceName(configuration.getString(CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY,
          DEFAULT_MICROSERVICE_NAME));
    }
    String version;
    if (!StringUtils.isEmpty(envConfig.getString(VERSION_MAPPING)) &&
        !StringUtils.isEmpty(envConfig.getString(envConfig.getString(VERSION_MAPPING)))) {
      version = envConfig.getString(envConfig.getString(VERSION_MAPPING));
    } else {
      version = configuration.getString(CONFIG_QUALIFIED_MICROSERVICE_VERSION_KEY,
          DefinitionConst.DEFAULT_MICROSERVICE_VERSION);
    }
    // just check version format
    new Version(version);
    microservice.setVersion(version);

    setDescription(configuration, microservice);
    microservice.setLevel(configuration.getString(CONFIG_QUALIFIED_MICROSERVICE_ROLE_KEY, "FRONT"));
    microservice.setPaths(ConfigurePropertyUtils.getMicroservicePaths(configuration));
    Map<String, String> propertiesMap = MicroservicePropertiesLoader.INSTANCE.loadProperties(configuration);
    microservice.setProperties(propertiesMap);
    microservice.setEnvironment(configuration.getString(SERVICECOMB_ENV, DEFAULT_SERVICECOMB_ENV));

    // set alias name when allow cross app
    if (allowCrossApp(propertiesMap)) {
      microservice.setAlias(Microservice.generateAbsoluteMicroserviceName(microservice.getAppId(),
          microservice.getServiceName()));
    }

    return microservice;
  }

  /**
   * {@code service_description.description} is split by {@code ,},
   * need to combine the description array to raw description.
   */
  private void setDescription(Configuration configuration, Microservice microservice) {
    String[] descriptionArray = configuration.getStringArray(CONFIG_QUALIFIED_MICROSERVICE_DESCRIPTION_KEY);
    if (null == descriptionArray || descriptionArray.length < 1) {
      return;
    }

    StringBuilder rawDescriptionBuilder = new StringBuilder();
    for (String desc : descriptionArray) {
      rawDescriptionBuilder.append(desc).append(",");
    }

    microservice.setDescription(rawDescriptionBuilder.substring(0, rawDescriptionBuilder.length() - 1));
  }

  private boolean allowCrossApp(Map<String, String> propertiesMap) {
    return Boolean.parseBoolean(propertiesMap.get(DefinitionConst.CONFIG_ALLOW_CROSS_APP_KEY));
  }
}
